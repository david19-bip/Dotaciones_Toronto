package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Acceso a datos de las ventas generadas a partir de pedidos entregados.
 */
public interface VentaRepository extends JpaRepository<Venta, Long> {
    /** Lista todas las ventas, de la mas reciente a la mas antigua. */
    List<Venta> findAllByOrderByFechaVentaDesc();

    // Usado en devoluciones: encontrar la venta asociada a un pedido
    /** Busca la venta asociada a un pedido especifico; se usa tanto para evitar duplicar ventas como para resolver devoluciones. */
    Venta findByIdPedido(Long idPedido);
}