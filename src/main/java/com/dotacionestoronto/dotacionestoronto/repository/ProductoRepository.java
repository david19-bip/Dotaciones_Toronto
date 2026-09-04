package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Acceso a datos del catalogo de productos.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    /** Lista los productos habilitados (visibles en el catalogo), del mas reciente al mas antiguo. */
    List<Producto> findByInhabilitadoFalseOrderByIdDesc();
}