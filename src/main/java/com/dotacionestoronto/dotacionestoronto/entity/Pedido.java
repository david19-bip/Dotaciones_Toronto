package com.dotacionestoronto.dotacionestoronto.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Pedido realizado por un cliente. Nace en estado "Pendiente" al finalizar el
 * carrito de compra y cambia de estado a medida que el vendedor lo gestiona.
 * Cuando llega a "Entregado" se genera automaticamente una {@link Venta}.
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id;

    @Column(name = "id_usuario_fk")
    private Long idUsuario;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Column(name = "estado")
    private String estado;

    @Column(name = "oculto")
    private Boolean oculto;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getOculto() { return oculto; }
    public void setOculto(Boolean oculto) { this.oculto = oculto; }
}