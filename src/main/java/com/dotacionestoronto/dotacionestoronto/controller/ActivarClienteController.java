package com.dotacionestoronto.dotacionestoronto.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.UsuarioRol;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRolRepository;

@Controller
public class ActivarClienteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @PostMapping("/activar-cliente")
    public String activarCliente() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        String correo = authentication.getName();

        Usuario usuario =
                usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            return "redirect:/login";
        }

        /*
         * Verificamos los roles reales que tiene
         * el usuario en la base de datos.
         */
        List<UsuarioRol> rolesUsuario =
                usuarioRolRepository.findByIdUsuario(
                        usuario.getId()
                );

        boolean puedeActivarCliente =
                rolesUsuario.stream()
                        .anyMatch(rol ->
                                rol.getIdRol() == 1
                                || rol.getIdRol() == 3
                                || rol.getIdRol() == 4
                        );

        /*
         * Si solamente es Cliente, no puede
         * utilizar esta función.
         */
        if (!puedeActivarCliente) {
            return "redirect:/cliente/panel?error=rol";
        }

        /*
         * Comprobamos si ya tiene el rol Cliente.
         */
        boolean yaEsCliente =
                usuarioRolRepository
                        .existsByIdUsuarioAndIdRol(
                                usuario.getId(),
                                2
                        );

        /*
         * Si todavía no es Cliente, lo agregamos.
         * NO eliminamos ninguno de sus otros roles.
         */
        if (!yaEsCliente) {

            UsuarioRol nuevoRolCliente =
                    new UsuarioRol(
                            usuario.getId(),
                            2
                    );

            usuarioRolRepository.save(nuevoRolCliente);
        }

        return redireccionarSegunRolActivo();
    }

    private String redireccionarSegunRolActivo() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_ADMIN"))) {

            return "redirect:/admin/panel";
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_VENDEDOR"))) {

            return "redirect:/vendedor/panel";
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_BODEGUERO"))) {

            return "redirect:/bodeguero/panel";
        }

        return "redirect:/login";
    }
}