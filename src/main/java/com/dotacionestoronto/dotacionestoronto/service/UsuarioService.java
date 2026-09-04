package com.dotacionestoronto.dotacionestoronto.service;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import org.springframework.security.authentication.DisabledException;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Puente entre las entidades {@link Usuario} de la aplicación y el modelo
 * de usuarios que entiende Spring Security ({@link UserDetails}).
 * Se usa como fuente de autenticación en {@link
 * com.dotacionestoronto.dotacionestoronto.config.SecurityConfig}.
 */
@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Busca al usuario por su correo (usado como "username" de login),
     * valida que no esté inhabilitado y traduce su {@code idRol} numérico
     * al authority de Spring Security correspondiente:
     * 1 = ROLE_ADMIN, 2 = ROLE_CLIENTE, 3 = ROLE_VENDEDOR, 4 = ROLE_BODEGUERO
     * (por defecto, ROLE_CLIENTE).
     *
     * @param correo correo electrónico ingresado en el formulario de login
     * @return los datos de autenticación (correo, contraseña cifrada y rol)
     * @throws UsernameNotFoundException si no existe un usuario con ese correo
     * @throws DisabledException si el usuario existe pero está inhabilitado
     */
    @Override
public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
    Usuario usuario = usuarioRepository.findByCorreo(correo);
    if (usuario == null) {
        throw new UsernameNotFoundException("Correo o contraseña incorrectos");
    }

    if ("Inhabilitado".equals(usuario.getEstado())) {
        throw new DisabledException("Usuario inhabilitado");
    }

    String rol = switch (usuario.getIdRol()) {
        case 1 -> "ROLE_ADMIN";
        case 2 -> "ROLE_CLIENTE";
        case 3 -> "ROLE_VENDEDOR";
        case 4 -> "ROLE_BODEGUERO";
        default -> "ROLE_CLIENTE";
    };

    return new org.springframework.security.core.userdetails.User(
        usuario.getCorreo(),
        usuario.getContrasena(),
        List.of(new SimpleGrantedAuthority(rol))
    );
    }
}