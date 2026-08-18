package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findAllByOrderByFechaVentaDesc();
}