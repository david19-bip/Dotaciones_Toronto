package com.dotacionestoronto.dotacionestoronto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.Carrito;

@Repository
/**
 * Acceso a datos de carritos de compra persistentes (no usado en el flujo actual de checkout en sesion).
 */
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    /** Busca el carrito activo asociado a un usuario. */
    Carrito findByIdUsuario(Long idUsuario);
}
