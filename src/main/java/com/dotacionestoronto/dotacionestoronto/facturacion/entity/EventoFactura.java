package com.dotacionestoronto.dotacionestoronto.facturacion.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Bitácora de eventos de una factura (auditoría del proceso DIAN).
 */
@Entity
@Table(name = "factura_electronica_evento")
public class EventoFactura {

    public static final String TIPO_GENERACION = "GENERACION";
    public static final String TIPO_ENVIO     = "ENVIO";
    public static final String TIPO_RESPUESTA = "RESPUESTA";
    public static final String TIPO_ERROR     = "ERROR";
    public static final String TIPO_REENVIO   = "REENVIO";
    public static final String TIPO_CAMBIO_ESTADO = "CAMBIO_ESTADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long id;

    @Column(name = "id_factura_fk", nullable = false)
    private Long idFactura;

    @Column(name = "fecha")
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "tipo", length = 30)
    private String tipo;

    @Column(name = "estado", length = 30)
    private String estado;

    @Column(name = "mensaje", length = 500)
    private String mensaje;

    @Lob
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    public EventoFactura() { }

    public EventoFactura(Long idFactura, String tipo, String estado, String mensaje, String payload) {
        this.idFactura = idFactura;
        this.tipo = tipo;
        this.estado = estado;
        this.mensaje = mensaje;
        this.payload = payload;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdFactura() { return idFactura; }
    public void setIdFactura(Long v) { this.idFactura = v; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime v) { this.fecha = v; }

    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }

    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String v) { this.mensaje = v; }

    public String getPayload() { return payload; }
    public void setPayload(String v) { this.payload = v; }
}
