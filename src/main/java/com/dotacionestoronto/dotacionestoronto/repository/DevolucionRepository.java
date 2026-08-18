package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    List<Devolucion> findAllByOrderByFechaDevolucionDesc();
    List<Devolucion> findByEstado(String estado);
}