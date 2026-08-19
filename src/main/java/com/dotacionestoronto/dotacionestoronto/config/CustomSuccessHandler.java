package com.dotacionestoronto.dotacionestoronto.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/panel_admin");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VENDEDOR"))) {
            response.sendRedirect("/vendedor/panel_vendedor");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            response.sendRedirect("/cliente/panel_cliente");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_BODEGUERO"))) {
            response.sendRedirect("/bodeguero/panel_bodeguero");
        } else {
            response.sendRedirect("/login?error");
        }
    }
}