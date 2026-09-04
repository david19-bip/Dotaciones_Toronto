package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Acceso a datos de los registros de inventario por producto.
 */
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    /** Lista los registros de inventario que estan actualmente habilitados (activos). */
    List<Inventario> findByInhabilitadoFalse();
}