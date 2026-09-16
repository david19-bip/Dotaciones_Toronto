package com.dotacionestoronto.dotacionestoronto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;

@Controller
public class UsuarioController {

    // Rol fijo con el que se registra cualquier usuario que llegue por el
    // formulario público. Coincide con el id_rol_fk = 2 ('Cliente') de la
    // tabla `rol`. Ya no se recibe ni se confía en un valor de rol enviado
    // desde el formulario.
    private static final Integer ROL_CLIENTE_POR_DEFECTO = 2;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @RequestParam String nombre,
            @RequestParam String documento,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam String password,
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
        usuario.setIdRol(ROL_CLIENTE_POR_DEFECTO);
        usuario.setEstado("Habilitado");

        usuarioRepository.save(usuario);

        model.addAttribute("exito", "Usuario registrado correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login?registro";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}