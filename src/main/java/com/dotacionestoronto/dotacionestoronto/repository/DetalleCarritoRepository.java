package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.DetalleCarrito;

@Repository
/**
 * Acceso a datos de las lineas de un carrito persistente.
 */
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    /** Lista todas las lineas de un carrito dado. */
    List<DetalleCarrito> findByIdCarrito(Long idCarrito);
    /** Busca la linea de un producto especifico dentro de un carrito. */
    DetalleCarrito findByIdCarritoAndIdProducto(Long idCarrito, Long idProducto);
}
