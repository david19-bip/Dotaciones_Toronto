package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dotacionestoronto.dotacionestoronto.entity.UsuarioRol;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Integer> {

    List<UsuarioRol> findByIdUsuario(Long idUsuario);

    boolean existsByIdUsuarioAndIdRol(Long idUsuario, Integer idRol);

    @Modifying
    @Query("DELETE FROM UsuarioRol ur WHERE ur.idUsuario = :idUsuario")
    void deleteByIdUsuario(@Param("idUsuario") Long idUsuario);
}