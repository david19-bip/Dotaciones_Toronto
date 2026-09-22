package com.dotacionestoronto.dotacionestoronto.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.UsuarioRol;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRolRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String correo = authentication.getName();

        Usuario usuario =
                usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            response.sendRedirect("/login?error");
            return;
        }

        HttpSession session = request.getSession();

        // Guardamos el usuario actualmente conectado
        session.setAttribute("usuarioLogueado", usuario);

        // Obtenemos todos los roles del usuario
        List<UsuarioRol> rolesUsuario =
                usuarioRolRepository.findByIdUsuario(usuario.getId());

        // Guardamos los roles disponibles en la sesión
        // para poder mostrar el botón "Cambiar rol"
        session.setAttribute("rolesDisponibles", rolesUsuario);

        /*
         * Si tiene más de un rol,
         * primero debe seleccionar cuál quiere utilizar.
         */
        if (rolesUsuario.size() > 1) {

            response.sendRedirect("/seleccionar-rol");
            return;
        }

        /*
         * Si tiene exactamente un rol,
         * lo establecemos como rol activo.
         */
        if (rolesUsuario.size() == 1) {

            Integer idRol =
                    rolesUsuario.get(0).getIdRol();

            String rolActivo =
                    obtenerNombreRol(idRol);

            if (rolActivo != null) {
                session.setAttribute(
                        "rolActivo",
                        rolActivo
                );
            }
        }

        /*
         * Compatibilidad con usuarios que todavía
         * solamente tienen el rol en id_rol_fk.
         */
        if (rolesUsuario.isEmpty()) {

            String rolActivo =
                    obtenerNombreRol(usuario.getIdRol());

            if (rolActivo != null) {

                session.setAttribute(
                        "rolActivo",
                        rolActivo
                );
            }
        }

        /*
         * Redirección inicial según los roles
         * que tiene el usuario.
         */
        if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_ADMIN"))) {

            response.sendRedirect("/admin/panel");

        } else if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_VENDEDOR"))) {

            response.sendRedirect("/vendedor/panel");

        } else if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_CLIENTE"))) {

            response.sendRedirect("/cliente/panel");

        } else if (authentication.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_BODEGUERO"))) {

            response.sendRedirect("/bodeguero/panel");

        } else {

            response.sendRedirect("/login?error");
        }
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