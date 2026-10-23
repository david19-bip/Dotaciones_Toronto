package com.dotacionestoronto.dotacionestoronto.facturacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.DetalleFacturaElectronica;

public interface DetalleFacturaElectronicaRepository extends JpaRepository<DetalleFacturaElectronica, Long> {

    List<DetalleFacturaElectronica> findByIdFacturaOrderByNumeroLineaAsc(Long idFactura);

    void deleteByIdFactura(Long idFactura);
}
