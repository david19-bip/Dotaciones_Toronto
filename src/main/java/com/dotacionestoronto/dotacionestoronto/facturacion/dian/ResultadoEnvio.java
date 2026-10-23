package com.dotacionestoronto.dotacionestoronto.facturacion.dian;

/**
 * Resultado de un envío a la DIAN.
 *
 * @param exitoso       true si el documento fue recibido (aunque quede en proceso)
 * @param estado        ACEPTADA | EN_PROCESO | RECHAZADA | ERROR
 * @param trackId       ZipKey / TrackId devuelto por la DIAN
 * @param mensaje       mensaje legible para el usuario
 * @param respuestaCruda respuesta completa del servicio (para auditoría)
 */
public record ResultadoEnvio(boolean exitoso, String estado, String trackId, String mensaje, String respuestaCruda) {

    public static ResultadoEnvio aceptada(String trackId, String mensaje, String crudo) {
        return new ResultadoEnvio(true, "ACEPTADA", trackId, mensaje, crudo);
    }

    public static ResultadoEnvio enProceso(String trackId, String mensaje, String crudo) {
        return new ResultadoEnvio(true, "EN_PROCESO", trackId, mensaje, crudo);
    }

    public static ResultadoEnvio rechazada(String mensaje, String crudo) {
        return new ResultadoEnvio(false, "RECHAZADA", null, mensaje, crudo);
    }

    public static ResultadoEnvio error(String mensaje, String crudo) {
        return new ResultadoEnvio(false, "ERROR", null, mensaje, crudo);
    }
}
