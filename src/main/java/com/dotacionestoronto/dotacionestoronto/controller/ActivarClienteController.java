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

import jakarta.servlet.http.HttpSession;

@Controller
public class ActivarClienteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @PostMapping("/activar-cliente")
    public String activarCliente(HttpSession session) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        // Verificamos que exista una sesión autenticada
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        // Obtenemos el correo del usuario actualmente autenticado
        String correo = authentication.getName();

        // Buscamos al usuario en la base de datos
        Usuario usuario =
                usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            return "redirect:/login";
        }

        /*
         * Obtenemos todos los roles que tiene
         * actualmente el usuario.
         */
        List<UsuarioRol> rolesUsuario =
                usuarioRolRepository.findByIdUsuario(
                        usuario.getId()
                );

        /*
         * Solo Administrador, Vendedor o Bodeguero
         * pueden activar el rol Cliente.
         */
        boolean puedeActivarCliente =
                rolesUsuario.stream()
                        .anyMatch(rol ->
                                rol.getIdRol() == 1
                                || rol.getIdRol() == 3
                                || rol.getIdRol() == 4
                        );

        /*
         * Si solamente es Cliente, no puede utilizar
         * esta función para obtener otros privilegios.
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
         * Si todavía no tiene Cliente,
         * lo agregamos sin eliminar los demás roles.
         */
        if (!yaEsCliente) {

            UsuarioRol nuevoRolCliente =
                    new UsuarioRol(
                            usuario.getId(),
                            2
                    );

            usuarioRolRepository.save(nuevoRolCliente);
        }

        /*
         * Volvemos a consultar los roles para tener
         * la lista actualizada.
         */
        List<UsuarioRol> rolesActualizados =
                usuarioRolRepository.findByIdUsuario(
                        usuario.getId()
                );

        /*
         * Guardamos los roles actualizados en sesión.
         * Esto permite que aparezca "Cambiar rol"
         * inmediatamente, sin cerrar sesión.
         */
        session.setAttribute(
                "rolesDisponibles",
                rolesActualizados
        );

        /*
         * Mantenemos al usuario en el rol que
         * actualmente estaba utilizando.
         */
        String rolActivo =
                (String) session.getAttribute("rolActivo");

        /*
         * Si por alguna razón no existe rolActivo,
         * usamos la autoridad actual de Spring Security.
         */
        if (rolActivo == null) {

            if (authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            a.getAuthority()
                                    .equals("ROLE_ADMIN"))) {

                rolActivo = "ROLE_ADMIN";

            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            a.getAuthority()
                                    .equals("ROLE_VENDEDOR"))) {

                rolActivo = "ROLE_VENDEDOR";

            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a ->
                            a.getAuthority()
                                    .equals("ROLE_BODEGUERO"))) {

                rolActivo = "ROLE_BODEGUERO";

            } else {

                rolActivo = "ROLE_CLIENTE";
            }

            session.setAttribute(
                    "rolActivo",
                    rolActivo
            );
        }

        /*
         * Redirigimos al panel correspondiente
         * al rol que estaba utilizando.
         */
        return switch (rolActivo) {

            case "ROLE_ADMIN" ->
                    "redirect:/admin/panel";

            case "ROLE_VENDEDOR" ->
                    "redirect:/vendedor/panel";

            case "ROLE_BODEGUERO" ->
                    "redirect:/bodeguero/panel";

            case "ROLE_CLIENTE" ->
                    "redirect:/cliente/panel";

            default ->
                    "redirect:/login";
        };
    }
}