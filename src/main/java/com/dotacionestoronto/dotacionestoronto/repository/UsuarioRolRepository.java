package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotacionestoronto.dotacionestoronto.entity.UsuarioRol;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Integer> {

    List<UsuarioRol> findByIdUsuario(Long idUsuario);

    boolean existsByIdUsuarioAndIdRol(Long idUsuario, Integer idRol);

    void deleteByIdUsuario(Long idUsuario);
}