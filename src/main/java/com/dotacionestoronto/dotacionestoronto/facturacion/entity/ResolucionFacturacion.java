package com.dotacionestoronto.dotacionestoronto.facturacion.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Resolución de numeración autorizada por la DIAN.
 * El consecutivo se toma de aquí con bloqueo pesimista para que dos
 * facturas simultáneas nunca reciban el mismo número.
 */
@Entity
@Table(name = "resolucion_facturacion")
public class ResolucionFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resolucion")
    private Long id;

    /** 01 = factura electrónica de venta, 20 = nota crédito, etc. */
    @Column(name = "tipo_documento", length = 4)
    private String tipoDocumento = "01";

    @Column(name = "numero_resolucion", length = 40)
    private String numeroResolucion;

    @Column(name = "prefijo", length = 10)
    private String prefijo;

    @Column(name = "rango_desde")
    private Long rangoDesde;

    @Column(name = "rango_hasta")
    private Long rangoHasta;

    @Column(name = "consecutivo_actual")
    private Long consecutivoActual;

    @Column(name = "clave_tecnica", length = 128)
    private String claveTecnica;

    @Column(name = "vigencia_desde")
    private LocalDate vigenciaDesde;

    @Column(name = "vigencia_hasta")
    private LocalDate vigenciaHasta;

    @Column(name = "vigente")
    private Boolean vigente = Boolean.TRUE;

    @Column(name = "notificacion", length = 100)
    private String notificacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String v) { this.tipoDocumento = v; }

    public String getNumeroResolucion() { return numeroResolucion; }
    public void setNumeroResolucion(String v) { this.numeroResolucion = v; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String v) { this.prefijo = v; }

    public Long getRangoDesde() { return rangoDesde; }
    public void setRangoDesde(Long v) { this.rangoDesde = v; }

    public Long getRangoHasta() { return rangoHasta; }
    public void setRangoHasta(Long v) { this.rangoHasta = v; }

    public Long getConsecutivoActual() { return consecutivoActual; }
    public void setConsecutivoActual(Long v) { this.consecutivoActual = v; }

    public String getClaveTecnica() { return claveTecnica; }
    public void setClaveTecnica(String v) { this.claveTecnica = v; }

    public LocalDate getVigenciaDesde() { return vigenciaDesde; }
    public void setVigenciaDesde(LocalDate v) { this.vigenciaDesde = v; }

    public LocalDate getVigenciaHasta() { return vigenciaHasta; }
    public void setVigenciaHasta(LocalDate v) { this.vigenciaHasta = v; }

    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean v) { this.vigente = v; }

    public String getNotificacion() { return notificacion; }
    public void setNotificacion(String v) { this.notificacion = v; }
}
