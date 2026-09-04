package com.dotacionestoronto.dotacionestoronto.repository;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Acceso a datos de los usuarios del sistema.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /** Busca un usuario por su correo electronico; es la clave usada para autenticacion (login). */
    Usuario findByCorreo(String correo);
    /** Busca un usuario por su numero de documento; se usa para validar que no se dupliquen usuarios al registrarse. */
    Usuario findByDocumento(String documento);
}