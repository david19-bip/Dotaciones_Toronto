package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.Pedido;

@Repository
/**
 * Acceso a datos de los pedidos generados por los clientes.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    /** Lista los pedidos visibles (no ocultos), del mas reciente al mas antiguo. */
    List<Pedido> findByOcultoFalseOrderByFechaPedidoDesc();
    /** Lista los pedidos visibles que se encuentran en un estado especifico. */
    List<Pedido> findByEstadoAndOcultoFalse(String estado);

    // Usado en el panel principal del vendedor
    /** Cuenta los pedidos visibles en un estado dado; usado para las metricas del panel del vendedor. */
    long countByEstadoAndOcultoFalse(String estado);

    // Usados por el panel y la lista de pedidos del cliente
    /** Lista los pedidos visibles de un cliente especifico, del mas reciente al mas antiguo. */
    List<Pedido> findByIdUsuarioAndOcultoFalseOrderByFechaPedidoDesc(Long idUsuario);
    /** Cuenta los pedidos de un cliente que estan en un estado dado. */
    long countByIdUsuarioAndEstado(Long idUsuario, String estado);
    /** Cuenta el total de pedidos realizados por un cliente. */
    long countByIdUsuario(Long idUsuario);

    // Usado en devoluciones: solo pedidos entregados pueden devolverse
    /** Lista los pedidos de un cliente en un estado dado; se usa por ejemplo para saber que pedidos entregados son elegibles para devolucion. */
    List<Pedido> findByIdUsuarioAndEstado(Long idUsuario, String estado);
}