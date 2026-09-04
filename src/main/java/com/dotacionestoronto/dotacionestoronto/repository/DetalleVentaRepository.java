package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.DetalleVenta;

@Repository
/**
 * Acceso a datos de las lineas de una venta ya concretada.
 */
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    /** Lista todas las lineas (productos vendidos) de una venta. */
    List<DetalleVenta> findByIdVenta(Long idVenta);
    /** Busca la linea de un producto especifico dentro de una venta. */
    DetalleVenta findByIdVentaAndIdProducto(Long idVenta, Long idProducto);
}