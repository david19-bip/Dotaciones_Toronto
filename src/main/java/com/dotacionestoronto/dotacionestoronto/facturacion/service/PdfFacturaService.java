package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.DetalleFacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Representación gráfica (PDF) de la factura electrónica de venta.
 *
 * Cumple los requisitos mínimos del artículo 35 de la Resolución 000165 de
 * 2023: datos del emisor y adquirente, numeración autorizada, fecha y hora,
 * descripción, impuestos, totales, CUFE y código QR.
 */
@Service
public class PdfFacturaService {

    private static final Color AZUL = new Color(22, 33, 62);
    private static final Color DORADO = new Color(226, 185, 111);
    private static final Color GRIS_TEXTO = new Color(90, 100, 120);
    private static final Color GRIS_FONDO = new Color(244, 246, 251);
    private static final Color VERDE = new Color(25, 135, 84);
    private static final Color ROJO = new Color(200, 40, 40);

    private static final DateTimeFormatter F_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final FacturacionProperties props;
    private final CufeService cufeService;
    private final QrService qrService;

    public PdfFacturaService(FacturacionProperties props, CufeService cufeService, QrService qrService) {
        this.props = props;
        this.cufeService = cufeService;
        this.qrService = qrService;
    }

    public String nombreArchivo(FacturaElectronica f) {
        return "Factura-" + f.getNumeroFactura() + ".pdf";
    }

