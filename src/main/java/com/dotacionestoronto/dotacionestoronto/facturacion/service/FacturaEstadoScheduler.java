package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;

/**
 * Consulta periódicamente el estado de las facturas que quedaron ENVIADAS
 * (la DIAN procesa de forma asíncrona: primero devuelve un ZipKey y luego el
 * resultado definitivo).
 *
 * Solo se ejecuta cuando el modo es DIAN o PROVEEDOR (transmisión real).
 */
@Component
public class FacturaEstadoScheduler {

    private static final Logger log = LoggerFactory.getLogger(FacturaEstadoScheduler.class);

    private final FacturacionProperties props;
    private final FacturacionService facturacionService;

    public FacturaEstadoScheduler(FacturacionProperties props, FacturacionService facturacionService) {
        this.props = props;
        this.facturacionService = facturacionService;
    }

    /** Cada 3 minutos revisa las facturas pendientes de respuesta. */
    @Scheduled(fixedDelay = 180_000, initialDelay = 60_000)
    public void consultarPendientes() {
        if (!props.isHabilitada() || !props.isTransmisionReal()) {
            return;
        }
        try {
            List<FacturaElectronica> pendientes =
                    facturacionService.listarPorEstado(FacturaElectronica.ESTADO_ENVIADA);

            for (FacturaElectronica f : pendientes) {
                try {
                    facturacionService.consultarEstado(f.getId());
                } catch (Exception e) {
                    log.warn("No fue posible consultar la factura {}: {}", f.getNumeroFactura(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Error en la consulta programada de estados DIAN: {}", e.getMessage());
        }
    }
}
