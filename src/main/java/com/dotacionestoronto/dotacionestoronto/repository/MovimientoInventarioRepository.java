package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotacionestoronto.dotacionestoronto.entity.MovimientoInventario;

/**
 * Acceso a datos del historial de movimientos de inventario (entradas, salidas y ajustes).
 */
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    /** Obtiene los ultimos 50 movimientos registrados, del mas reciente al mas antiguo, para mostrar el historial en el panel correspondiente. */
    List<MovimientoInventario> findTop50ByOrderByIdDesc();
}
