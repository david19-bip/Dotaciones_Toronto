package com.dotacionestoronto.dotacionestoronto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Registro de existencias de un {@link Producto} en bodega: cantidad actual,
 * stock minimo de alerta y si el item esta habilitado para operar.
 */
@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long id;

    @Column(name = "id_producto_fk")
    private Long idProducto;

    @Column(name = "cantidad_actual")
    private Integer cantidadActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "inhabilitado")
    private Boolean inhabilitado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public Integer getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(Integer cantidadActual) { this.cantidadActual = cantidadActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public Boolean getInhabilitado() { return inhabilitado; }
    public void setInhabilitado(Boolean inhabilitado) { this.inhabilitado = inhabilitado; }
}