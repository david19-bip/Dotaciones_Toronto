package com.dotacionestoronto.dotacionestoronto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;

@Repository
/**
 * Acceso a datos de las solicitudes de devolucion de productos ya vendidos.
 */
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    /** Lista todas las devoluciones, de la mas reciente a la mas antigua. */
    List<Devolucion> findAllByOrderByFechaDevolucionDesc();
    /** Lista las devoluciones que se encuentran en un estado dado (por ejemplo: Pendiente, Aprobada, Rechazada). */
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
    /** Lista las devoluciones hechas por un usuario, resolviendo la cadena devolucion -> detalle_venta -> venta -> usuario mediante una consulta SQL nativa (no hay relacion JPA directa entre estas entidades). */
    List<Devolucion> findByUsuario(@Param("idUsuario") Long idUsuario);

    // Usado para calcular cuánto de un detalle_venta ya fue devuelto antes
    /** Lista las devoluciones registradas sobre una linea de venta especifica; se usa para calcular cuanto de esa linea ya fue devuelto. */
    List<Devolucion> findByIdDetalleVenta(Long idDetalleVenta);
}