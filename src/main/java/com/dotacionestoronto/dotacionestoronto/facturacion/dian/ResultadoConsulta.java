package com.dotacionestoronto.dotacionestoronto.facturacion.dian;

/** Resultado de la consulta del estado de un documento en la DIAN. */
public record ResultadoConsulta(boolean encontrado, String estado, String mensaje, String respuestaCruda) {

    public static ResultadoConsulta aceptada(String mensaje, String crudo) {
        return new ResultadoConsulta(true, "ACEPTADA", mensaje, crudo);
    }

    public static ResultadoConsulta rechazada(String mensaje, String crudo) {
        return new ResultadoConsulta(true, "RECHAZADA", mensaje, crudo);
    }

    public static ResultadoConsulta enProceso(String mensaje, String crudo) {
        return new ResultadoConsulta(true, "EN_PROCESO", mensaje, crudo);
    }

    public static ResultadoConsulta error(String mensaje, String crudo) {
        return new ResultadoConsulta(false, "ERROR", mensaje, crudo);
    }
}
