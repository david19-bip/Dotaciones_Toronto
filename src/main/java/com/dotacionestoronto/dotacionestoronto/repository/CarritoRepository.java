package com.dotacionestoronto.dotacionestoronto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Carrito findByIdUsuario(Long idUsuario);
}
