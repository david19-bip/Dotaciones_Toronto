package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOcultoFalseOrderByFechaPedidoDesc();
    List<Pedido> findByEstadoAndOcultoFalse(String estado);
}