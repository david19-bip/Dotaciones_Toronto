package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    List<Devolucion> findAllByOrderByFechaDevolucionDesc();
    List<Devolucion> findByEstado(String estado);

    // La tabla devolucion solo tiene id_detalle_venta_fk, no id_usuario_fk directo.
    // No existe entity DetalleVenta relacionada por JPA, así que se llega al usuario
    // con una consulta nativa: devolucion -> detalle_venta -> venta -> usuario.
    @Query(value = "SELECT d.* FROM devolucion d " +
            "JOIN detalle_venta dv ON d.id_detalle_venta_fk = dv.id_detalle_venta " +
            "JOIN venta v ON dv.id_venta_fk = v.id_venta " +
            "WHERE v.id_usuario_fk = :idUsuario " +
            "ORDER BY d.fecha_devolucion DESC",
            nativeQuery = true)
    List<Devolucion> findByUsuario(@Param("idUsuario") Long idUsuario);

    // Usado para calcular cuánto de un detalle_venta ya fue devuelto antes
    List<Devolucion> findByIdDetalleVenta(Long idDetalleVenta);
}