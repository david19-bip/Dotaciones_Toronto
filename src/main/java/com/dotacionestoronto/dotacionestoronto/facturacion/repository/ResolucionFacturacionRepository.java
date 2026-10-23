package com.dotacionestoronto.dotacionestoronto.facturacion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.ResolucionFacturacion;

import jakarta.persistence.LockModeType;

public interface ResolucionFacturacionRepository extends JpaRepository<ResolucionFacturacion, Long> {

    @Query("select r from ResolucionFacturacion r where r.tipoDocumento = :tipo and r.vigente = true")
    Optional<ResolucionFacturacion> findVigenteByTipo(@Param("tipo") String tipo);

    /**
     * Bloqueo pesimista: garantiza consecutivos únicos aunque dos usuarios
     * entreguen pedidos al mismo tiempo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ResolucionFacturacion r where r.id = :id")
    Optional<ResolucionFacturacion> findByIdForUpdate(@Param("id") Long id);
}
