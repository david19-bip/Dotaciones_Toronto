package com.dotacionestoronto.dotacionestoronto.controller;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de las páginas públicas y del proceso de autenticación:
 * página de inicio, formulario de login y registro de nuevos usuarios.
 * El login en sí (procesamiento de credenciales) lo maneja Spring Security
 * (ver {@code SecurityConfig}); este controlador solo expone los formularios
 * y la creación de cuentas nuevas.
 */
@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Muestra el formulario de inicio de sesión. */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Muestra el formulario de registro de un nuevo usuario. */
    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    /**
     * Procesa el registro de un nuevo usuario: valida que ningún campo
     * obligatorio esté vacío, que el correo tenga un formato válido y que
     * no exista ya un usuario con el mismo correo o documento. Si todo es
     * correcto, cifra la contraseña con BCrypt y guarda el usuario con
     * estado "Habilitado".
     *
     * @param nombre    nombre completo del usuario
     * @param documento número de documento de identidad (debe ser único)
     * @param correo    correo electrónico (debe ser único, usado como login)
     * @param telefono  teléfono de contacto
     * @param direccion dirección de residencia
     * @param password  contraseña en texto plano (se cifra antes de guardar)
     * @param rol       id de rol asignado al usuario (1=Admin, 2=Cliente, 3=Vendedor, 4=Bodeguero)
     * @param model     modelo de la vista, usado para mostrar mensajes de error o éxito
     * @return la vista de registro (si hay error) o un redirect al login (si fue exitoso)
     */
    @PostMapping("/registro")
    public String registrar(
            @RequestParam String nombre,
            @RequestParam String documento,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam String password,
            @RequestParam Integer rol,
            Model model) {

        if (nombre.isEmpty() || documento.isEmpty() || correo.isEmpty() ||
            telefono.isEmpty() || direccion.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Todos los campos son obligatorios.");
            return "registro";
        }

        if (!correo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            model.addAttribute("error", "El correo no tiene un formato válido.");
            return "registro";
        }

        if (usuarioRepository.findByCorreo(correo) != null) {
            model.addAttribute("error", "Ya existe un usuario con ese correo.");
            return "registro";
        }

        if (usuarioRepository.findByDocumento(documento) != null) {
            model.addAttribute("error", "Ya existe un usuario con ese documento.");
            return "registro";
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setDocumento(documento);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);
        usuario.setContrasena(passwordEncoder.encode(password));
        usuario.setIdRol(rol);
        usuario.setEstado("Habilitado");

        usuarioRepository.save(usuario);

        model.addAttribute("exito", "Usuario registrado correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login?registro";
    }
    
    /** Muestra la página de inicio pública de la aplicación. */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}