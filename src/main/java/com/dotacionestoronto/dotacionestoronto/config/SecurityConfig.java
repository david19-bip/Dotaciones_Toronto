package com.dotacionestoronto.dotacionestoronto.config;

import com.dotacionestoronto.dotacionestoronto.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.DisabledException;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración principal de Spring Security para Dotaciones Toronto.
 * Define qué rutas son públicas y cuáles requieren un rol específico
 * (Administrador, Vendedor, Cliente o Bodeguero), el flujo de login/logout
 * por formulario y el algoritmo de cifrado de contraseñas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Servicio que carga los usuarios y sus roles para la autenticación (ver {@link UsuarioService}). */
    @Autowired
    private UsuarioService usuarioService;

    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    /**
     * Encoder de contraseñas basado en BCrypt, usado tanto al registrar
     * usuarios como al validar sus credenciales en el login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================================
    // CONFIGURACIÓN DE SEGURIDAD
    // ==========================================

    /**
     * Define las reglas de autorización por ruta, el login por formulario
     * (con manejo diferenciado de errores para usuarios inhabilitados) y el
     * logout de la aplicación.
     * <p>
     * Reglas de acceso: las rutas estáticas y de autenticación son públicas;
     * cada prefijo {@code /admin}, {@code /vendedor}, {@code /cliente} y
     * {@code /bodeguero} exige el authority de rol correspondiente; y
     * cualquier otra ruta exige, como mínimo, estar autenticado.
     *
     * @param http builder de configuración HTTP de Spring Security
     * @return la cadena de filtros de seguridad ya construida
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // Rutas públicas
                .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/images/**").permitAll()

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
    // Distingue un login fallido por credenciales incorrectas de uno
    // fallido porque el usuario fue inhabilitado (DisabledException
    // lanzada en UsuarioService), para mostrar un mensaje adecuado.
.failureHandler((request, response, exception) -> {
    String mensaje = exception.getMessage() != null ? exception.getMessage() : "";
    Throwable causa = exception.getCause();
    String mensajeCausa = causa != null && causa.getMessage() != null ? causa.getMessage() : "";
    
    if (mensaje.contains("inhabilitado") || mensajeCausa.contains("inhabilitado")
        || exception.getClass().getSimpleName().contains("Disabled")
        || (causa != null && causa.getClass().getSimpleName().contains("Disabled"))) {
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

    /**
     * Expone el {@link AuthenticationManager} configurado con
     * {@link UsuarioService} como fuente de usuarios y BCrypt como
     * mecanismo de verificación de contraseñas.
     *
     * @param http builder de configuración HTTP, usado para obtener el
     *             {@link AuthenticationManagerBuilder} compartido
     * @return el manager de autenticación ya construido
     */
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