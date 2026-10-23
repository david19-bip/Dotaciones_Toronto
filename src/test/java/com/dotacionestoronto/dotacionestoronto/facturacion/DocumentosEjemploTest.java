package com.dotacionestoronto.dotacionestoronto.facturacion;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.DetalleFacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.CufeService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.FirmaDigitalService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.PdfFacturaService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.QrService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.XmlFacturaService;

/**
 * Genera documentos de ejemplo (XML firmado, PDF y QR) en target/demo para
 * revisar el resultado del módulo sin necesidad de base de datos ni DIAN.
 */
class DocumentosEjemploTest {

    @Test
    @DisplayName("Genera los documentos de ejemplo en target/demo")
    void generarDocumentos() throws Exception {

        FacturacionProperties props = new FacturacionProperties();
        CufeService cufeService = new CufeService();
        XmlFacturaService xmlService = new XmlFacturaService(cufeService);
        FirmaDigitalService firmaService = new FirmaDigitalService(props, cufeService);
        QrService qrService = new QrService();
        PdfFacturaService pdfService = new PdfFacturaService(props, cufeService, qrService);

        FacturaElectronica f = ejemplo(cufeService);

        String xml = xmlService.construir(f);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(getClass().getResourceAsStream("/certificado/firma-pruebas.p12"), "prueba123".toCharArray());
        PrivateKey llave = (PrivateKey) ks.getKey("prueba", "prueba123".toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate("prueba");

        String xmlFirmado = firmaService.firmar(xml, llave, cert);

        Path salida = Path.of("target", "demo");
        Files.createDirectories(salida);
        Files.writeString(salida.resolve("factura-ejemplo.xml"), xmlFirmado, StandardCharsets.UTF_8);
        Files.write(salida.resolve("factura-ejemplo.pdf"), pdfService.generar(f));
        Files.write(salida.resolve("factura-qr.png"), qrService.generarPng(f.getQrUrl(), 300));

        new File(salida.toFile(), "LEEME.txt").toPath();
        Files.writeString(salida.resolve("LEEME.txt"),
                "Documentos de ejemplo generados por el módulo de facturación electrónica.\n"
                + "Factura: " + f.getNumeroFactura() + "\n"
                + "CUFE: " + f.getCufe() + "\n"
                + "Cadena CUFE: " + f.getCadenaCufe() + "\n"
                + "Firmado con el certificado de PRUEBAS incluido en src/test/resources/certificado.\n",
                StandardCharsets.UTF_8);
    }

    private FacturaElectronica ejemplo(CufeService cufeService) {

        FacturaElectronica f = new FacturaElectronica();
        f.setNumeroFactura("DT00001");
        f.setPrefijo("DT");
        f.setConsecutivo(1L);
        f.setFechaEmision(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        f.setEstado(FacturaElectronica.ESTADO_ACEPTADA);
        f.setAmbiente("2");
        f.setTipoDocumento("01");
        f.setMoneda("COP");
        f.setFormaPago("1");
        f.setMedioPago("10");
        f.setIdPedido(7L);

        f.setNitEmisor("900123456");
        f.setDvEmisor(cufeService.calcularDigitoVerificacion("900123456"));
        f.setRazonSocialEmisor("DOTACIONES TORONTO S.A.S.");
        f.setNombreComercialEmisor("Dotaciones Toronto");
        f.setDireccionEmisor("CL 1 # 1 - 1");
        f.setCiudadEmisor("Bogotá D.C.");
        f.setDepartamentoEmisor("Bogotá D.C.");
        f.setTelefonoEmisor("3000000000");
        f.setCorreoEmisor("facturacion@dotacionestoronto.com");
        f.setRegimenEmisor("48");
        f.setResponsabilidadesEmisor("O-13,O-15,O-23");
        f.setActividadEconomicaEmisor("4641");

        f.setTipoDocAdquirente("13");
        f.setNitAdquirente("1012345678");
        f.setRazonSocialAdquirente("Juan Camilo Pérez");
        f.setCorreoAdquirente("juan.perez@correo.com");
        f.setDireccionAdquirente("Calle 100 # 20-30");
        f.setTelefonoAdquirente("3001234567");
        f.setCiudadAdquirente("Bogotá D.C.");

        f.setClaveTecnica("fc8eac422eba16e22ffd8c6f94b3f40a6e38162c");
        f.setResolucionNumero("18760000001");
        f.setResolucionDesde(1L);
        f.setResolucionHasta(5000L);
        f.setResolucionVigenciaDesde(LocalDate.of(2025, 1, 1));
        f.setResolucionVigenciaHasta(LocalDate.of(2027, 12, 31));
        f.setSoftwareId("11111111-2222-3333-4444-555555555555");

        List<DetalleFacturaElectronica> detalles = new ArrayList<>();

        detalles.add(linea(1, "12", "Camisa de trabajo - Talla M - Azul", "2", "50000.00", "19.00", "19000.00", "100000.00"));
        detalles.add(linea(2, "15", "Botas de seguridad punta de acero - Talla 42", "1", "159663.87", "19.00", "30336.13", "159663.87"));
        detalles.add(linea(3, "18", "Casco de seguridad amarillo", "3", "42016.81", "19.00", "23949.58", "126050.43"));

        f.setDetalles(detalles);
        f.setValorBruto(new BigDecimal("385714.30"));
        f.setDescuentoTotal(BigDecimal.ZERO);
        f.setTotalIva(new BigDecimal("73285.71"));
        f.setTotalImpuestos(new BigDecimal("73285.71"));
        f.setTotalFactura(new BigDecimal("459000.01"));

        cufeService.calcularYAsignar(f);
        f.setSoftwareSecurityCode(cufeService.calcularSoftwareSecurityCode("11111111-2222-3333-4444-555555555555", "00000", f.getNumeroFactura()));
        f.setQrUrl(cufeService.construirUrlQr(f));
        f.setTrackId("12345678-1234-1234-1234-123456789012");
        f.setMensajeDian("Documento aceptado en modo simulación.");
        f.setIntentos(1);
        f.setSimulado(Boolean.TRUE);

        return f;
    }

    private DetalleFacturaElectronica linea(int n, String codigo, String descripcion, String cantidad,
                                            String precio, String ivaPct, String ivaValor, String total) {
        DetalleFacturaElectronica d = new DetalleFacturaElectronica();
        d.setNumeroLinea(n);
        d.setCodigoProducto(codigo);
        d.setDescripcion(descripcion);
        d.setCantidad(new BigDecimal(cantidad));
        d.setUnidadMedida("94");
        d.setPrecioUnitario(new BigDecimal(precio));
        d.setDescuento(BigDecimal.ZERO);
        d.setIvaPorcentaje(new BigDecimal(ivaPct));
        d.setIvaValor(new BigDecimal(ivaValor));
        d.setTotalLinea(new BigDecimal(total));
        return d;
    }
}
