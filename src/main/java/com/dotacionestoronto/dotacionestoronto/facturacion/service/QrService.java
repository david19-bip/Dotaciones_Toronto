package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Genera el código QR de la factura electrónica (exigido por el artículo 11,
 * numeral 16 de la Resolución 000165 de 2023; tamaño mínimo 2 cm).
 */
@Service
public class QrService {

    public BufferedImage generarImagen(String contenido, int tamaño) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamaño, tamaño, hints);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el código QR: " + e.getMessage(), e);
        }
    }

    public byte[] generarPng(String contenido, int tamaño) {
        try {
            BufferedImage imagen = generarImagen(contenido, tamaño);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(imagen, "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el código QR", e);
        }
    }

    /** QR en data URI, listo para un <img src="..."> en la vista. */
    public String generarDataUri(String contenido, int tamaño) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(generarPng(contenido, tamaño));
    }
}
