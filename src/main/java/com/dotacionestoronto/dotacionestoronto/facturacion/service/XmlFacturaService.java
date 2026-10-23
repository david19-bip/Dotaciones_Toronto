package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.DetalleFacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;

/**
 * Construye el XML de la factura electrónica de venta en formato UBL 2.1
 * con las extensiones propias de la DIAN (Anexo Técnico v1.9,
 * Resolución 000165 de 2023).
 *
 * El XML nace sin firma: la firma XAdES-BES se inserta después en
 * {@link FirmaDigitalService} dentro del segundo UBLExtension.
 */
@Service
public class XmlFacturaService {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter F_HORA = DateTimeFormatter.ofPattern("HH:mm:ssXXX");

    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
    private static final String NS_STS = "dian:gov:co:facturaelectronica:Structures-2-1";
    private static final String NS_DS  = "http://www.w3.org/2000/09/xmldsig#";
    private static final String NS_XADES = "http://uri.etsi.org/01903/v1.3.2#";
    private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    private static final String AGENCY_ID = "195";
    private static final String AGENCY_NAME = "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)";

    private final CufeService cufeService;

    public XmlFacturaService(CufeService cufeService) {
        this.cufeService = cufeService;
    }

    /**
     * Genera el XML completo (sin firma) listo para firmarse y enviarse.
     */
    public String construir(FacturaElectronica f) {

        StringBuilder x = new StringBuilder(8192);

        x.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        x.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"");
        x.append(" xmlns:cac=\"").append(NS_CAC).append("\"");
        x.append(" xmlns:cbc=\"").append(NS_CBC).append("\"");
        x.append(" xmlns:ds=\"").append(NS_DS).append("\"");
        x.append(" xmlns:ext=\"").append(NS_EXT).append("\"");
        x.append(" xmlns:sts=\"").append(NS_STS).append("\"");
        x.append(" xmlns:xades=\"").append(NS_XADES).append("\"");
        x.append(" xmlns:xsi=\"").append(NS_XSI).append("\">");

        // ---------- 1. UBLExtensions (DianExtensions + espacio para la firma) ----------
        x.append(extensions(f));

        // ---------- 2. Cabecera ----------
        x.append("<cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>");
        x.append("<cbc:CustomizationID>10</cbc:CustomizationID>");
        x.append("<cbc:ProfileID>DIAN 2.1</cbc:ProfileID>");
        x.append("<cbc:ProfileExecutionID>").append(nvl(f.getAmbiente(), "2")).append("</cbc:ProfileExecutionID>");
        x.append("<cbc:ID>").append(esc(f.getNumeroFactura())).append("</cbc:ID>");
        x.append("<cbc:UUID schemeID=\"").append(nvl(f.getAmbiente(), "2"))
         .append("\" schemeName=\"CUFE-SHA384\">").append(esc(f.getCufe())).append("</cbc:UUID>");
        x.append("<cbc:IssueDate>").append(f.getFechaEmision().format(F_FECHA)).append("</cbc:IssueDate>");
        x.append("<cbc:IssueTime>")
         .append(f.getFechaEmision().atZone(CufeService.ZONA_COLOMBIA).format(F_HORA))
         .append("</cbc:IssueTime>");
        x.append("<cbc:InvoiceTypeCode>").append(nvl(f.getTipoDocumento(), "01")).append("</cbc:InvoiceTypeCode>");
        x.append("<cbc:DocumentCurrencyCode>").append(nvl(f.getMoneda(), "COP")).append("</cbc:DocumentCurrencyCode>");
        x.append("<cbc:LineCountNumeric>").append(f.getDetalles().size()).append("</cbc:LineCountNumeric>");

        // Referencia al pedido del sistema (trazabilidad interna)
        if (f.getIdPedido() != null) {
            x.append("<cac:OrderReference>")
             .append("<cbc:ID>").append(f.getIdPedido()).append("</cbc:ID>")
             .append("</cac:OrderReference>");
        }

        // ---------- 3. Emisor y adquirente ----------
        x.append(emisor(f));
        x.append(adquirente(f));

        // ---------- 4. Forma de pago ----------
        x.append("<cac:PaymentMeans>")
         .append("<cbc:ID>").append(nvl(f.getFormaPago(), "1")).append("</cbc:ID>")
         .append("<cbc:PaymentMeansCode>").append(nvl(f.getMedioPago(), "10")).append("</cbc:PaymentMeansCode>")
         .append("<cbc:PaymentDueDate>").append(f.getFechaEmision().format(F_FECHA)).append("</cbc:PaymentDueDate>")
         .append("<cbc:PaymentID>").append(esc(f.getNumeroFactura())).append("</cbc:PaymentID>")
         .append("</cac:PaymentMeans>");

