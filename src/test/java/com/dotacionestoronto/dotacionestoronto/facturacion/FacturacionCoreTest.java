package com.dotacionestoronto.dotacionestoronto.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.DetalleFacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.CufeService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.FirmaDigitalService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.PdfFacturaService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.QrService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.XmlFacturaService;

/**
 * Pruebas del núcleo del módulo: CUFE, XML UBL 2.1, firma XAdES-BES,
 * código QR y representación gráfica en PDF.
 *
 * No requieren base de datos ni conexión a la DIAN.
 */
class FacturacionCoreTest {

    private CufeService cufeService;
    private XmlFacturaService xmlService;
    private FirmaDigitalService firmaService;
    private PdfFacturaService pdfService;
    private QrService qrService;
    private FacturacionProperties props;

    @BeforeEach
    void setUp() {
        props = new FacturacionProperties();
        cufeService = new CufeService();
        xmlService = new XmlFacturaService(cufeService);
        firmaService = new FirmaDigitalService(props, cufeService);
        qrService = new QrService();
        pdfService = new PdfFacturaService(props, cufeService, qrService);
    }

    // ==========================================================
    // CUFE
    // ==========================================================
    @Test
    @DisplayName("El SHA-384 produce el hash hexadecimal esperado")
    void sha384() {
        // Vector conocido: SHA-384("abc")
        assertEquals("cb00753f45a35e8bb5a03d699ac65007272c32ab0eded1631a8b605a43ff5bed8086072ba1e7cc2358baeca134c825a7",
                cufeService.sha384Hex("abc"));
    }

    @Test
    @DisplayName("El CUFE se calcula con la cadena oficial del Anexo 1.9")
    void cufe() {

        FacturaElectronica f = facturaEjemplo();
        String cufe = cufeService.calcularYAsignar(f);

        assertNotNull(cufe);
        assertEquals(96, cufe.length(), "El CUFE debe tener 96 caracteres hexadecimales");

        String esperada = "DT00001"
                + "2026-10-23"
                + cufeService.horaConZona(f.getFechaEmision())
                + "100000.00"       // ValorFactura sin impuestos
                + "01" + "19000.00" // IVA
                + "04" + "0.00"     // INC
                + "03" + "0.00"     // ICA
                + "119000.00"       // Total
                + "900123456"       // NIT emisor
                + "1012345678"      // Documento adquirente
                + "clave-tecnica-de-prueba"
                + "2";              // Ambiente de pruebas

        assertEquals(esperada, f.getCadenaCufe());
        assertEquals(cufeService.sha384Hex(esperada), cufe);
    }

    @Test
    @DisplayName("El SoftwareSecurityCode es SHA-384(SoftwareID + PIN + NumFac)")
    void softwareSecurityCode() {
        String codigo = cufeService.calcularSoftwareSecurityCode("ABC-123", "98765", "DT00001");
        assertEquals(cufeService.sha384Hex("ABC-12398765DT00001"), codigo);
        assertEquals(96, codigo.length());
    }

    @Test
    @DisplayName("El dígito de verificación del NIT se calcula con módulo 11")
    void digitoVerificacion() {
        // NIT de la DIAN 800.197.268 -> dígito de verificación 4
        assertEquals("4", cufeService.calcularDigitoVerificacion("800197268"));
        assertEquals("8", cufeService.calcularDigitoVerificacion("900123456"));
        assertEquals(1, cufeService.calcularDigitoVerificacion("900123456").length());
    }

