package com.dotacionestoronto.dotacionestoronto.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Representa el carrito de compras persistente de un usuario.
 * Nota: en el flujo actual de la aplicacion, el carrito de compra se maneja
 * en la sesion HTTP (ver CarritoController); esta entidad queda disponible
 * para un futuro carrito persistido en base de datos.
 */
@Entity
@Table(name = "carrito")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Long id;

    @Column(name = "id_usuario_fk")
    private Long idUsuario;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
