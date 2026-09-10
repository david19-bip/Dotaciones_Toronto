package com.dotacionestoronto.dotacionestoronto.service;

import com.dotacionestoronto.dotacionestoronto.entity.DetalleVenta;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import com.dotacionestoronto.dotacionestoronto.repository.DetalleVentaRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteVentasExcelService {

    private static final Color NAVY_OSCURO = new Color(0x1A, 0x1A, 0x2E);
    private static final Color NAVY_MEDIO = new Color(0x16, 0x21, 0x3E);
    private static final Color NAVY_PROFUNDO = new Color(0x0F, 0x34, 0x60);
    private static final Color DORADO = new Color(0xE2, 0xB9, 0x6F);
    private static final Color GRIS_TEXTO = new Color(0xCB, 0xD5, 0xE0);
    private static final Color GRIS_TEXTO_OSC = new Color(0x47, 0x55, 0x69);
    private static final Color GRIS_CLARO = new Color(0xF4, 0xF6, 0xFB);
    private static final Color BLANCO = Color.WHITE;
    private static final Color VERDE = new Color(0x19, 0x87, 0x54);
    private static final int COL_COUNT = 6;

    private final UsuarioRepository usuarioRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public ReporteVentasExcelService(UsuarioRepository usuarioRepository,
                                      DetalleVentaRepository detalleVentaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    public byte[] generarReporte(List<Venta> ventas) {
        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            XSSFSheet hoja = libro.createSheet("Ventas");
            hoja.setDisplayGridlines(false);
            hoja.setZoom(105);

            int fila = agregarEncabezado(libro, hoja, ventas.size());
            fila = agregarFranjaDorada(libro, hoja, fila);
            fila++;
            fila = agregarResumen(libro, hoja, fila, ventas);
            fila++;
            agregarTabla(libro, hoja, fila, ventas);
            ajustarAnchos(hoja);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el reporte Excel de ventas", e);
        }
    }

    private int agregarEncabezado(XSSFWorkbook libro, XSSFSheet hoja, int totalVentas) {
        for (int i = 0; i < 3; i++) {
            hoja.createRow(i).setHeightInPoints(i == 1 ? 30f : 16f);
        }
        hoja.addMergedRegion(new CellRangeAddress(0, 2, 0, 0));
        hoja.getRow(0).createCell(0).setCellStyle(estiloFondo(libro, NAVY_OSCURO));
        insertarLogo(libro, hoja);

        hoja.addMergedRegion(new CellRangeAddress(0, 2, 1, 3));
        Cell titulo = hoja.getRow(0).createCell(1);
        titulo.setCellValue(textoRico(libro,
                new String[]{"DOTACIONES TORONTO\n", "Reporte de ventas\n",
                        "Generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"))},
                new Font[]{crearFuente(libro, 8, false, GRIS_TEXTO), crearFuente(libro, 18, true, DORADO),
                        crearFuente(libro, 9, false, GRIS_TEXTO)}));
        titulo.setCellStyle(estiloContenedor(libro, NAVY_OSCURO, true, VerticalAlignment.CENTER));

        hoja.addMergedRegion(new CellRangeAddress(0, 2, 4, 5));
        Cell contador = hoja.getRow(0).createCell(4);
        contador.setCellValue(textoRico(libro,
                new String[]{totalVentas + "\n", "VENTAS REGISTRADAS"},
                new Font[]{crearFuente(libro, 26, true, DORADO), crearFuente(libro, 8, true, GRIS_TEXTO)}));
        CellStyle estiloContador = estiloContenedor(libro, NAVY_PROFUNDO, true, VerticalAlignment.CENTER);
        estiloContador.setAlignment(HorizontalAlignment.CENTER);
        contador.setCellStyle(estiloContador);
        return 3;
    }

    private void insertarLogo(XSSFWorkbook libro, XSSFSheet hoja) {
        String[] rutas = {"/static/images/logo blanco.png", "/static/images/logo_blanco.png", "/images/logo blanco.png"};
        for (String ruta : rutas) {
            try (InputStream entrada = getClass().getResourceAsStream(ruta)) {
                if (entrada == null) continue;
                int imagen = libro.addPicture(entrada.readAllBytes(), Workbook.PICTURE_TYPE_PNG);
                XSSFDrawing dibujo = hoja.createDrawingPatriarch();
                XSSFClientAnchor ancla = new XSSFClientAnchor(0, 0, 0, 0, 0, 0, 1, 3);
                ancla.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                dibujo.createPicture(ancla, imagen);
                return;
            } catch (IOException ignored) {
                return;
            }
        }
    }

    private int agregarFranjaDorada(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio) {
        Row fila = hoja.createRow(filaInicio);
        fila.setHeightInPoints(4f);
        CellStyle estilo = estiloFondo(libro, DORADO);
        for (int columna = 0; columna < COL_COUNT; columna++) fila.createCell(columna).setCellStyle(estilo);
        return filaInicio + 1;
    }

    private int agregarResumen(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio, List<Venta> ventas) {
        BigDecimal total = ventas.stream().map(this::totalVenta).reduce(BigDecimal.ZERO, BigDecimal::add);
        long productos = ventas.stream().flatMap(v -> detalleVentaRepository.findByIdVenta(v.getId()).stream())
                .mapToLong(d -> d.getCantidad() == null ? 0 : d.getCantidad()).sum();
        hoja.createRow(filaInicio).setHeightInPoints(3f);
        agregarTarjeta(libro, hoja, filaInicio, 0, 1, String.valueOf(ventas.size()), "VENTAS", NAVY_PROFUNDO);
        agregarTarjeta(libro, hoja, filaInicio, 2, 3, String.valueOf(productos), "PRODUCTOS VENDIDOS", VERDE);
        agregarTarjeta(libro, hoja, filaInicio, 4, 5, "$" + formatearNumero(total), "TOTAL VENDIDO", NAVY_MEDIO);
        return filaInicio + 2;
    }

    private void agregarTarjeta(XSSFWorkbook libro, XSSFSheet hoja, int fila, int inicio, int fin,
                                String numero, String etiqueta, Color acento) {
        hoja.addMergedRegion(new CellRangeAddress(fila, fila, inicio, fin));
        hoja.getRow(fila).createCell(inicio).setCellStyle(estiloFondo(libro, acento));
        int filaCuerpo = fila + 1;
        if (hoja.getRow(filaCuerpo) == null) hoja.createRow(filaCuerpo).setHeightInPoints(34f);
        hoja.addMergedRegion(new CellRangeAddress(filaCuerpo, filaCuerpo, inicio, fin));
        Cell celda = hoja.getRow(filaCuerpo).createCell(inicio);
        celda.setCellValue(textoRico(libro, new String[]{numero + "\n", etiqueta},
                new Font[]{crearFuente(libro, 15, true, NAVY_PROFUNDO), crearFuente(libro, 8, true, GRIS_TEXTO_OSC)}));
        celda.setCellStyle(estiloContenedor(libro, GRIS_CLARO, false, VerticalAlignment.CENTER));
    }

    private void agregarTabla(XSSFWorkbook libro, XSSFSheet hoja, int filaInicio, List<Venta> ventas) {
        String[] encabezados = {"ID VENTA", "ID PEDIDO", "CLIENTE", "FECHA Y HORA", "PRODUCTOS", "TOTAL"};
        Row encabezado = hoja.createRow(filaInicio);
        encabezado.setHeightInPoints(20f);
        CellStyle estiloHeader = estiloContenedor(libro, NAVY_MEDIO, true, VerticalAlignment.CENTER);
        estiloHeader.setFont(crearFuente(libro, 10, true, DORADO));
        for (int columna = 0; columna < encabezados.length; columna++) {
            Cell celda = encabezado.createCell(columna);
            celda.setCellValue(encabezados[columna]);
            celda.setCellStyle(estiloHeader);
        }

        int filaActual = filaInicio + 1;
        boolean par = false;
        for (Venta venta : ventas) {
            List<DetalleVenta> detalles = detalleVentaRepository.findByIdVenta(venta.getId());
            String cliente = usuarioRepository.findById(venta.getIdUsuario()).map(Usuario::getNombre).orElse("Cliente eliminado");
            Color fondo = par ? GRIS_CLARO : BLANCO;
            Row fila = hoja.createRow(filaActual++);
            fila.setHeightInPoints(18f);
            celdaTexto(libro, fila, 0, "#" + venta.getId(), fondo);
            celdaTexto(libro, fila, 1, "#" + venta.getIdPedido(), fondo);
            celdaTexto(libro, fila, 2, cliente, fondo);
            celdaTexto(libro, fila, 3, venta.getFechaVenta() == null ? "-" : venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fondo);
            celdaTexto(libro, fila, 4, String.valueOf(detalles.stream().mapToInt(d -> d.getCantidad() == null ? 0 : d.getCantidad()).sum()), fondo);
            celdaTexto(libro, fila, 5, "$" + formatearNumero(totalVenta(venta)), fondo);
            par = !par;
        }
        if (ventas.isEmpty()) {
            hoja.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, COL_COUNT - 1));
            Cell celda = hoja.createRow(filaActual).createCell(0);
            celda.setCellValue("No hay ventas registradas.");
            celda.setCellStyle(estiloContenedor(libro, BLANCO, false, VerticalAlignment.CENTER));
            filaActual++;
        }
        hoja.setAutoFilter(new CellRangeAddress(filaInicio, filaActual - 1, 0, COL_COUNT - 1));
        hoja.createFreezePane(0, filaInicio + 1);
    }

    private BigDecimal totalVenta(Venta venta) {
        return detalleVentaRepository.findByIdVenta(venta.getId()).stream()
                .filter(d -> d.getPrecioUnitario() != null && d.getCantidad() != null)
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void celdaTexto(XSSFWorkbook libro, Row fila, int columna, String texto, Color fondo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(texto == null ? "-" : texto);
        CellStyle estilo = estiloContenedor(libro, fondo, false, VerticalAlignment.CENTER);
        estilo.setFont(crearFuente(libro, 10, false, new Color(0x1E, 0x29, 0x3B)));
        celda.setCellStyle(estilo);
    }

    private String formatearNumero(BigDecimal valor) {
        return String.format("%,.0f", valor == null ? BigDecimal.ZERO : valor);
    }

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

    private CellStyle estiloContenedor(XSSFWorkbook libro, Color fondo, boolean ajustarTexto, VerticalAlignment alineacion) {
        XSSFCellStyle estilo = (XSSFCellStyle) estiloFondo(libro, fondo);
        estilo.setVerticalAlignment(alineacion);
        estilo.setWrapText(ajustarTexto);
        estilo.setIndention((short) 1);
        return estilo;
    }

    private XSSFColor toXssfColor(Color color) {
        return new XSSFColor(new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()}, null);
    }

    private RichTextString textoRico(XSSFWorkbook libro, String[] segmentos, Font[] fuentes) {
        StringBuilder texto = new StringBuilder();
        int[] inicios = new int[segmentos.length];
        for (int i = 0; i < segmentos.length; i++) {
            inicios[i] = texto.length();
            texto.append(segmentos[i]);
        }
        XSSFRichTextString resultado = new XSSFRichTextString(texto.toString());
        for (int i = 0; i < segmentos.length; i++) resultado.applyFont(inicios[i], inicios[i] + segmentos[i].length(), fuentes[i]);
        return resultado;
    }

    private void ajustarAnchos(XSSFSheet hoja) {
        int[] anchos = {3000, 3200, 8500, 5000, 3600, 4800};
        for (int columna = 0; columna < anchos.length; columna++) hoja.setColumnWidth(columna, anchos[columna]);
    }
}
