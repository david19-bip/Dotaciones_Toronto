package com.dotacionestoronto.dotacionestoronto.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;

/**
 * Crea automáticamente una cuenta de administrador la primera vez que la
 * aplicación arranca, ya que el formulario público de registro (/registro)
 * ya no permite elegir el rol y siempre crea usuarios como Cliente.
 *
 * Si ya existe un usuario con el correo configurado abajo, no hace nada
 * (no lo duplica ni le resetea la contraseña en cada arranque).
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    // ==========================================
    // Credenciales del administrador por defecto.
    // Cámbialas aquí antes de entregar/desplegar el proyecto,
    // y una vez el admin inicie sesión, se recomienda cambiar
    // la contraseña desde /admin/perfil.
    // ==========================================
    private static final String ADMIN_NOMBRE = "Administrador";
    private static final String ADMIN_CORREO = "admin@dotacionestoronto.com";
    private static final String ADMIN_PASSWORD = "Admin123";
    private static final String ADMIN_DOCUMENTO = "0000000000";
    private static final String ADMIN_TELEFONO = "0000000000";
    private static final String ADMIN_DIRECCION = "Oficina principal";
    private static final Integer ROL_ADMIN = 1; // id_rol_fk = 1 ('Administrador')

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByCorreo(ADMIN_CORREO) != null) {
            return; // Ya existe, no se vuelve a crear ni se sobrescribe
        }

        Usuario admin = new Usuario();
        admin.setNombre(ADMIN_NOMBRE);
        admin.setDocumento(ADMIN_DOCUMENTO);
        admin.setCorreo(ADMIN_CORREO);
        admin.setTelefono(ADMIN_TELEFONO);
        admin.setDireccion(ADMIN_DIRECCION);
        admin.setContrasena(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setIdRol(ROL_ADMIN);
        admin.setEstado("Habilitado");

        usuarioRepository.save(admin);

        System.out.println("========================================================");
        System.out.println(" Usuario administrador creado automáticamente:");
        System.out.println(" Correo:      " + ADMIN_CORREO);
        System.out.println(" Contraseña:  " + ADMIN_PASSWORD);
        System.out.println(" (Cámbiala luego desde /admin/perfil)");
        System.out.println("========================================================");
    }
}