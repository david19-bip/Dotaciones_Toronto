package com.dotacionestoronto.dotacionestoronto.facturacion.dian;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;

/**
 * Contrato de comunicación con la DIAN (o con un proveedor tecnológico).
 * Permite cambiar de modo (simulación, DIAN directa, proveedor) sin tocar
 * el resto del módulo.
 */
public interface DianGateway {

    /** Identificador del modo: SIMULADO | DIAN | PROVEEDOR. */
    String modo();

    /** Envía el XML firmado y devuelve el resultado de la transmisión. */
    ResultadoEnvio enviar(FacturaElectronica factura, String xmlFirmado);

    /** Consulta el estado de un envío por trackId / zipKey. */
    ResultadoConsulta consultar(String trackId);

    /** Indica si el canal permite consultar el estado después del envío. */
    default boolean soportaConsulta() {
        return true;
    }
}
