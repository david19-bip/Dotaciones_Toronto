package com.dotacionestoronto.dotacionestoronto.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.UsuarioRol;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRolRepository;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            throw new UsernameNotFoundException("Correo o contraseña incorrectos");
        }

        if ("Inhabilitado".equals(usuario.getEstado())) {
            throw new DisabledException("Usuario inhabilitado");
        }

        List<UsuarioRol> rolesUsuario =
                usuarioRolRepository.findByIdUsuario(usuario.getId());

        List<SimpleGrantedAuthority> autoridades = new ArrayList<>();

        for (UsuarioRol usuarioRol : rolesUsuario) {

            String rol = switch (usuarioRol.getIdRol()) {
                case 1 -> "ROLE_ADMIN";
                case 2 -> "ROLE_CLIENTE";
                case 3 -> "ROLE_VENDEDOR";
                case 4 -> "ROLE_BODEGUERO";
                default -> null;
            };

            if (rol != null) {
                autoridades.add(new SimpleGrantedAuthority(rol));
            }
        }

        /*
         * Compatibilidad con usuarios existentes:
         * si todavía no tienen registros en usuario_rol,
         * usamos el rol que actualmente tienen en usuario.id_rol_fk.
         */
        if (autoridades.isEmpty()) {

            String rolPrincipal = switch (usuario.getIdRol()) {
                case 1 -> "ROLE_ADMIN";
                case 2 -> "ROLE_CLIENTE";
                case 3 -> "ROLE_VENDEDOR";
                case 4 -> "ROLE_BODEGUERO";
                default -> "ROLE_CLIENTE";
            };

            autoridades.add(
                    new SimpleGrantedAuthority(rolPrincipal)
            );
        }

        return new User(
                usuario.getCorreo(),
                usuario.getContrasena(),
                autoridades
        );
    }
}