package com.dotacionestoronto.dotacionestoronto.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Solicitud de devolucion de uno o varios productos de una venta ya realizada,
 * asociada a un {@link DetalleVenta}. Incluye motivo, cantidad devuelta y
 * estado del tramite (por ejemplo: Pendiente, Aprobada, Rechazada).
 */
@Entity
@Table(name = "devolucion")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Long id;

    @Column(name = "id_detalle_venta_fk")
    private Long idDetalleVenta;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Column(name = "estado")
    private String estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(Long idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}