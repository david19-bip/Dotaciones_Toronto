package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.DetallePedido;

@Repository
/**
 * Acceso a datos de las lineas (productos, cantidades, precios) de un pedido.
 */
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    /** Lista todas las lineas de un pedido, usadas por ejemplo para calcular su total o generar la venta correspondiente. */
    List<DetallePedido> findByIdPedido(Long idPedido);
}