        // ---------- 5. Impuestos totales ----------
        x.append(totalesImpuestos(f));

        // ---------- 6. Totales monetarios ----------
        x.append("<cac:LegalMonetaryTotal>")
         .append(moneyTag("cbc:LineExtensionAmount", f.getValorBruto()))
         .append(moneyTag("cbc:TaxExclusiveAmount", f.getValorBruto()))
         .append(moneyTag("cbc:TaxInclusiveAmount", f.getTotalFactura()))
         .append(moneyTag("cbc:AllowanceTotalAmount", f.getDescuentoTotal()))
         .append(moneyTag("cbc:PayableAmount", f.getTotalFactura()))
         .append("</cac:LegalMonetaryTotal>");

        // ---------- 7. Líneas ----------
        List<DetalleFacturaElectronica> detalles = f.getDetalles();
        int numero = 1;
        for (DetalleFacturaElectronica d : detalles) {
            x.append(linea(d, numero));
            numero++;
        }

        x.append("</Invoice>");
        return x.toString();
    }

    // ==========================================================
    // UBLEXTENSIONS
    // ==========================================================
    private String extensions(FacturaElectronica f) {
        StringBuilder x = new StringBuilder();
        x.append("<ext:UBLExtensions>");
        x.append("<ext:UBLExtension><ext:ExtensionContent>");
        x.append("<sts:DianExtensions>");

        // --- Control de la factura (resolución de numeración) ---
        x.append("<sts:InvoiceControl>");
        x.append("<sts:InvoiceAuthorization>").append(esc(f.getResolucionNumero())).append("</sts:InvoiceAuthorization>");
        x.append("<sts:AuthorizationPeriod>");
        x.append("<cbc:StartDate>").append(f.getResolucionVigenciaDesde() != null ? f.getResolucionVigenciaDesde().format(F_FECHA) : "").append("</cbc:StartDate>");
        x.append("<cbc:EndDate>").append(f.getResolucionVigenciaHasta() != null ? f.getResolucionVigenciaHasta().format(F_FECHA) : "").append("</cbc:EndDate>");
        x.append("</sts:AuthorizationPeriod>");
        x.append("<sts:AuthorizedInvoices>");
        x.append("<sts:Prefix>").append(esc(f.getPrefijo())).append("</sts:Prefix>");
        x.append("<sts:From>").append(f.getResolucionDesde()).append("</sts:From>");
        x.append("<sts:To>").append(f.getResolucionHasta()).append("</sts:To>");
        x.append("</sts:AuthorizedInvoices>");
        x.append("</sts:InvoiceControl>");

        // --- País ---
        x.append("<sts:InvoiceSource>");
        x.append("<cbc:IdentificationCode listAgencyID=\"6\" listAgencyName=\"United Nations Economic Commission for Europe\" ")
         .append("listSchemeURI=\"urn:oasis:names:specification:ubl:codelist:gc:CountryIdentificationCode-2.1\">CO</cbc:IdentificationCode>");
        x.append("</sts:InvoiceSource>");

        // --- Software ---
        x.append("<sts:SoftwareProvider>");
        x.append("<sts:ProviderID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\" schemeID=\"").append(esc(f.getDvEmisor())).append("\">")
         .append(esc(f.getNitEmisor())).append("</sts:ProviderID>");
        x.append("<sts:SoftwareID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\">").append(esc(f.getSoftwareId())).append("</sts:SoftwareID>");
        x.append("</sts:SoftwareProvider>");

        x.append("<sts:SoftwareSecurityCode schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"")
         .append(AGENCY_NAME).append("\">").append(esc(f.getSoftwareSecurityCode())).append("</sts:SoftwareSecurityCode>");

        // --- Proveedor de autorización (DIAN) ---
        x.append("<sts:AuthorizationProvider>");
        x.append("<sts:AuthorizationProviderID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"")
         .append(AGENCY_NAME).append("\" schemeID=\"4\">800197268</sts:AuthorizationProviderID>");
        x.append("</sts:AuthorizationProvider>");

        // --- Código QR exigido por la DIAN ---
        x.append("<sts:QRCode>").append(esc(f.getQrUrl())).append("</sts:QRCode>");

        x.append("</sts:DianExtensions>");
        x.append("</ext:ExtensionContent></ext:UBLExtension>");

        // Segundo extensión: aquí se inserta ds:Signature
        x.append("<ext:UBLExtension><ext:ExtensionContent/></ext:UBLExtension>");
        x.append("</ext:UBLExtensions>");
        return x.toString();
    }

    // ==========================================================
    // EMISOR
    // ==========================================================
    private String emisor(FacturaElectronica f) {
        StringBuilder x = new StringBuilder();
        String nit = esc(f.getNitEmisor());

        x.append("<cac:AccountingSupplierParty>");
        x.append("<cbc:AdditionalAccountID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"")
         .append(AGENCY_NAME).append("\">1</cbc:AdditionalAccountID>");
        x.append("<cac:Party>");

        x.append("<cac:PartyIdentification>");
        x.append("<cbc:ID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\" schemeID=\"31\" schemeName=\"31\">").append(nit).append("</cbc:ID>");
        x.append("</cac:PartyIdentification>");

        x.append("<cac:PartyName><cbc:Name>")
         .append(esc(f.getNombreComercialEmisor() != null ? f.getNombreComercialEmisor() : f.getRazonSocialEmisor()))
         .append("</cbc:Name></cac:PartyName>");

        x.append("<cac:PartyTaxScheme>");
        x.append("<cbc:RegistrationName>").append(esc(f.getRazonSocialEmisor())).append("</cbc:RegistrationName>");
        x.append("<cbc:CompanyID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\" schemeID=\"0\" schemeName=\"31\">").append(nit).append("-").append(esc(f.getDvEmisor())).append("</cbc:CompanyID>");
        x.append("<cbc:TaxLevelCode listName=\"").append(nvl(f.getRegimenEmisor(), "48")).append("\">")
         .append(esc(nvl(f.getResponsabilidadesEmisor(), "O-13"))).append("</cbc:TaxLevelCode>");
        x.append("<cac:TaxScheme><cbc:ID>01</cbc:ID><cbc:Name>IVA</cbc:Name></cac:TaxScheme>");
        x.append("</cac:PartyTaxScheme>");

        x.append("<cac:PartyLegalEntity>");
        x.append("<cbc:RegistrationName>").append(esc(f.getRazonSocialEmisor())).append("</cbc:RegistrationName>");
        x.append("<cbc:CompanyID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\" schemeID=\"0\" schemeName=\"31\">").append(nit).append("-").append(esc(f.getDvEmisor())).append("</cbc:CompanyID>");
        x.append("<cac:CorporateRegistrationScheme>");
        x.append("<cbc:ID>").append(esc(f.getPrefijo())).append("</cbc:ID>");
        x.append("<cbc:Name>").append(esc(f.getResolucionNumero())).append("</cbc:Name>");
        x.append("</cac:CorporateRegistrationScheme>");
        x.append(direccion(f.getDireccionEmisor(), f.getCiudadEmisor(), null, f.getDepartamentoEmisor()));
        x.append("</cac:PartyLegalEntity>");

        x.append("<cac:Contact>");
        x.append("<cbc:Name>").append(esc(f.getNombreComercialEmisor())).append("</cbc:Name>");
        x.append("<cbc:Telephone>").append(esc(f.getTelefonoEmisor())).append("</cbc:Telephone>");
        x.append("<cbc:ElectronicMail>").append(esc(f.getCorreoEmisor())).append("</cbc:ElectronicMail>");
        x.append("</cac:Contact>");

        x.append("</cac:Party></cac:AccountingSupplierParty>");
        return x.toString();
    }

    // ==========================================================
    // ADQUIRENTE
    // ==========================================================
    private String adquirente(FacturaElectronica f) {
        StringBuilder x = new StringBuilder();

        String tipoDoc = nvl(f.getTipoDocAdquirente(), "13");
        String documento = cufeService.soloDigitos(f.getNitAdquirente());
        String nombre = f.getRazonSocialAdquirente();

        // Consumidor final (DIAN): documento genérico 222222222222
        if (documento == null || documento.isBlank()) {
            documento = "222222222222";
            nombre = "consumidor final";
            tipoDoc = "13";
        }

        x.append("<cac:AccountingCustomerParty>");
        x.append("<cac:Party>");

        x.append("<cac:PartyIdentification>");
        x.append("<cbc:ID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\" schemeID=\"").append(esc(tipoDoc)).append("\" schemeName=\"").append(esc(tipoDoc)).append("\">")
         .append(esc(documento)).append("</cbc:ID>");
        x.append("</cac:PartyIdentification>");

        x.append("<cac:PartyName><cbc:Name>").append(esc(nombre)).append("</cbc:Name></cac:PartyName>");

        x.append("<cac:PartyTaxScheme>");
        x.append("<cbc:RegistrationName>").append(esc(nombre)).append("</cbc:RegistrationName>");
        x.append("<cbc:CompanyID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\" schemeID=\"").append(esc(tipoDoc)).append("\" schemeName=\"").append(esc(tipoDoc)).append("\">")
         .append(esc(documento)).append("</cbc:CompanyID>");
        x.append("<cbc:TaxLevelCode listName=\"ZZ\">R-99-PN</cbc:TaxLevelCode>");
        x.append("<cac:TaxScheme><cbc:ID>ZZ</cbc:ID><cbc:Name>No aplica</cbc:Name></cac:TaxScheme>");
        x.append("</cac:PartyTaxScheme>");

        x.append("<cac:PartyLegalEntity>");
        x.append("<cbc:RegistrationName>").append(esc(nombre)).append("</cbc:RegistrationName>");
        x.append("<cbc:CompanyID schemeAgencyID=\"").append(AGENCY_ID).append("\" schemeAgencyName=\"").append(AGENCY_NAME)
         .append("\" schemeID=\"").append(esc(tipoDoc)).append("\" schemeName=\"").append(esc(tipoDoc)).append("\">")
         .append(esc(documento)).append("</cbc:CompanyID>");
        x.append(direccion(f.getDireccionAdquirente(), f.getCiudadAdquirente(), null, null));
        x.append("</cac:PartyLegalEntity>");

        x.append("<cac:Contact>");
        if (f.getTelefonoAdquirente() != null) {
            x.append("<cbc:Telephone>").append(esc(f.getTelefonoAdquirente())).append("</cbc:Telephone>");
        }
        if (f.getCorreoAdquirente() != null) {
            x.append("<cbc:ElectronicMail>").append(esc(f.getCorreoAdquirente())).append("</cbc:ElectronicMail>");
        }
        x.append("</cac:Contact>");

        x.append("</cac:Party></cac:AccountingCustomerParty>");
        return x.toString();
    }

    // ==========================================================
    // IMPUESTOS Y TOTALES
    // ==========================================================
    private String totalesImpuestos(FacturaElectronica f) {
        if (f.getTotalIva() == null || f.getTotalIva().compareTo(BigDecimal.ZERO) <= 0) {
            return "";
        }
        BigDecimal ivaPct = BigDecimal.ZERO;
        if (!f.getDetalles().isEmpty() && f.getDetalles().get(0).getIvaPorcentaje() != null) {
            ivaPct = f.getDetalles().get(0).getIvaPorcentaje();
        }

        StringBuilder x = new StringBuilder();
        x.append("<cac:TaxTotal>");
        x.append(moneyTag("cbc:TaxAmount", f.getTotalIva()));
        x.append("<cac:TaxSubtotal>");
        x.append(moneyTag("cbc:TaxableAmount", f.getValorBruto()));
        x.append(moneyTag("cbc:TaxAmount", f.getTotalIva()));
        x.append("<cac:TaxCategory>");
        x.append("<cbc:Percent>").append(ivaPct.setScale(2, RoundingMode.HALF_UP)).append("</cbc:Percent>");
        x.append("<cac:TaxScheme><cbc:ID>01</cbc:ID><cbc:Name>IVA</cbc:Name></cac:TaxScheme>");
        x.append("</cac:TaxCategory>");
        x.append("</cac:TaxSubtotal>");
        x.append("</cac:TaxTotal>");
        return x.toString();
    }

    private String linea(DetalleFacturaElectronica d, int numero) {
        BigDecimal cantidad = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ONE;
        BigDecimal precio = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : BigDecimal.ZERO;
        BigDecimal iva = d.getIvaValor() != null ? d.getIvaValor() : BigDecimal.ZERO;
        BigDecimal ivaPct = d.getIvaPorcentaje() != null ? d.getIvaPorcentaje() : BigDecimal.ZERO;
        BigDecimal base = precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP)
                .subtract(d.getDescuento() != null ? d.getDescuento() : BigDecimal.ZERO);

        StringBuilder x = new StringBuilder();
        x.append("<cac:InvoiceLine>");
        x.append("<cbc:ID>").append(numero).append("</cbc:ID>");
        x.append("<cbc:InvoicedQuantity unitCode=\"").append(nvl(d.getUnidadMedida(), "94")).append("\">")
         .append(cantidad.setScale(2, RoundingMode.HALF_UP)).append("</cbc:InvoicedQuantity>");
        x.append(moneyTag("cbc:LineExtensionAmount", base));

        if (iva.compareTo(BigDecimal.ZERO) > 0) {
            x.append("<cac:TaxTotal>");
            x.append(moneyTag("cbc:TaxAmount", iva));
            x.append("<cac:TaxSubtotal>");
            x.append(moneyTag("cbc:TaxableAmount", base));
            x.append(moneyTag("cbc:TaxAmount", iva));
            x.append("<cac:TaxCategory>");
            x.append("<cbc:Percent>").append(ivaPct.setScale(2, RoundingMode.HALF_UP)).append("</cbc:Percent>");
            x.append("<cac:TaxScheme><cbc:ID>01</cbc:ID><cbc:Name>IVA</cbc:Name></cac:TaxScheme>");
            x.append("</cac:TaxCategory></cac:TaxSubtotal></cac:TaxTotal>");
        }

        x.append("<cac:Item>");
        x.append("<cbc:Description>").append(esc(d.getDescripcion())).append("</cbc:Description>");
        x.append("<cac:SellersItemIdentification>");
        x.append("<cbc:ID>").append(esc(d.getCodigoProducto())).append("</cbc:ID>");
        x.append("</cac:SellersItemIdentification>");
        x.append("<cac:StandardItemIdentification>");
        x.append("<cbc:ID schemeID=\"999\">").append(esc(d.getCodigoProducto())).append("</cbc:ID>");
        x.append("</cac:StandardItemIdentification>");
        x.append("</cac:Item>");

        x.append("<cac:Price>");
        x.append(moneyTag("cbc:PriceAmount", precio));
        x.append("<cbc:BaseQuantity unitCode=\"").append(nvl(d.getUnidadMedida(), "94")).append("\">1.00</cbc:BaseQuantity>");
        x.append("</cac:Price>");

        x.append("</cac:InvoiceLine>");
        return x.toString();
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    private String direccion(String linea, String ciudad, String ciudadCodigo, String departamento) {
        if ((linea == null || linea.isBlank()) && (ciudad == null || ciudad.isBlank())) {
            return "";
        }
        StringBuilder x = new StringBuilder();
        x.append("<cac:Address>");
        x.append("<cbc:ID>").append(esc(nvl(ciudadCodigo, "11001"))).append("</cbc:ID>");
        x.append("<cbc:CityName>").append(esc(nvl(ciudad, "Bogotá D.C."))).append("</cbc:CityName>");
        x.append("<cbc:CountrySubentity>").append(esc(nvl(departamento, nvl(ciudad, "Bogotá D.C.")))).append("</cbc:CountrySubentity>");
        x.append("<cbc:CountrySubentityCode>").append(esc(nvl(ciudadCodigo, "11001"))).append("</cbc:CountrySubentityCode>");
        x.append("<cac:AddressLine><cbc:Line>").append(esc(nvl(linea, "No informada"))).append("</cbc:Line></cac:AddressLine>");
        x.append("<cac:Country><cbc:IdentificationCode>CO</cbc:IdentificationCode>");
        x.append("<cbc:Name languageID=\"es\">Colombia</cbc:Name></cac:Country>");
        x.append("</cac:Address>");
        return x.toString();
    }

    private String moneyTag(String tag, BigDecimal valor) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        return "<" + tag + " currencyID=\"COP\">" + v.setScale(2, RoundingMode.HALF_UP).toPlainString() + "</" + tag + ">";
    }

    /** Escapa los caracteres especiales de XML. */
    public String esc(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String nvl(String valor, String defecto) {
        return (valor == null || valor.isBlank()) ? defecto : valor;
    }

    /** Nombre del archivo XML según la convención de la DIAN. */
    public String nombreArchivo(FacturaElectronica f) {
        return cufeService.soloDigitos(f.getNitEmisor()) + f.getTipoDocumento() + f.getNumeroFactura() + ".xml";
    }

    /** Fecha y hora en formato DIAN. */
    public String fechaHoraDian(LocalDateTime fecha) {
        return fecha.atZone(CufeService.ZONA_COLOMBIA).format(F_HORA);
    }
}
