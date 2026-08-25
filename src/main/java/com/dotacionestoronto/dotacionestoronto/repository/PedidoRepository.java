package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOcultoFalseOrderByFechaPedidoDesc();
    List<Pedido> findByEstadoAndOcultoFalse(String estado);

    // Usados por el panel y la lista de pedidos del cliente
    List<Pedido> findByIdUsuarioAndOcultoFalseOrderByFechaPedidoDesc(Long idUsuario);
    long countByIdUsuarioAndEstado(Long idUsuario, String estado);
    long countByIdUsuario(Long idUsuario);
}