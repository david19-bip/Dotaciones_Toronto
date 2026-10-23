package com.dotacionestoronto.dotacionestoronto.facturacion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;

public interface FacturaElectronicaRepository extends JpaRepository<FacturaElectronica, Long> {

    Optional<FacturaElectronica> findByIdVenta(Long idVenta);

    Optional<FacturaElectronica> findByNumeroFactura(String numeroFactura);

    List<FacturaElectronica> findAllByOrderByFechaEmisionDesc();

    List<FacturaElectronica> findByEstadoOrderByFechaEmisionDesc(String estado);

    @Query("select f from FacturaElectronica f where f.idCliente = ?1 order by f.fechaEmision desc")
    List<FacturaElectronica> findByIdCliente(Long idCliente);

    long countByEstado(String estado);

    boolean existsByIdVenta(Long idVenta);
}
