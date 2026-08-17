package com.dotacionestoronto.dotacionestoronto.service;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + correo);
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