    public byte[] generar(FacturaElectronica f) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 40);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(cabecera(f));
            doc.add(espacio(10));
            doc.add(partes(f));
            doc.add(espacio(10));
            doc.add(tablaItems(f));
            doc.add(espacio(8));
            doc.add(totales(f));
            doc.add(espacio(12));
            doc.add(pie(f));

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el PDF de la factura: " + e.getMessage(), e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
    }

    // ==========================================================
    // CABECERA
    // ==========================================================
    private PdfPTable cabecera(FacturaElectronica f) throws Exception {

        PdfPTable tabla = new PdfPTable(new float[]{ 55f, 45f });
        tabla.setWidthPercentage(100);

        // -------- Izquierda: logo y datos del emisor --------
        PdfPCell izquierda = new PdfPCell();
        izquierda.setBorder(Rectangle.NO_BORDER);
        izquierda.setPadding(0);

        Image logo = cargarLogo();
        if (logo != null) {
            logo.scaleToFit(150, 60);
            logo.setAlignment(Image.LEFT);
            izquierda.addElement(logo);
        }

        Paragraph razon = new Paragraph(texto(f.getRazonSocialEmisor()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, AZUL));
        razon.setSpacingBefore(6);
        izquierda.addElement(razon);

        izquierda.addElement(new Paragraph(
                "NIT: " + texto(f.getNitEmisor()) + "-" + texto(f.getDvEmisor())
                        + "   ·   Régimen: " + texto(f.getRegimenEmisor()),
                FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTO)));

        izquierda.addElement(new Paragraph(
                texto(f.getDireccionEmisor()) + "  ·  " + texto(f.getCiudadEmisor()),
                FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTO)));

        izquierda.addElement(new Paragraph(
                "Tel: " + texto(f.getTelefonoEmisor()) + "  ·  " + texto(f.getCorreoEmisor()),
                FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTO)));

        izquierda.addElement(new Paragraph(
                "Actividad económica: " + texto(f.getActividadEconomicaEmisor()),
                FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTO)));

        tabla.addCell(izquierda);

        // -------- Derecha: recuadro del documento --------
        PdfPCell derecha = new PdfPCell();
        derecha.setBorder(Rectangle.BOX);
        derecha.setBorderColor(DORADO);
        derecha.setBorderWidth(1.2f);
        derecha.setBackgroundColor(GRIS_FONDO);
        derecha.setPadding(10);

        Paragraph titulo = new Paragraph("FACTURA ELECTRÓNICA DE VENTA",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, AZUL));
        titulo.setAlignment(Element.ALIGN_CENTER);
        derecha.addElement(titulo);

        Paragraph numero = new Paragraph("No. " + texto(f.getNumeroFactura()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, AZUL));
        numero.setAlignment(Element.ALIGN_CENTER);
        numero.setSpacingBefore(4);
        derecha.addElement(numero);

        Paragraph fecha = new Paragraph(
                "Fecha y hora de expedición: " + (f.getFechaEmision() != null ? f.getFechaEmision().format(F_FECHA_HORA) : "-"),
                FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTO));
        fecha.setAlignment(Element.ALIGN_CENTER);
        derecha.addElement(fecha);

        Paragraph estado = new Paragraph(estadoTexto(f),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8,
                        "ACEPTADA".equals(f.getEstado()) ? VERDE : ("RECHAZADA".equals(f.getEstado()) ? ROJO : GRIS_TEXTO)));
        estado.setAlignment(Element.ALIGN_CENTER);
        estado.setSpacingBefore(3);
        derecha.addElement(estado);

        tabla.addCell(derecha);
        return tabla;
    }

    // ==========================================================
    // EMISOR / ADQUIRENTE / DOCUMENTO
    // ==========================================================
    private PdfPTable partes(FacturaElectronica f) {

        PdfPTable tabla = new PdfPTable(new float[]{ 50f, 50f });
        tabla.setWidthPercentage(100);

        // --- Adquirente ---
        PdfPCell adq = caja("ADQUIRENTE");
        adq.addElement(linea(texto(f.getRazonSocialAdquirente()), 9, true));
        adq.addElement(linea(tipoDocumentoTexto(f.getTipoDocAdquirente()) + ": " + texto(f.getNitAdquirente()), 8, false));
        if (f.getDireccionAdquirente() != null) {
            adq.addElement(linea("Dirección: " + f.getDireccionAdquirente(), 8, false));
        }
        if (f.getTelefonoAdquirente() != null) {
            adq.addElement(linea("Teléfono: " + f.getTelefonoAdquirente(), 8, false));
        }
        if (f.getCorreoAdquirente() != null) {
            adq.addElement(linea("Correo: " + f.getCorreoAdquirente(), 8, false));
        }
        tabla.addCell(adq);

        // --- Datos del documento ---
        PdfPCell info = caja("INFORMACIÓN DEL DOCUMENTO");
        info.addElement(linea("Forma de pago: " + texto(f.getFormaPago()) + "   Medio: " + texto(f.getMedioPago()), 8, false));
        info.addElement(linea("Moneda: " + texto(f.getMoneda()), 8, false));
        if (f.getIdPedido() != null) {
            info.addElement(linea("Pedido asociado: #" + f.getIdPedido(), 8, false));
        }
        info.addElement(linea("Resolución DIAN: " + texto(f.getResolucionNumero()), 8, false));
        info.addElement(linea("Autorización del " + fecha(f.getResolucionVigenciaDesde()) + " al " + fecha(f.getResolucionVigenciaHasta())
                + " · Prefijo " + texto(f.getPrefijo())
                + " del " + f.getResolucionDesde() + " al " + f.getResolucionHasta(), 8, false));
        info.addElement(linea("Ambiente: " + ("1".equals(f.getAmbiente()) ? "Producción" : "Pruebas / habilitación"), 8, false));
        tabla.addCell(info);

        return tabla;
    }

    // ==========================================================
    // ITEMS
    // ==========================================================
    private PdfPTable tablaItems(FacturaElectronica f) {

        PdfPTable tabla = new PdfPTable(new float[]{ 8f, 42f, 12f, 15f, 11f, 12f });
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);

        String[] encabezados = { "#", "Descripción", "Cant.", "Vr. unitario", "IVA", "Total" };
        for (String h : encabezados) {
            PdfPCell c = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
            c.setBackgroundColor(AZUL);
            c.setPadding(5);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(c);
        }

        List<DetalleFacturaElectronica> detalles = f.getDetalles();
        int i = 1;
        for (DetalleFacturaElectronica d : detalles) {
            BigDecimal cantidad = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
            BigDecimal precio = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : BigDecimal.ZERO;
            BigDecimal iva = d.getIvaValor() != null ? d.getIvaValor() : BigDecimal.ZERO;
            BigDecimal totalLinea = precio.multiply(cantidad).add(iva);

            tabla.addCell(celda(String.valueOf(i), Element.ALIGN_CENTER));
            tabla.addCell(celda(texto(d.getDescripcion()) + (d.getCodigoProducto() != null ? "  (" + d.getCodigoProducto() + ")" : ""), Element.ALIGN_LEFT));
            tabla.addCell(celda(numero(cantidad), Element.ALIGN_CENTER));
            tabla.addCell(celda(moneda(precio), Element.ALIGN_RIGHT));
            tabla.addCell(celda(moneda(iva), Element.ALIGN_RIGHT));
            tabla.addCell(celda(moneda(totalLinea), Element.ALIGN_RIGHT));
            i++;
        }

        if (detalles.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("Sin ítems registrados",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTO)));
            vacio.setColspan(6);
            vacio.setPadding(6);
            tabla.addCell(vacio);
        }

        return tabla;
    }

    // ==========================================================
    // TOTALES
    // ==========================================================
    private PdfPTable totales(FacturaElectronica f) {

        PdfPTable tabla = new PdfPTable(new float[]{ 62f, 38f });
        tabla.setWidthPercentage(100);

        PdfPCell vacia = new PdfPCell();
        vacia.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(vacia);

        PdfPTable t = new PdfPTable(new float[]{ 55f, 45f });
        t.setWidthPercentage(100);

        t.addCell(totalEtiqueta("Subtotal (sin impuestos)"));
        t.addCell(totalValor(moneda(f.getValorBruto())));

        if (f.getDescuentoTotal() != null && f.getDescuentoTotal().compareTo(BigDecimal.ZERO) > 0) {
            t.addCell(totalEtiqueta("Descuentos"));
            t.addCell(totalValor("- " + moneda(f.getDescuentoTotal())));
        }

        t.addCell(totalEtiqueta("IVA"));
        t.addCell(totalValor(moneda(f.getTotalIva())));

        PdfPCell etiquetaTotal = new PdfPCell(new Phrase("TOTAL A PAGAR",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        etiquetaTotal.setBackgroundColor(AZUL);
        etiquetaTotal.setPadding(7);
        t.addCell(etiquetaTotal);

        PdfPCell valorTotal = new PdfPCell(new Phrase(moneda(f.getTotalFactura()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        valorTotal.setBackgroundColor(AZUL);
        valorTotal.setPadding(7);
        valorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(valorTotal);

        PdfPCell contenedor = new PdfPCell(t);
        contenedor.setBorder(Rectangle.NO_BORDER);
        contenedor.setPadding(0);
        tabla.addCell(contenedor);

        return tabla;
    }

    // ==========================================================
    // PIE: CUFE, QR, resolución y software
    // ==========================================================
    private PdfPTable pie(FacturaElectronica f) {

        PdfPTable tabla = new PdfPTable(new float[]{ 22f, 78f });
        tabla.setWidthPercentage(100);

        // --- QR ---
        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(Rectangle.BOX);
        qrCell.setBorderColor(new Color(220, 224, 232));
        qrCell.setPadding(6);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        try {
            String contenidoQr = f.getQrUrl() != null ? f.getQrUrl() : cufeService.construirUrlQr(f);
            BufferedImage qr = qrService.generarImagen(contenidoQr, 240);
            Image qrImg = Image.getInstance(qr, null);
            qrImg.scaleToFit(95, 95);
            qrCell.addElement(qrImg);
            qrCell.addElement(new Paragraph("Escanee para verificar",
                    FontFactory.getFont(FontFactory.HELVETICA, 6, GRIS_TEXTO)));
        } catch (Exception e) {
            qrCell.addElement(new Paragraph("QR no disponible",
                    FontFactory.getFont(FontFactory.HELVETICA, 6, ROJO)));
        }
        tabla.addCell(qrCell);

        // --- Datos fiscales ---
        PdfPCell datos = new PdfPCell();
        datos.setBorder(Rectangle.BOX);
        datos.setBorderColor(new Color(220, 224, 232));
        datos.setPadding(6);

        datos.addElement(new Paragraph("CUFE:",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, AZUL)));
        Paragraph cufe = new Paragraph(texto(f.getCufe()),
                FontFactory.getFont(FontFactory.COURIER, 7, GRIS_TEXTO));
        datos.addElement(cufe);

        datos.addElement(new Paragraph(
                "Software: " + texto(props.getDian().getSoftwareNombre())
                        + "  ·  ID: " + texto(f.getSoftwareId())
                        + "  ·  Fabricante: " + texto(props.getEmisor().getFabricanteSoftware())
                        + " (NIT " + texto(props.getEmisor().getNit()) + ")",
                FontFactory.getFont(FontFactory.HELVETICA, 7, GRIS_TEXTO)));

        datos.addElement(new Paragraph(
                "Resolución de autorización de numeración DIAN No. " + texto(f.getResolucionNumero())
                        + " del " + fecha(f.getResolucionVigenciaDesde())
                        + ", prefijo " + texto(f.getPrefijo())
                        + " del " + f.getResolucionDesde() + " al " + f.getResolucionHasta(),
                FontFactory.getFont(FontFactory.HELVETICA, 7, GRIS_TEXTO)));

        String leyenda = "ACEPTADA".equals(f.getEstado())
                ? ("Documento validado por la DIAN" + (Boolean.TRUE.equals(f.getSimulado()) ? " (modo simulación - sin validez fiscal)" : ""))
                : "Documento generado. Pendiente de validación por la DIAN.";
        Paragraph validacion = new Paragraph(leyenda,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7,
                        "ACEPTADA".equals(f.getEstado()) && !Boolean.TRUE.equals(f.getSimulado()) ? VERDE : GRIS_TEXTO));
        datos.addElement(validacion);

        if (f.getFechaRespuesta() != null) {
            datos.addElement(new Paragraph("Fecha de validación DIAN: " + f.getFechaRespuesta().format(F_FECHA_HORA),
                    FontFactory.getFont(FontFactory.HELVETICA, 7, GRIS_TEXTO)));
        }

        datos.addElement(new Paragraph("Datos QR: " + cufeService.construirDatosQr(f),
                FontFactory.getFont(FontFactory.HELVETICA, 6, GRIS_TEXTO)));

        tabla.addCell(datos);
        return tabla;
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    private Image cargarLogo() {
        try {
            ClassPathResource recurso = new ClassPathResource("static/images/logo negro.png");
            if (!recurso.exists()) {
                return null;
            }
            try (InputStream is = recurso.getInputStream()) {
                return Image.getInstance(is.readAllBytes());
            }
        } catch (Exception e) {
            return null;
        }
    }

    private PdfPCell caja(String titulo) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(new Color(220, 224, 232));
        c.setPadding(8);
        c.setBackgroundColor(Color.WHITE);

        Paragraph t = new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, AZUL));
        t.setSpacingAfter(4);
        c.addElement(t);
        return c;
    }

    private Paragraph linea(String texto, float tamano, boolean negrita) {
        return new Paragraph(texto,
                FontFactory.getFont(negrita ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, tamano, GRIS_TEXTO));
    }

    private PdfPCell celda(String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA, 8)));
        c.setPadding(5);
        c.setHorizontalAlignment(alineacion);
        return c;
    }

    private PdfPCell totalEtiqueta(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTO)));
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(new Color(230, 233, 239));
        c.setPadding(5);
        return c;
    }

    private PdfPCell totalValor(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, AZUL)));
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(new Color(230, 233, 239));
        c.setPadding(5);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private Paragraph espacio(float alto) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(alto);
        return p;
    }

    private String estadoTexto(FacturaElectronica f) {
        String estado = f.getEstado() != null ? f.getEstado() : "BORRADOR";
        if (Boolean.TRUE.equals(f.getSimulado())) {
            return "Estado: " + estado + " (simulación)";
        }
        return "Estado DIAN: " + estado;
    }

    private String tipoDocumentoTexto(String codigo) {
        if (codigo == null) {
            return "Documento";
        }
        switch (codigo) {
            case "11": return "Registro civil";
            case "12": return "Tarjeta de identidad";
            case "13": return "Cédula de ciudadanía";
            case "21": return "Tarjeta de extranjería";
            case "22": return "Cédula de extranjería";
            case "31": return "NIT";
            case "41": return "Pasaporte";
            case "42": return "Documento de identificación extranjero";
            default: return "Documento";
        }
    }

    private String fecha(java.time.LocalDate fecha) {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
    }

    private String moneda(BigDecimal valor) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "CO"));
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        DecimalFormat formato = new DecimalFormat("#,##0.00", simbolos);
        return "$ " + formato.format(v.setScale(2, RoundingMode.HALF_UP));
    }

    private String numero(BigDecimal valor) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        return v.stripTrailingZeros().toPlainString();
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}
