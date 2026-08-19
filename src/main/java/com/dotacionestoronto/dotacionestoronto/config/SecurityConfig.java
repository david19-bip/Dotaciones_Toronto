package com.dotacionestoronto.dotacionestoronto.config;

import com.dotacionestoronto.dotacionestoronto.service.UsuarioService;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioService usuarioService;

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

                // Rutas públicas
                .requestMatchers(
                    "/login",
                    "/registro",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                // Rutas del administrador
                .requestMatchers("/admin/**")
                .hasAuthority("ROLE_ADMIN")

                // Rutas del vendedor
                .requestMatchers("/vendedor/**")
                .hasAuthority("ROLE_VENDEDOR")

                // Rutas del cliente
                .requestMatchers("/cliente/**")
                .hasAuthority("ROLE_CLIENTE")

                // Rutas del bodeguero
                .requestMatchers("/bodeguero/**")
                .hasAuthority("ROLE_BODEGUERO")

                // Cualquier otra ruta requiere autenticación
                .anyRequest()
                .authenticated()
            )

            // ==========================================
            // LOGIN
            // ==========================================

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(new CustomSuccessHandler())
                .failureUrl("/login?error")
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