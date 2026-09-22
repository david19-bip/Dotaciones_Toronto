package com.dotacionestoronto.dotacionestoronto.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.UsuarioRol;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRolRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class SeleccionRolController {

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @GetMapping("/seleccionar-rol")
    public String seleccionarRol(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<UsuarioRol> roles =
                usuarioRolRepository.findByIdUsuario(
                        usuario.getId()
                );

        model.addAttribute("roles", roles);

        return "seleccionar-rol";
    }

    @PostMapping("/seleccionar-rol")
    public String guardarRol(
            @RequestParam("idRol") Integer idRol,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        /*
         * Verificamos que el rol seleccionado
         * realmente pertenezca al usuario.
         */
        boolean tieneRol =
                usuarioRolRepository
                        .existsByIdUsuarioAndIdRol(
                                usuario.getId(),
                                idRol
                        );

        if (!tieneRol) {
            return "redirect:/seleccionar-rol?error";
        }

        String autoridad = obtenerNombreRol(idRol);

        if (autoridad == null) {
            return "redirect:/seleccionar-rol?error";
        }

        /*
         * Obtenemos la autenticación actual.
         */
        Authentication autenticacionActual =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        /*
         * Creamos una nueva autenticación
         * con SOLAMENTE el rol seleccionado.
         */
        UsernamePasswordAuthenticationToken nuevaAutenticacion =
                new UsernamePasswordAuthenticationToken(
                        autenticacionActual.getPrincipal(),
                        autenticacionActual.getCredentials(),
                        List.of(
                                new SimpleGrantedAuthority(autoridad)
                        )
                );

        /*
         * Reemplazamos la autenticación actual.
         */
        SecurityContextHolder
                .getContext()
                .setAuthentication(nuevaAutenticacion);

        /*
         * Guardamos el rol activo en la sesión.
         */
        session.setAttribute("rolActivo", autoridad);

        /*
         * Guardamos el nuevo SecurityContext
         * para que el cambio permanezca en las
         * siguientes peticiones.
         */
        HttpSessionSecurityContextRepository
                securityContextRepository =
                    new HttpSessionSecurityContextRepository();

        securityContextRepository.saveContext(
                SecurityContextHolder.getContext(),
                request,
                response
        );

        /*
         * Enviamos al usuario al panel
         * correspondiente.
         */
        return switch (idRol) {

            case 1 -> "redirect:/admin/panel";

            case 2 -> "redirect:/cliente/panel";

            case 3 -> "redirect:/vendedor/panel";

            case 4 -> "redirect:/bodeguero/panel";

            default -> "redirect:/login?error";
        };
    }

    @GetMapping("/cambiar-rol")
    public String cambiarRol() {
        return "redirect:/seleccionar-rol";
    }

    private String obtenerNombreRol(Integer idRol) {

        if (idRol == null) {
            return null;
        }

        return switch (idRol) {

            case 1 -> "ROLE_ADMIN";

            case 2 -> "ROLE_CLIENTE";

            case 3 -> "ROLE_VENDEDOR";

            case 4 -> "ROLE_BODEGUERO";

            default -> null;
        };
    }
}