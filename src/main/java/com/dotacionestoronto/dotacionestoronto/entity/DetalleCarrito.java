package com.dotacionestoronto.dotacionestoronto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_carrito")
public class DetalleCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_carrito")
    private Long id;

    @Column(name = "id_carrito_fk")
    private Long idCarrito;

    @Column(name = "id_producto_fk")
    private Long idProducto;

    @Column(name = "cantidad")
    private Integer cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdCarrito() { return idCarrito; }
    public void setIdCarrito(Long idCarrito) { this.idCarrito = idCarrito; }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
