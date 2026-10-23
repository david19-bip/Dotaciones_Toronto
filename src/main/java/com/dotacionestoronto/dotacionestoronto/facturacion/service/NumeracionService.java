package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.ResolucionFacturacion;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.ResolucionFacturacionRepository;

/**
 * Administra el consecutivo de la numeración autorizada por la DIAN.
 *
 * El consecutivo se incrementa con bloqueo pesimista (SELECT ... FOR UPDATE)
 * dentro de la misma transacción que crea la factura, de modo que si la
 * emisión falla el número no se "quema".
 */
@Service
public class NumeracionService {

    private final ResolucionFacturacionRepository resolucionRepository;

    public NumeracionService(ResolucionFacturacionRepository resolucionRepository) {
        this.resolucionRepository = resolucionRepository;
    }

    /** Datos del número asignado. */
    public record NumeroAsignado(String numeroFactura, String prefijo, long consecutivo, ResolucionFacturacion resolucion) { }

    public ResolucionFacturacion resolucionVigente() {
        return resolucionRepository.findVigenteByTipo("01")
                .orElseThrow(() -> new IllegalStateException(
                        "No hay una resolución de facturación vigente configurada. " +
                        "Ejecute el script facturacion_dian.sql o revise facturacion.numeracion.*"));
    }

    /**
     * Toma el siguiente consecutivo y lo reserva.
     * Debe invocarse dentro de una transacción de lectura/escritura.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public NumeroAsignado siguienteNumero() {

        ResolucionFacturacion base = resolucionVigente();

        ResolucionFacturacion r = resolucionRepository.findByIdForUpdate(base.getId())
                .orElseThrow(() -> new IllegalStateException("Resolución no encontrada"));

        LocalDate hoy = LocalDate.now(CufeService.ZONA_COLOMBIA);
        if (r.getVigenciaDesde() != null && hoy.isBefore(r.getVigenciaDesde())) {
            throw new IllegalStateException("La resolución " + r.getNumeroResolucion() + " aún no está vigente.");
        }
        if (r.getVigenciaHasta() != null && hoy.isAfter(r.getVigenciaHasta())) {
            throw new IllegalStateException("La resolución " + r.getNumeroResolucion() + " está vencida. Solicite una nueva a la DIAN.");
        }

        long ultimo = r.getConsecutivoActual() != null ? r.getConsecutivoActual() : (r.getRangoDesde() - 1);
        long siguiente = ultimo + 1;

        if (siguiente > r.getRangoHasta()) {
            throw new IllegalStateException("Se agotó el rango autorizado por la DIAN ("
                    + r.getRangoDesde() + " a " + r.getRangoHasta() + "). Solicite una nueva resolución.");
        }

        r.setConsecutivoActual(siguiente);
        resolucionRepository.save(r);

        return new NumeroAsignado(formatearNumero(r.getPrefijo(), siguiente, r.getRangoHasta()),
                r.getPrefijo(), siguiente, r);
    }

    /** Prefijo + consecutivo, rellenado con ceros a la izquierda. */
    public String formatearNumero(String prefijo, long consecutivo, Long rangoHasta) {
        int digitos = rangoHasta != null ? String.valueOf(rangoHasta).length() : 4;
        digitos = Math.max(digitos, 4);
        return (prefijo == null ? "" : prefijo) + String.format("%0" + digitos + "d", consecutivo);
    }
}
