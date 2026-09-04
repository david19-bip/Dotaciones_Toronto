package com.dotacionestoronto.dotacionestoronto.service;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera el reporte de usuarios en formato Excel (.xlsx) que se descarga
 * desde el panel de administración
 * (endpoint {@code GET /admin/usuarios/reporte/excel}).
 * Usa Apache POI (poi-ooxml) para construir el libro y la hoja de cálculo.
 */
@Service
public class ReporteUsuariosExcelService {

    // Misma paleta ejecutiva que el reporte PDF: navy profundo + dorado
    private static final Color NAVY_OSCURO   = new Color(0x1A, 0x1A, 0x2E);
    private static final Color NAVY_MEDIO    = new Color(0x16, 0x21, 0x3E);
    private static final Color NAVY_PROFUNDO = new Color(0x0F, 0x34, 0x60);
    private static final Color DORADO        = new Color(0xE2, 0xB9, 0x6F);
    private static final Color GRIS_TEXTO    = new Color(0xCB, 0xD5, 0xE0);
    private static final Color GRIS_TEXTO_OSC = new Color(0x47, 0x55, 0x69);
    private static final Color GRIS_CLARO    = new Color(0xF4, 0xF6, 0xFB);
    private static final Color BLANCO        = Color.WHITE;

    // Chips de rol/estado (mismos tonos que los badges de Bootstrap)
    private static final Color ROL_ADMIN     = new Color(0xDC, 0x35, 0x45);
    private static final Color ROL_CLIENTE   = new Color(0x0D, 0x6E, 0xFD);
    private static final Color ROL_VENDEDOR  = new Color(0x19, 0x87, 0x54);
    private static final Color ROL_BODEGUERO = new Color(0xB4, 0x8A, 0x00);
    private static final Color ESTADO_OK     = new Color(0x19, 0x87, 0x54);
    private static final Color ESTADO_OFF    = new Color(0x6C, 0x75, 0x7D);

    private static final int COL_COUNT = 7; // ID, NOMBRE, DOCUMENTO, CORREO, TELEFONO, ROL, ESTADO

    // Se prueban varias rutas típicas por si el archivo tiene espacio o guion bajo
    private static final String[] RUTAS_LOGO = {
            "/static/images/logo blanco.png",
            "/static/images/logo_blanco.png",
            "/images/logo blanco.png",
            "/images/logo_blanco.png"
    };

