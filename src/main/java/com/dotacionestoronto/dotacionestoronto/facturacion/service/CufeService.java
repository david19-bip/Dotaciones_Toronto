package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;

/**
 * Cálculo del CUFE (Código Único de Factura Electrónica) y utilidades
 * criptográficas exigidas por el Anexo Técnico 1.9 de la DIAN
 * (Resolución 000165 de 2023, numeral 11.2).
 *
 * Cadena oficial:
 *   CUFE = SHA-384( NumFac + FecFac + HorFac + ValFac +
 *                   CodImp1 + ValImp1 + CodImp2 + ValImp2 + CodImp3 + ValImp3 +
 *                   ValTot + NitOFE + NumAdq + ClTec + TipoAmbiente )
 */
@Service
public class CufeService {

    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Construye la cadena exacta que se le pasa al SHA-384.
     * Todos los valores monetarios van con dos decimales y punto decimal.
     */
    public String construirCadena(FacturaElectronica f) {
        LocalDateTime fecha = f.getFechaEmision() != null ? f.getFechaEmision() : LocalDateTime.now(ZONA_COLOMBIA);

        String numFac = nvl(f.getNumeroFactura());
        String fecFac = fecha.format(FECHA);
        String horFac = horaConZona(fecha);

        String valFac = dinero(f.getValorBruto());                 // valor sin impuestos
        String codImp1 = "01";                                     // IVA
        String valImp1 = dinero(f.getTotalIva());
        String codImp2 = "04";                                     // INC
        String valImp2 = "0.00";
        String codImp3 = "03";                                     // ICA
        String valImp3 = "0.00";
        String valTot = dinero(f.getTotalFactura());
        String nitOfe = soloDigitos(f.getNitEmisor());
        String numAdq = soloDigitos(f.getNitAdquirente());
        String clTec = nvl(f.getClaveTecnica());
        String tipoAmbiente = nvl(f.getAmbiente() != null ? f.getAmbiente() : "2");

        return numFac + fecFac + horFac + valFac
                + codImp1 + valImp1
                + codImp2 + valImp2
                + codImp3 + valImp3
                + valTot + nitOfe + numAdq + clTec + tipoAmbiente;
    }

    /** Calcula y asigna el CUFE a la factura. */
    public String calcularYAsignar(FacturaElectronica f) {
        String cadena = construirCadena(f);
        String cufe = sha384Hex(cadena);
        f.setCadenaCufe(cadena);
        f.setCufe(cufe);
        return cufe;
    }

    /**
     * SoftwareSecurityCode = SHA-384(SoftwareID + PIN + NumFac)
     * (Anexo Técnico 1.9, DianExtensions).
     */
    public String calcularSoftwareSecurityCode(String softwareId, String pin, String numeroFactura) {
        return sha384Hex(nvl(softwareId) + nvl(pin) + nvl(numeroFactura));
    }

    /** URL oficial del código QR según el ambiente. */
    public String construirUrlQr(FacturaElectronica f) {
        String base = "1".equals(f.getAmbiente())
                ? "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey="
                : "https://catalogo-vpfe-hab.dian.gov.co/document/searchqr?documentkey=";
        return base + nvl(f.getCufe());
    }

    /**
     * Datos del QR en el orden del Anexo Técnico (se imprimen bajo el código
     * para que cualquier lector pueda contrastar la información).
     */
    public String construirDatosQr(FacturaElectronica f) {
        LocalDateTime fecha = f.getFechaEmision() != null ? f.getFechaEmision() : LocalDateTime.now(ZONA_COLOMBIA);
        return "NumFac: " + nvl(f.getNumeroFactura())
                + "  FecFac: " + fecha.format(FECHA)
                + "  HorFac: " + horaConZona(fecha)
                + "  NitFac: " + soloDigitos(f.getNitEmisor())
                + "  DocAdq: " + soloDigitos(f.getNitAdquirente())
                + "  ValFac: " + dinero(f.getValorBruto())
                + "  ValIva: " + dinero(f.getTotalIva())
                + "  ValOtroIm: 0.00"
                + "  ValTolFac: " + dinero(f.getTotalFactura())
                + "  CUFE: " + nvl(f.getCufe());
    }

    // ==========================================================
    // UTILIDADES
    // ==========================================================

    public String horaConZona(LocalDateTime fecha) {
        ZonedDateTime z = fecha.atZone(ZONA_COLOMBIA);
        String offset = z.getOffset().getId().replace("Z", "+00:00");
        return fecha.format(HORA) + offset;      // Ej: 14:33:07-05:00
    }

    public String sha384Hex(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible calcular SHA-384", e);
        }
    }

    public String sha256Hex(byte[] datos) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datos);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible calcular SHA-256", e);
        }
    }


    public String sha256Base64(byte[] datos) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datos);
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible calcular SHA-256", e);
        }
    }

    /** Dígito de verificación del NIT (módulo 11, algoritmo DIAN). */
    public String calcularDigitoVerificacion(String nit) {
        String numero = soloDigitos(nit);
        if (numero.isEmpty()) {
            return "";
        }
        int[] pesos = {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};
        int suma = 0;
        int index = 0;
        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numero.charAt(i)) * pesos[index];
            index++;
        }
        int residuo = suma % 11;
        int dv = residuo > 1 ? 11 - residuo : residuo;
        return String.valueOf(dv);
    }

    public String dinero(BigDecimal valor) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public String soloDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("[^0-9]", "");
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
