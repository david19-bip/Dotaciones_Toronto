package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByInhabilitadoFalse();
}