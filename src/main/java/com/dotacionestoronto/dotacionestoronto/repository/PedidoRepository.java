package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOcultoFalseOrderByFechaPedidoDesc();
    List<Pedido> findByEstadoAndOcultoFalse(String estado);

    // Usado en el panel principal del vendedor
    long countByEstadoAndOcultoFalse(String estado);

    // Usado por el historial de pedidos del cliente (incluye Entregado/Cancelado)
    List<Pedido> findByIdUsuarioOrderByFechaPedidoDesc(Long idUsuario);

    // Usados por el panel del cliente
    List<Pedido> findByIdUsuarioAndOcultoFalseOrderByFechaPedidoDesc(Long idUsuario);
    long countByIdUsuarioAndEstado(Long idUsuario, String estado);
    long countByIdUsuario(Long idUsuario);

    // Usado en devoluciones: solo pedidos entregados pueden devolverse
    List<Pedido> findByIdUsuarioAndEstado(Long idUsuario, String estado);
}