    // ==========================================================
    // XML
    // ==========================================================
    @Test
    @DisplayName("El XML es UBL 2.1 válido y contiene las extensiones DIAN")
    void xmlUbl() throws Exception {

        FacturaElectronica f = facturaEjemplo();
        cufeService.calcularYAsignar(f);
        f.setQrUrl(cufeService.construirUrlQr(f));

        String xml = xmlService.construir(f);

        Document doc = parsear(xml);
        Element raiz = doc.getDocumentElement();

        assertEquals("Invoice", raiz.getLocalName());
        assertEquals(2, raiz.getElementsByTagNameNS("*", "UBLExtension").getLength());
        assertEquals(1, raiz.getElementsByTagNameNS("*", "DianExtensions").getLength());
        assertEquals(1, raiz.getElementsByTagNameNS("*", "SoftwareSecurityCode").getLength());
        assertEquals(1, raiz.getElementsByTagNameNS("*", "QRCode").getLength());
        assertEquals(2, raiz.getElementsByTagNameNS("*", "InvoiceLine").getLength());

        // Totales
        String cufeXml = raiz.getElementsByTagNameNS("*", "UUID").item(0).getTextContent();
        assertEquals(f.getCufe(), cufeXml);

        String payable = raiz.getElementsByTagNameNS("*", "PayableAmount").item(0).getTextContent();
        assertEquals("119000.00", payable);
    }

    @Test
    @DisplayName("El XML escapa caracteres especiales")
    void escapado() {
        FacturaElectronica f = facturaEjemplo();
        f.getDetalles().get(0).setDescripcion("Camisa & Pantalón <talla M> \"edición\"");
        cufeService.calcularYAsignar(f);
        f.setQrUrl(cufeService.construirUrlQr(f));

        String xml = xmlService.construir(f);
        assertTrue(xml.contains("&amp;"));
        assertTrue(xml.contains("&lt;talla M&gt;"));
    }

