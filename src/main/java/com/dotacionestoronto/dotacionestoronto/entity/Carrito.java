package com.dotacionestoronto.dotacionestoronto.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

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
