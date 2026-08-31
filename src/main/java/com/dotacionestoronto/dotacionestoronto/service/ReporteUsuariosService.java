package com.dotacionestoronto.dotacionestoronto.service;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteUsuariosService {

    // Paleta ejecutiva del panel admin: navy profundo + dorado
    private static final Color NAVY_OSCURO   = new Color(0x1A, 0x1A, 0x2E);
    private static final Color NAVY_MEDIO    = new Color(0x16, 0x21, 0x3E);
    private static final Color NAVY_PROFUNDO = new Color(0x0F, 0x34, 0x60);
    private static final Color DORADO        = new Color(0xE2, 0xB9, 0x6F);
    private static final Color DORADO_SUAVE  = new Color(0xF6, 0xEA, 0xD3);
    private static final Color GRIS_TEXTO    = new Color(0x47, 0x55, 0x69);
    private static final Color GRIS_CLARO    = new Color(0xF4, 0xF6, 0xFB);
    private static final Color BLANCO        = Color.WHITE;

    // Chips de rol (mismos tonos que los badges de Bootstrap en la vista)
    private static final Color ROL_ADMIN     = new Color(0xDC, 0x35, 0x45);
    private static final Color ROL_CLIENTE   = new Color(0x0D, 0x6E, 0xFD);
    private static final Color ROL_VENDEDOR  = new Color(0x19, 0x87, 0x54);
    private static final Color ROL_BODEGUERO = new Color(0xB4, 0x8A, 0x00);

    private static final Color ESTADO_OK   = new Color(0x19, 0x87, 0x54);
    private static final Color ESTADO_OFF  = new Color(0x6C, 0x75, 0x7D);

    private final Font fSerifTituloGrande = new Font(Font.HELVETICA, 22, Font.BOLD, DORADO);
    private final Font fSubtitulo = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0xCB, 0xD5, 0xE0));
    private final Font fEtiquetaBlanca = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(0xB8, 0xC2, 0xD6));
    private final Font fStatNumero = new Font(Font.HELVETICA, 20, Font.BOLD, NAVY_PROFUNDO);
    private final Font fStatLabel = new Font(Font.HELVETICA, 8, Font.BOLD, GRIS_TEXTO);
    private final Font fTablaHeader = new Font(Font.HELVETICA, 9, Font.BOLD, DORADO);
    private final Font fTablaCelda = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(0x1E, 0x29, 0x3B));
    private final Font fChip = new Font(Font.HELVETICA, 8, Font.BOLD, BLANCO);
    private final Font fFooter = new Font(Font.HELVETICA, 7, Font.NORMAL, GRIS_TEXTO);

    public byte[] generarReporte(List<Usuario> usuarios) {
        Document documento = new Document(PageSize.A4, 28, 28, 0, 40);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();

            agregarEncabezado(documento, usuarios.size());
            agregarResumen(documento, usuarios);
            agregarTabla(documento, usuarios);

            documento.close();
        } catch (DocumentException e) {
            throw new RuntimeException("No se pudo generar el reporte PDF", e);
        }

        return salida.toByteArray();
    }

    // ==========================================
    // ENCABEZADO — banda navy con degradado y franja dorada
    // ==========================================
    private void agregarEncabezado(Document documento, int totalUsuarios) throws DocumentException {
        PdfPTable banda = new PdfPTable(2);
        banda.setWidthPercentage(100);
        try {
            banda.setWidths(new float[]{3f, 1f});
        } catch (DocumentException ignored) { }
        banda.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaTitulo = new PdfPCell();
        celdaTitulo.setBorder(Rectangle.NO_BORDER);
        celdaTitulo.setBackgroundColor(NAVY_OSCURO);
        celdaTitulo.setPadding(18);
        celdaTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph marca = new Paragraph("DOTACIONES TORONTO", fEtiquetaBlanca);
        marca.setSpacingAfter(4);
        Paragraph titulo = new Paragraph("Reporte de usuarios del sistema", fSerifTituloGrande);
        titulo.setSpacingAfter(4);
        Paragraph fecha = new Paragraph(
                "Generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")),
                fSubtitulo);

        celdaTitulo.addElement(marca);
        celdaTitulo.addElement(titulo);
        celdaTitulo.addElement(fecha);

        PdfPCell celdaContador = new PdfPCell();
        celdaContador.setBorder(Rectangle.NO_BORDER);
        celdaContador.setBackgroundColor(NAVY_PROFUNDO);
        celdaContador.setPadding(18);
        celdaContador.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaContador.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph numGrande = new Paragraph(String.valueOf(totalUsuarios),
                new Font(Font.HELVETICA, 34, Font.BOLD, DORADO));
        numGrande.setAlignment(Element.ALIGN_CENTER);
        Paragraph etiquetaNum = new Paragraph("USUARIOS REGISTRADOS",
                new Font(Font.HELVETICA, 7, Font.BOLD, new Color(0xCB, 0xD5, 0xE0)));
        etiquetaNum.setAlignment(Element.ALIGN_CENTER);

        celdaContador.addElement(numGrande);
        celdaContador.addElement(etiquetaNum);

        banda.addCell(celdaTitulo);
        banda.addCell(celdaContador);
        documento.add(banda);

        // Franja dorada de firma
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

    // ==========================================
    // RESUMEN — tarjetas estadísticas
    // ==========================================
    private void agregarResumen(Document documento, List<Usuario> usuarios) throws DocumentException {
        long habilitados = usuarios.stream().filter(u -> "Habilitado".equalsIgnoreCase(u.getEstado())).count();
        long inhabilitados = usuarios.size() - habilitados;
        long clientes = usuarios.stream().filter(u -> u.getIdRol() != null && u.getIdRol() == 2).count();
        long staff = usuarios.size() - clientes;

        PdfPTable fila = new PdfPTable(4);
        fila.setWidthPercentage(100);
        fila.setSpacingAfter(18);

        agregarTarjetaStat(fila, String.valueOf(habilitados), "HABILITADOS", ESTADO_OK);
        agregarTarjetaStat(fila, String.valueOf(inhabilitados), "INHABILITADOS", ESTADO_OFF);
        agregarTarjetaStat(fila, String.valueOf(clientes), "CLIENTES", ROL_CLIENTE);
        agregarTarjetaStat(fila, String.valueOf(staff), "PERSONAL INTERNO", NAVY_PROFUNDO);

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
    private void agregarTabla(Document documento, List<Usuario> usuarios) throws DocumentException {
        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.6f, 2f, 1.6f, 2.4f, 1.4f, 1.3f});

        agregarCeldaHeader(tabla, "ID");
        agregarCeldaHeader(tabla, "NOMBRE");
        agregarCeldaHeader(tabla, "DOCUMENTO");
        agregarCeldaHeader(tabla, "CORREO");
        agregarCeldaHeader(tabla, "ROL");
        agregarCeldaHeader(tabla, "ESTADO");
        tabla.setHeaderRows(1);

        boolean par = false;
        for (Usuario u : usuarios) {
            Color fondoFila = par ? GRIS_CLARO : BLANCO;

            agregarCeldaTexto(tabla, "#" + u.getId(), fondoFila);
            agregarCeldaTexto(tabla, u.getNombre(), fondoFila);
            agregarCeldaTexto(tabla, u.getDocumento(), fondoFila);
            agregarCeldaTexto(tabla, u.getCorreo(), fondoFila);
            agregarCeldaChip(tabla, nombreRol(u.getIdRol()), colorRol(u.getIdRol()), fondoFila);
            agregarCeldaChip(tabla, u.getEstado(),
                    "Habilitado".equalsIgnoreCase(u.getEstado()) ? ESTADO_OK : ESTADO_OFF, fondoFila);

            par = !par;
        }

        if (usuarios.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("No hay usuarios registrados.", fTablaCelda));
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

    private String nombreRol(Integer idRol) {
        if (idRol == null) return "-";
        return switch (idRol) {
            case 1 -> "Admin";
            case 2 -> "Cliente";
            case 3 -> "Vendedor";
            default -> "Bodeguero";
        };
    }

    private Color colorRol(Integer idRol) {
        if (idRol == null) return ROL_BODEGUERO;
        return switch (idRol) {
            case 1 -> ROL_ADMIN;
            case 2 -> ROL_CLIENTE;
            case 3 -> ROL_VENDEDOR;
            default -> ROL_BODEGUERO;
        };
    }

    // ==========================================
    // Pie de página con numeración y franja dorada
    // ==========================================
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            PdfContentByte cb = writer.getDirectContent();

            cb.setColorFill(DORADO);
            cb.rectangle(0, 30, documento.getPageSize().getWidth(), 2);
            cb.fill();

            Phrase pie = new Phrase(
                    "Dotaciones Toronto · Documento generado automáticamente · Página " + writer.getPageNumber(),
                    new Font(Font.HELVETICA, 7, Font.NORMAL, GRIS_TEXTO));
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pie,
                    documento.getPageSize().getWidth() / 2, 16, 0);
        }
    }
}