    /**
     * Construye el archivo Excel con la tabla de usuarios recibida.
     *
     * @param usuarios listado de usuarios a incluir en el reporte
     * @return el contenido del archivo .xlsx ya generado, listo para
     *         enviarse como adjunto de descarga
     */
    public byte[] generarReporte(List<Usuario> usuarios) {
        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            XSSFSheet hoja = libro.createSheet("Usuarios");
            hoja.setDisplayGridlines(false);
            hoja.setZoom(105);

            int fila = 0;
            fila = agregarEncabezado(libro, hoja, fila, usuarios.size());
            fila = agregarFranjaDorada(libro, hoja, fila);
            fila++; // espacio
            fila = agregarResumen(libro, hoja, fila, usuarios);
            fila++; // espacio
            agregarTabla(libro, hoja, fila, usuarios);

            ajustarAnchosColumnas(hoja);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte Excel", e);
        }
    }

    // ==========================================
    // ENCABEZADO — logo + bloque título navy + bloque contador dorado
    // ==========================================
    private int agregarEncabezado(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio, int totalUsuarios) {
        int filas = 3; // 0,1,2 relativas

        for (int i = 0; i < filas; i++) {
            hoja.createRow(filaInicio + i).setHeightInPoints(i == 1 ? 30f : 16f);
        }

        // Celda contenedora del logo (columna 0), fondo navy, sin texto
        hoja.addMergedRegion(new CellRangeAddress(filaInicio, filaInicio + filas - 1, 0, 0));
        Cell celdaLogo = hoja.getRow(filaInicio).createCell(0);
        celdaLogo.setCellStyle(estiloFondo(libro, NAVY_OSCURO));
        insertarLogo(libro, hoja, filaInicio, filas);

        // Bloque título (columnas 1-4)
        hoja.addMergedRegion(new CellRangeAddress(filaInicio, filaInicio + filas - 1, 1, 4));
        Cell celdaTitulo = hoja.getRow(filaInicio).createCell(1);
        celdaTitulo.setCellValue(textoRico(libro,
                new String[]{"DOTACIONES TORONTO\n",
                        "Reporte de usuarios del sistema\n",
                        "Generado el " + LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"))},
                new Font[]{
                        crearFuente(libro, 8, false, GRIS_TEXTO),
                        crearFuente(libro, 18, true, DORADO),
                        crearFuente(libro, 9, false, GRIS_TEXTO)
                }));
        celdaTitulo.setCellStyle(estiloContenedor(libro, NAVY_OSCURO, true, VerticalAlignment.CENTER));

        // Bloque contador (columnas 5-6)
        hoja.addMergedRegion(new CellRangeAddress(filaInicio, filaInicio + filas - 1, 5, 6));
        Cell celdaContador = hoja.getRow(filaInicio).createCell(5);
        celdaContador.setCellValue(textoRico(libro,
                new String[]{totalUsuarios + "\n", "USUARIOS REGISTRADOS"},
                new Font[]{
                        crearFuente(libro, 26, true, DORADO),
                        crearFuente(libro, 8, true, GRIS_TEXTO)
                }));
        CellStyle estiloContador = estiloContenedor(libro, NAVY_PROFUNDO, true, VerticalAlignment.CENTER);
        estiloContador.setAlignment(HorizontalAlignment.CENTER);
        celdaContador.setCellStyle(estiloContador);

        return filaInicio + filas;
    }

    /**
     * Inserta el logo de la empresa dentro de la caja formada por la columna 0
     * y las filas del banner. Si el recurso no existe, no rompe el reporte.
     */
    private void insertarLogo(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio, int filas) {
        byte[] logoBytes = cargarLogo();
        if (logoBytes == null) {
            return;
        }
        try {
            int indiceImagen = libro.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
            XSSFDrawing dibujo = hoja.createDrawingPatriarch();
            XSSFClientAnchor ancla = new XSSFClientAnchor();
            ancla.setCol1(0);
            ancla.setRow1(filaInicio);
            ancla.setCol2(1);
            ancla.setRow2(filaInicio + filas);
            ancla.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            dibujo.createPicture(ancla, indiceImagen);
        } catch (Exception ignored) {
            // Si algo falla insertando la imagen, el reporte se sigue generando sin logo
        }
    }

    private byte[] cargarLogo() {
        for (String ruta : RUTAS_LOGO) {
            try (InputStream is = getClass().getResourceAsStream(ruta)) {
                if (is != null) {
                    return is.readAllBytes();
                }
            } catch (IOException ignored) {
                // probamos la siguiente ruta
            }
        }
        System.err.println("[ReporteUsuariosExcelService] No se encontró el logo en ninguna ruta conocida.");
        return null;
    }

    private int agregarFranjaDorada(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio) {
        Row fila = hoja.createRow(filaInicio);
        fila.setHeightInPoints(4f);
        CellStyle estilo = estiloFondo(libro, DORADO);
        for (int c = 0; c < COL_COUNT; c++) {
            fila.createCell(c).setCellStyle(estilo);
        }
        return filaInicio + 1;
    }

    // ==========================================
    // RESUMEN — 4 tarjetas estadísticas
    // ==========================================
    private int agregarResumen(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio, List<Usuario> usuarios) {
        long habilitados = usuarios.stream().filter(u -> "Habilitado".equalsIgnoreCase(u.getEstado())).count();
        long inhabilitados = usuarios.size() - habilitados;
        long clientes = usuarios.stream().filter(u -> u.getIdRol() != null && u.getIdRol() == 2).count();
        long staff = usuarios.size() - clientes;

        hoja.createRow(filaInicio).setHeightInPoints(3f);
        agregarTarjetaStat(libro, hoja, filaInicio, 0, 1, String.valueOf(habilitados), "HABILITADOS", ESTADO_OK);
        agregarTarjetaStat(libro, hoja, filaInicio, 2, 2, String.valueOf(inhabilitados), "INHABILITADOS", ESTADO_OFF);
        agregarTarjetaStat(libro, hoja, filaInicio, 3, 4, String.valueOf(clientes), "CLIENTES", ROL_CLIENTE);
        agregarTarjetaStat(libro, hoja, filaInicio, 5, 6, String.valueOf(staff), "PERSONAL INTERNO", NAVY_PROFUNDO);

        return filaInicio + 2;
    }

    private void agregarTarjetaStat(XSSFWorkbook libro, XSSFSheet hoja, int filaAccent,
                                     int colInicio, int colFin, String numero, String etiqueta, Color acento) {
        if (colFin > colInicio) {
            hoja.addMergedRegion(new CellRangeAddress(filaAccent, filaAccent, colInicio, colFin));
        }
        hoja.getRow(filaAccent).createCell(colInicio).setCellStyle(estiloFondo(libro, acento));

        int filaCuerpo = filaAccent + 1;
        if (hoja.getRow(filaCuerpo) == null) {
            hoja.createRow(filaCuerpo).setHeightInPoints(34f);
        }
        if (colFin > colInicio) {
            hoja.addMergedRegion(new CellRangeAddress(filaCuerpo, filaCuerpo, colInicio, colFin));
        }
        Cell celda = hoja.getRow(filaCuerpo).createCell(colInicio);
        celda.setCellValue(textoRico(libro,
                new String[]{numero + "\n", etiqueta},
                new Font[]{
                        crearFuente(libro, 15, true, NAVY_PROFUNDO),
                        crearFuente(libro, 8, true, GRIS_TEXTO_OSC)
                }));
        CellStyle estilo = estiloContenedor(libro, GRIS_CLARO, false, VerticalAlignment.CENTER);
        celda.setCellStyle(estilo);
    }

    // ==========================================
    // TABLA PRINCIPAL
    // ==========================================
    private void agregarTabla(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio, List<Usuario> usuarios) {
        String[] encabezados = {"ID", "NOMBRE", "DOCUMENTO", "CORREO", "TELÉFONO", "ROL", "ESTADO"};

        Row filaHeader = hoja.createRow(filaInicio);
        filaHeader.setHeightInPoints(20f);
        CellStyle estiloHeader = estiloContenedor(libro, NAVY_MEDIO, true, VerticalAlignment.CENTER);
        Font fuenteHeader = crearFuente(libro, 10, true, DORADO);
        estiloHeader.setFont(fuenteHeader);
        for (int c = 0; c < encabezados.length; c++) {
            Cell celda = filaHeader.createCell(c);
            celda.setCellValue(encabezados[c]);
            celda.setCellStyle(estiloHeader);
        }

        int filaActual = filaInicio + 1;
        boolean par = false;
        for (Usuario u : usuarios) {
            Color fondoFila = par ? GRIS_CLARO : BLANCO;
            Row fila = hoja.createRow(filaActual);
            fila.setHeightInPoints(18f);

            celdaTexto(libro, fila, 0, "#" + u.getId(), fondoFila);
            celdaTexto(libro, fila, 1, u.getNombre(), fondoFila);
            celdaTexto(libro, fila, 2, u.getDocumento(), fondoFila);
            celdaTexto(libro, fila, 3, u.getCorreo(), fondoFila);
            celdaTexto(libro, fila, 4, u.getTelefono(), fondoFila);
            celdaChip(libro, fila, 5, nombreRol(u.getIdRol()), colorRol(u.getIdRol()));
            celdaChip(libro, fila, 6, u.getEstado(),
                    "Habilitado".equalsIgnoreCase(u.getEstado()) ? ESTADO_OK : ESTADO_OFF);

            par = !par;
            filaActual++;
        }

        if (usuarios.isEmpty()) {
            Row filaVacia = hoja.createRow(filaActual);
            hoja.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, COL_COUNT - 1));
            Cell celda = filaVacia.createCell(0);
            celda.setCellValue("No hay usuarios registrados.");
            CellStyle estilo = estiloContenedor(libro, BLANCO, false, VerticalAlignment.CENTER);
            estilo.setAlignment(HorizontalAlignment.CENTER);
            celda.setCellStyle(estilo);
            filaActual++;
        }

        hoja.setAutoFilter(new CellRangeAddress(filaInicio, filaActual - 1, 0, COL_COUNT - 1));
        hoja.createFreezePane(0, filaInicio + 1);
    }

    private void celdaTexto(XSSFWorkbook libro, Row fila, int col, String texto, Color fondo) {
        Cell celda = fila.createCell(col);
        celda.setCellValue(texto != null ? texto : "-");
        CellStyle estilo = estiloContenedor(libro, fondo, false, VerticalAlignment.CENTER);
        estilo.setFont(crearFuente(libro, 10, false, new Color(0x1E, 0x29, 0x3B)));
        celda.setCellStyle(estilo);
    }

    private void celdaChip(XSSFWorkbook libro, Row fila, int col, String texto, Color colorChip) {
        Cell celda = fila.createCell(col);
        celda.setCellValue(texto != null ? texto.toUpperCase() : "-");
        CellStyle estilo = estiloContenedor(libro, colorChip, false, VerticalAlignment.CENTER);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setFont(crearFuente(libro, 9, true, BLANCO));
        celda.setCellStyle(estilo);
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
    // Utilidades de estilo
    // ==========================================
    private Font crearFuente(XSSFWorkbook libro, int tamano, boolean negrita, Color color) {
        XSSFFont fuente = libro.createFont();
        fuente.setFontHeightInPoints((short) tamano);
        fuente.setBold(negrita);
        fuente.setColor(toXssfColor(color));
        return fuente;
    }

    private CellStyle estiloFondo(XSSFWorkbook libro, Color color) {
        XSSFCellStyle estilo = libro.createCellStyle();
        estilo.setFillForegroundColor(toXssfColor(color));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle estiloContenedor(XSSFWorkbook libro, Color fondo, boolean ajustarTexto,
                                        VerticalAlignment vAlign) {
        XSSFCellStyle estilo = (XSSFCellStyle) estiloFondo(libro, fondo);
        estilo.setVerticalAlignment(vAlign);
        estilo.setWrapText(ajustarTexto);
        estilo.setIndention((short) 1);
        return estilo;
    }

    private XSSFColor toXssfColor(Color color) {
        return new XSSFColor(new byte[]{
                (byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()
        }, null);
    }

    private RichTextString textoRico(XSSFWorkbook libro, String[] segmentos, Font[] fuentes) {
        StringBuilder sb = new StringBuilder();
        List<int[]> rangos = new ArrayList<>();
        for (String segmento : segmentos) {
            int inicio = sb.length();
            sb.append(segmento);
            rangos.add(new int[]{inicio, sb.length()});
        }
        XSSFRichTextString texto = new XSSFRichTextString(sb.toString());
        for (int i = 0; i < segmentos.length; i++) {
            texto.applyFont(rangos.get(i)[0], rangos.get(i)[1], fuentes[i]);
        }
        return texto;
    }

    private void ajustarAnchosColumnas(XSSFSheet hoja) {
        int[] anchos = {3400, 6800, 4200, 8200, 4200, 3600, 4200}; // en unidades de 1/256 char
        for (int c = 0; c < anchos.length; c++) {
            hoja.setColumnWidth(c, anchos[c]);
        }
    }
}