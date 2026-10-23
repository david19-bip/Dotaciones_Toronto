package com.dotacionestoronto.dotacionestoronto.facturacion.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "factura_electronica_detalle")
public class DetalleFacturaElectronica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_factura")
    private Long id;

    @Column(name = "id_factura_fk", nullable = false)
    private Long idFactura;

    @Column(name = "numero_linea")
    private Integer numeroLinea;

    @Column(name = "codigo_producto", length = 60)
    private String codigoProducto;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "cantidad", precision = 19, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "unidad_medida", length = 6)
    private String unidadMedida = "94";

    @Column(name = "precio_unitario", precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "descuento", precision = 19, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "iva_porcentaje", precision = 6, scale = 2)
    private BigDecimal ivaPorcentaje = BigDecimal.ZERO;

    @Column(name = "iva_valor", precision = 19, scale = 2)
    private BigDecimal ivaValor = BigDecimal.ZERO;

    @Column(name = "total_linea", precision = 19, scale = 2)
    private BigDecimal totalLinea = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdFactura() { return idFactura; }
    public void setIdFactura(Long v) { this.idFactura = v; }

    public Integer getNumeroLinea() { return numeroLinea; }
    public void setNumeroLinea(Integer v) { this.numeroLinea = v; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String v) { this.codigoProducto = v; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal v) { this.cantidad = v; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String v) { this.unidadMedida = v; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal v) { this.precioUnitario = v; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal v) { this.descuento = v; }

    public BigDecimal getIvaPorcentaje() { return ivaPorcentaje; }
    public void setIvaPorcentaje(BigDecimal v) { this.ivaPorcentaje = v; }

    public BigDecimal getIvaValor() { return ivaValor; }
    public void setIvaValor(BigDecimal v) { this.ivaValor = v; }

    public BigDecimal getTotalLinea() { return totalLinea; }
    public void setTotalLinea(BigDecimal v) { this.totalLinea = v; }
}
