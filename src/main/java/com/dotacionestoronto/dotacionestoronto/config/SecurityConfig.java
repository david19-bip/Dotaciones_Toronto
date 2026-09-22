package com.dotacionestoronto.dotacionestoronto.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.dotacionestoronto.dotacionestoronto.service.UsuarioService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================================
    // CONFIGURACIÓN DE SEGURIDAD
    // ==========================================

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // ==========================================
                // RUTAS PÚBLICAS
                // ==========================================

                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                // ==========================================
                // SELECTOR DE ROLES
                // ==========================================

                .requestMatchers("/seleccionar-rol")
                .authenticated()

                .requestMatchers("/cambiar-rol")
                .authenticated()

                // ==========================================
                // ADMINISTRADOR
                // ==========================================

                .requestMatchers("/admin/**")
                .hasAuthority("ROLE_ADMIN")

                // ==========================================
                // VENDEDOR
                // ==========================================

                .requestMatchers("/vendedor/**")
                .hasAuthority("ROLE_VENDEDOR")

                // ==========================================
                // CLIENTE
                // ==========================================

                .requestMatchers("/cliente/**")
                .hasAuthority("ROLE_CLIENTE")

                // ==========================================
                // BODEGUERO
                // ==========================================

                .requestMatchers("/bodeguero/**")
                .hasAuthority("ROLE_BODEGUERO")

                // ==========================================
                // CUALQUIER OTRA RUTA
                // ==========================================

                .anyRequest()
                .authenticated()
            )

            // ==========================================
            // LOGIN
            // ==========================================

            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                .successHandler(customSuccessHandler)

                .failureHandler((request, response, exception) -> {

                    String mensaje =
                        exception.getMessage() != null
                            ? exception.getMessage()
                            : "";

                    Throwable causa = exception.getCause();

                    String mensajeCausa =
                        causa != null && causa.getMessage() != null
                            ? causa.getMessage()
                            : "";

                    if (
                        mensaje.contains("inhabilitado")
                        || mensajeCausa.contains("inhabilitado")
                        || exception.getClass()
                            .getSimpleName()
                            .contains("Disabled")
                        || (
                            causa != null
                            && causa.getClass()
                                .getSimpleName()
                                .contains("Disabled")
                        )
                    ) {

                        response.sendRedirect("/login?inhabilitado");

                    } else {

                        response.sendRedirect("/login?error");
                    }
                })

                .permitAll()
            )

            // ==========================================
            // LOGOUT
            // ==========================================

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/login?logout")

                .logoutRequestMatcher(
                    new AntPathRequestMatcher("/logout")
                )

                .permitAll()
            );

        return http.build();
    }

    // ==========================================
    // AUTHENTICATION MANAGER
    // ==========================================

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder builder =
            http.getSharedObject(
                AuthenticationManagerBuilder.class
            );

        builder
            .userDetailsService(usuarioService)
            .passwordEncoder(passwordEncoder());

        return builder.build();
    }
}