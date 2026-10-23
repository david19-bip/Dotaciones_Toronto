package com.dotacionestoronto.dotacionestoronto.facturacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.EventoFactura;

public interface EventoFacturaRepository extends JpaRepository<EventoFactura, Long> {

    List<EventoFactura> findByIdFacturaOrderByFechaAsc(Long idFactura);
}
