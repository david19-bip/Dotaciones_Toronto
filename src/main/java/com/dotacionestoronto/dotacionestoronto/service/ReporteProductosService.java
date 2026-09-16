package com.dotacionestoronto.dotacionestoronto.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReporteProductosService {

    // ==========================================
    // Datos de contacto de la empresa
    // ==========================================
    private static final String EMPRESA_NOMBRE    = "DOTACIONES TORONTO";
    private static final String EMPRESA_CORREO    = "dotaciones.elobrero@gmail.com";
    private static final String EMPRESA_TELEFONO  = "+57 321 209 9989";
    private static final String EMPRESA_DIRECCION = "Carrera 63 Sur #21-12, Bogotá D.C., Colombia";

    // Misma paleta ejecutiva del panel admin: navy profundo + dorado
    private static final Color NAVY_OSCURO   = new Color(0x1A, 0x1A, 0x2E);
    private static final Color NAVY_MEDIO    = new Color(0x16, 0x21, 0x3E);
    private static final Color NAVY_PROFUNDO = new Color(0x0F, 0x34, 0x60);
    private static final Color DORADO        = new Color(0xE2, 0xB9, 0x6F);
    private static final Color GRIS_TEXTO    = new Color(0x47, 0x55, 0x69);
    private static final Color GRIS_CLARO    = new Color(0xF4, 0xF6, 0xFB);
    private static final Color BLANCO        = Color.WHITE;

    private static final Color ESTADO_OK   = new Color(0x19, 0x87, 0x54); // Disponible
    private static final Color ESTADO_OFF  = new Color(0xDC, 0x35, 0x45); // Agotado
    private static final Color TALLA_CHIP  = new Color(0x0F, 0x34, 0x60);

    private static final String[] RUTAS_LOGO = {
            "/static/images/logo blanco.png",
            "/static/images/logo_blanco.png",
            "/images/logo blanco.png",
            "/images/logo_blanco.png"
    };

    private final Font fSerifTituloGrande = new Font(Font.HELVETICA, 22, Font.BOLD, DORADO);
    private final Font fSubtitulo = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0xCB, 0xD5, 0xE0));
    private final Font fEtiquetaBlanca = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(0xB8, 0xC2, 0xD6));
    private final Font fContactoBlanco = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(0xCB, 0xD5, 0xE0));
    private final Font fStatNumero = new Font(Font.HELVETICA, 20, Font.BOLD, NAVY_PROFUNDO);
    private final Font fStatLabel = new Font(Font.HELVETICA, 8, Font.BOLD, GRIS_TEXTO);
    private final Font fTablaHeader = new Font(Font.HELVETICA, 9, Font.BOLD, DORADO);
    private final Font fTablaCelda = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(0x1E, 0x29, 0x3B));
    private final Font fChip = new Font(Font.HELVETICA, 8, Font.BOLD, BLANCO);
    private final Font fPiePagina = new Font(Font.HELVETICA, 7, Font.NORMAL, GRIS_TEXTO);

    public byte[] generarReporte(List<Producto> productos) {
        Document documento = new Document(PageSize.A4, 28, 28, 0, 40);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();

            agregarEncabezado(documento, productos.size());
            agregarResumen(documento, productos);
            agregarTabla(documento, productos);

            documento.close();
        } catch (DocumentException e) {
            throw new RuntimeException("No se pudo generar el reporte PDF", e);
        }

        return salida.toByteArray();
    }

    // ==========================================
    // ENCABEZADO
    // ==========================================
    private void agregarEncabezado(Document documento, int totalProductos) throws DocumentException {
        PdfPTable banda = new PdfPTable(3);
        banda.setWidthPercentage(100);
        try {
            banda.setWidths(new float[]{1f, 2.3f, 1f});
        } catch (DocumentException ignored) { }
        banda.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaLogo = new PdfPCell();
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setBackgroundColor(NAVY_OSCURO);
        celdaLogo.setPadding(10);
        celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaLogo.setHorizontalAlignment(Element.ALIGN_CENTER);

        Image logo = cargarLogo();
        if (logo != null) {
            logo.scaleToFit(95, 95);
            celdaLogo.addElement(logo);
        }

        PdfPCell celdaTitulo = new PdfPCell();
        celdaTitulo.setBorder(Rectangle.NO_BORDER);
        celdaTitulo.setBackgroundColor(NAVY_OSCURO);
        celdaTitulo.setPadding(18);
        celdaTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph marca = new Paragraph(EMPRESA_NOMBRE, fEtiquetaBlanca);
        marca.setSpacingAfter(4);
        Paragraph titulo = new Paragraph("Reporte de productos del catálogo", fSerifTituloGrande);
        titulo.setSpacingAfter(4);
        Paragraph fecha = new Paragraph(
                "Generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")),
                fSubtitulo);
        fecha.setSpacingAfter(6);

        // Datos de contacto de la empresa
        Paragraph direccion = new Paragraph(EMPRESA_DIRECCION, fContactoBlanco);
        Paragraph telefono = new Paragraph("Tel: " + EMPRESA_TELEFONO, fContactoBlanco);
        Paragraph correo = new Paragraph("Correo: " + EMPRESA_CORREO, fContactoBlanco);

        celdaTitulo.addElement(marca);
        celdaTitulo.addElement(titulo);
        celdaTitulo.addElement(fecha);
        celdaTitulo.addElement(direccion);
        celdaTitulo.addElement(telefono);
        celdaTitulo.addElement(correo);

        PdfPCell celdaContador = new PdfPCell();
        celdaContador.setBorder(Rectangle.NO_BORDER);
        celdaContador.setBackgroundColor(NAVY_PROFUNDO);
        celdaContador.setPadding(18);
        celdaContador.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaContador.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph numGrande = new Paragraph(String.valueOf(totalProductos),
                new Font(Font.HELVETICA, 34, Font.BOLD, DORADO));
        numGrande.setAlignment(Element.ALIGN_CENTER);
        Paragraph etiquetaNum = new Paragraph("PRODUCTOS REGISTRADOS",
                new Font(Font.HELVETICA, 7, Font.BOLD, new Color(0xCB, 0xD5, 0xE0)));
        etiquetaNum.setAlignment(Element.ALIGN_CENTER);

        celdaContador.addElement(numGrande);
        celdaContador.addElement(etiquetaNum);

        banda.addCell(celdaLogo);
        banda.addCell(celdaTitulo);
        banda.addCell(celdaContador);
        documento.add(banda);

        PdfPTable franja = new PdfPTable(1);
        franja.setWidthPercentage(100);
        PdfPCell celdaFranja = new PdfPCell();
        celdaFranja.setFixedHeight(5f);
        celdaFranja.setBackgroundColor(DORADO);
        celdaFranja.setBorder(Rectangle.NO_BORDER);
        franja.addCell(celdaFranja);
        documento.add(franja);

        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(14);
        documento.add(espacio);
    }

    private Image cargarLogo() {
        for (String ruta : RUTAS_LOGO) {
            try (InputStream is = getClass().getResourceAsStream(ruta)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    return Image.getInstance(bytes);
                }
            } catch (IOException | com.lowagie.text.BadElementException ignored) {
                // probamos la siguiente ruta
            }
        }
        System.err.println("[ReporteProductosService] No se encontró el logo en ninguna ruta conocida.");
        return null;
    }

    // ==========================================
    // RESUMEN — tarjetas estadísticas
    // ==========================================
    private void agregarResumen(Document documento, List<Producto> productos) throws DocumentException {
        long disponibles = productos.stream().filter(p -> "Disponible".equalsIgnoreCase(p.getEstado())).count();
        long agotados = productos.size() - disponibles;
        BigDecimal valorTotal = productos.stream()
                .map(Producto::getPrecio)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long tallasDistintas = productos.stream()
                .map(Producto::getTalla)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();

        PdfPTable fila = new PdfPTable(4);
        fila.setWidthPercentage(100);
        fila.setSpacingAfter(18);

        agregarTarjetaStat(fila, String.valueOf(disponibles), "DISPONIBLES", ESTADO_OK);
        agregarTarjetaStat(fila, String.valueOf(agotados), "AGOTADOS", ESTADO_OFF);
        agregarTarjetaStat(fila, String.valueOf(tallasDistintas), "TALLAS EN CATÁLOGO", TALLA_CHIP);
        agregarTarjetaStat(fila, "$" + formatearNumero(valorTotal), "VALOR TOTAL CATÁLOGO", NAVY_PROFUNDO);

        documento.add(fila);
    }

    private void agregarTarjetaStat(PdfPTable fila, String numero, String etiqueta, Color acento) {
        PdfPTable tarjeta = new PdfPTable(1);
        try {
            PdfPCell barra = new PdfPCell();
            barra.setFixedHeight(3f);
            barra.setBackgroundColor(acento);
            barra.setBorder(Rectangle.NO_BORDER);
            tarjeta.addCell(barra);

            PdfPCell cuerpo = new PdfPCell();
            cuerpo.setBackgroundColor(GRIS_CLARO);
            cuerpo.setBorder(Rectangle.NO_BORDER);
            cuerpo.setPadding(12);
            Paragraph num = new Paragraph(numero, fStatNumero);
            Paragraph etq = new Paragraph(etiqueta, fStatLabel);
            etq.setSpacingBefore(2);
            cuerpo.addElement(num);
            cuerpo.addElement(etq);
            tarjeta.addCell(cuerpo);
        } catch (Exception ignored) { }

        PdfPCell contenedor = new PdfPCell(tarjeta);
        contenedor.setBorder(Rectangle.NO_BORDER);
        contenedor.setPadding(4);
        fila.addCell(contenedor);
    }

    // ==========================================
    // TABLA PRINCIPAL
    // ==========================================
    private void agregarTabla(Document documento, List<Producto> productos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.6f, 2.4f, 1.4f, 1f, 1.6f, 1.3f});

        agregarCeldaHeader(tabla, "ID");
        agregarCeldaHeader(tabla, "NOMBRE");
        agregarCeldaHeader(tabla, "PRECIO");
        agregarCeldaHeader(tabla, "TALLA");
        agregarCeldaHeader(tabla, "COLOR");
        agregarCeldaHeader(tabla, "ESTADO");
        tabla.setHeaderRows(1);

        boolean par = false;
        for (Producto p : productos) {
            Color fondoFila = par ? GRIS_CLARO : BLANCO;

            agregarCeldaTexto(tabla, "#" + p.getId(), fondoFila);
            agregarCeldaTexto(tabla, p.getNombre(), fondoFila);
            agregarCeldaTexto(tabla, "$" + formatearNumero(p.getPrecio()), fondoFila);
            agregarCeldaChip(tabla, p.getTalla(), TALLA_CHIP, fondoFila);
            agregarCeldaTexto(tabla, p.getColor(), fondoFila);
            agregarCeldaChip(tabla, p.getEstado(),
                    "Disponible".equalsIgnoreCase(p.getEstado()) ? ESTADO_OK : ESTADO_OFF, fondoFila);

            par = !par;
        }

        if (productos.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("No hay productos registrados.", fTablaCelda));
            vacio.setColspan(6);
            vacio.setPadding(14);
            vacio.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacio.setBorderColor(new Color(0xE2, 0xE8, 0xF0));
            tabla.addCell(vacio);
        }

        documento.add(tabla);
    }

    private void agregarCeldaHeader(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fTablaHeader));
        celda.setBackgroundColor(NAVY_MEDIO);
        celda.setPadding(8);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabla.addCell(celda);
    }

    private void agregarCeldaTexto(PdfPTable tabla, String texto, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "-", fTablaCelda));
        celda.setBackgroundColor(fondo);
        celda.setPadding(7);
        celda.setBorderColor(new Color(0xE2, 0xE8, 0xF0));
        celda.setBorderWidthLeft(0);
        celda.setBorderWidthRight(0);
        celda.setBorderWidthTop(0);
        tabla.addCell(celda);
    }

    private void agregarCeldaChip(PdfPTable tabla, String texto, Color colorChip, Color fondo) {
        PdfPCell celdaFondo = new PdfPCell();
        celdaFondo.setBackgroundColor(fondo);
        celdaFondo.setPadding(6);
        celdaFondo.setBorderColor(new Color(0xE2, 0xE8, 0xF0));
        celdaFondo.setBorderWidthLeft(0);
        celdaFondo.setBorderWidthRight(0);
        celdaFondo.setBorderWidthTop(0);

        PdfPTable chip = new PdfPTable(1);
        PdfPCell interior = new PdfPCell(new Phrase(texto != null ? texto.toUpperCase() : "-", fChip));
        interior.setBackgroundColor(colorChip);
        interior.setPadding(4);
        interior.setBorder(Rectangle.NO_BORDER);
        interior.setHorizontalAlignment(Element.ALIGN_CENTER);
        chip.addCell(interior);

        celdaFondo.addElement(chip);
        tabla.addCell(celdaFondo);
    }

    private String formatearNumero(BigDecimal valor) {
        if (valor == null) return "0";
        return String.format("%,.0f", valor);
    }

    // ==========================================
    // Pie de página con numeración, datos de contacto y franja dorada
    // ==========================================
    private class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            PdfContentByte cb = writer.getDirectContent();

            cb.setColorFill(DORADO);
            cb.rectangle(0, 30, documento.getPageSize().getWidth(), 2);
            cb.fill();

            Phrase contacto = new Phrase(
                    EMPRESA_DIRECCION + "  |  " + EMPRESA_TELEFONO + "  |  " + EMPRESA_CORREO,
                    fPiePagina);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, contacto,
                    documento.getPageSize().getWidth() / 2, 22, 0);

            Phrase pie = new Phrase(
                    EMPRESA_NOMBRE + " · Documento generado automáticamente · Página " + writer.getPageNumber(),
                    fPiePagina);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pie,
                    documento.getPageSize().getWidth() / 2, 14, 0);
        }
    }
}