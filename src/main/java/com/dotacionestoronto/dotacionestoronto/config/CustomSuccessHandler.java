package com.dotacionestoronto.dotacionestoronto.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Manejador de éxito de autenticación usado por {@link SecurityConfig}.
 * Tras un login correcto, redirige al usuario al panel correspondiente a su
 * rol (Administrador, Vendedor, Cliente o Bodeguero) en lugar de enviarlo
 * siempre a la misma página.
 */
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Inspecciona los authorities (roles) del usuario recién autenticado y
     * redirige al panel que le corresponde. Si el usuario no tiene ninguno
     * de los roles esperados, se redirige de vuelta al login con un error.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/panel");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VENDEDOR"))) {
            response.sendRedirect("/vendedor/panel");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            response.sendRedirect("/cliente/panel");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_BODEGUERO"))) {
            response.sendRedirect("/bodeguero/panel");
        } else {
            response.sendRedirect("/login?error");
        }
    }
}