    // ==========================================================
    // FIRMA XAdES-BES
    // ==========================================================
    @Test
    @DisplayName("El XML firmado supera la verificación de firma (XMLDSig)")
    void firmaValida() throws Exception {

        org.apache.xml.security.Init.init();

        FacturaElectronica f = facturaEjemplo();
        cufeService.calcularYAsignar(f);
        f.setQrUrl(cufeService.construirUrlQr(f));
        String xml = xmlService.construir(f);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(getClass().getResourceAsStream("/certificado/firma-pruebas.p12"), "prueba123".toCharArray());
        PrivateKey llave = (PrivateKey) ks.getKey("prueba", "prueba123".toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate("prueba");

        String firmado = firmaService.firmar(xml, llave, cert);

        assertTrue(firmado.contains("<ds:Signature"), "Debe contener el nodo ds:Signature");
        assertTrue(firmado.contains("SignedProperties"), "Debe contener las propiedades XAdES");
        assertTrue(firmaService.estaFirmado(firmado));

        // Verificación con Apache Santuario (la misma librería que usa la DIAN)
        Document doc = parsear(firmado);

        // El validador marca el atributo Id de SignedProperties (igual que hace
        // la DIAN al validar el documento)
        org.w3c.dom.NodeList props = doc.getElementsByTagNameNS("http://uri.etsi.org/01903/v1.3.2#", "SignedProperties");
        for (int i = 0; i < props.getLength(); i++) {
            ((Element) props.item(i)).setIdAttributeNS(null, "Id", true);
        }

        Element firmaEl = (Element) doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature").item(0);
        org.apache.xml.security.signature.XMLSignature validador =
                new org.apache.xml.security.signature.XMLSignature(firmaEl, "", true);

        assertEquals(2, validador.getSignedInfo().getLength(), "Debe tener 2 referencias");
        assertTrue(validador.checkSignatureValue(cert), "La firma RSA debe ser válida");
    }

    // ==========================================================
    // PDF Y QR
    // ==========================================================
    @Test
    @DisplayName("El PDF de la representación gráfica se genera correctamente")
    void pdf() {
        FacturaElectronica f = facturaEjemplo();
        cufeService.calcularYAsignar(f);
        f.setQrUrl(cufeService.construirUrlQr(f));
        f.setEstado(FacturaElectronica.ESTADO_ACEPTADA);

        byte[] pdf = pdfService.generar(f);

        assertNotNull(pdf);
        assertTrue(pdf.length > 3000, "El PDF debe tener contenido");
        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test
    @DisplayName("El código QR se genera como PNG")
    void qr() {
        byte[] png = qrService.generarPng("https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=abc", 200);
        assertTrue(png.length > 500);
        // Firma PNG
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 'P', png[1]);
    }

    // ==========================================================
    // DATOS DE EJEMPLO
    // ==========================================================
    private FacturaElectronica facturaEjemplo() {

        FacturaElectronica f = new FacturaElectronica();
        f.setNumeroFactura("DT00001");
        f.setPrefijo("DT");
        f.setConsecutivo(1L);
        f.setFechaEmision(LocalDateTime.of(2026, 10, 23, 10, 30, 0));
        f.setEstado(FacturaElectronica.ESTADO_GENERADA);
        f.setAmbiente("2");
        f.setTipoDocumento("01");
        f.setMoneda("COP");
        f.setFormaPago("1");
        f.setMedioPago("10");

        f.setNitEmisor("900123456");
        f.setDvEmisor("1");
        f.setRazonSocialEmisor("DOTACIONES TORONTO S.A.S.");
        f.setNombreComercialEmisor("Dotaciones Toronto");
        f.setDireccionEmisor("CL 1 # 1 - 1");
        f.setCiudadEmisor("Bogotá D.C.");
        f.setDepartamentoEmisor("Bogotá D.C.");
        f.setTelefonoEmisor("3000000000");
        f.setCorreoEmisor("facturacion@dotacionestoronto.com");
        f.setRegimenEmisor("48");
        f.setResponsabilidadesEmisor("O-13,O-15");
        f.setActividadEconomicaEmisor("4641");

        f.setTipoDocAdquirente("13");
        f.setNitAdquirente("1012345678");
        f.setRazonSocialAdquirente("Cliente de Prueba");
        f.setCorreoAdquirente("cliente@correo.com");
        f.setDireccionAdquirente("Calle 100 # 20-30");
        f.setCiudadAdquirente("Bogotá D.C.");

        f.setClaveTecnica("clave-tecnica-de-prueba");
        f.setResolucionNumero("18760000001");
        f.setResolucionDesde(1L);
        f.setResolucionHasta(5000L);
        f.setResolucionVigenciaDesde(LocalDate.of(2025, 1, 1));
        f.setResolucionVigenciaHasta(LocalDate.of(2027, 12, 31));
        f.setSoftwareId("11111111-2222-3333-4444-555555555555");

        f.setValorBruto(new BigDecimal("100000.00"));
        f.setTotalIva(new BigDecimal("19000.00"));
        f.setTotalImpuestos(new BigDecimal("19000.00"));
        f.setTotalFactura(new BigDecimal("119000.00"));
        f.setDescuentoTotal(BigDecimal.ZERO);
        f.setSoftwareSecurityCode("code");

        List<DetalleFacturaElectronica> detalles = new ArrayList<>();

        DetalleFacturaElectronica d1 = new DetalleFacturaElectronica();
        d1.setNumeroLinea(1);
        d1.setCodigoProducto("12");
        d1.setDescripcion("Camisa de trabajo - Talla M - Azul");
        d1.setCantidad(new BigDecimal("2"));
        d1.setUnidadMedida("94");
        d1.setPrecioUnitario(new BigDecimal("42016.81"));
        d1.setIvaPorcentaje(new BigDecimal("19.00"));
        d1.setIvaValor(new BigDecimal("15966.39"));
        d1.setTotalLinea(new BigDecimal("84033.62"));
        d1.setDescuento(BigDecimal.ZERO);
        detalles.add(d1);

        DetalleFacturaElectronica d2 = new DetalleFacturaElectronica();
        d2.setNumeroLinea(2);
        d2.setCodigoProducto("15");
        d2.setDescripcion("Botas de seguridad - Talla 42");
        d2.setCantidad(new BigDecimal("1"));
        d2.setUnidadMedida("94");
        d2.setPrecioUnitario(new BigDecimal("15966.38"));
        d2.setIvaPorcentaje(new BigDecimal("19.00"));
        d2.setIvaValor(new BigDecimal("3033.61"));
        d2.setTotalLinea(new BigDecimal("15966.38"));
        d2.setDescuento(BigDecimal.ZERO);
        detalles.add(d2);

        f.setDetalles(detalles);
        return f;
    }

    private Document parsear(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }
}
