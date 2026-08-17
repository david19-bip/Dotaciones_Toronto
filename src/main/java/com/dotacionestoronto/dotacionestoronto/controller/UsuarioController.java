package com.dotacionestoronto.dotacionestoronto.controller;

import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UsuarioController {

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
            @RequestParam Integer rol,
            Model model) {

        if (usuarioRepository.findByCorreo(correo) != null) {
            model.addAttribute("error", "Ya existe un usuario con ese correo.");
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
        return "registro";
    }

    @GetMapping("/admin/panel")
    public String panelAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("nombre", usuario.getNombre());
        return "admin/panel_admin";
    }

    @GetMapping("/vendedor/panel")
    public String panelVendedor(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("nombre", usuario.getNombre());
        return "vendedor/panel_vendedor";
    }

    @GetMapping("/cliente/panel")
    public String panelCliente(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("nombre", usuario.getNombre());
        return "cliente/panel_cliente";
    }

    @GetMapping("/bodeguero/panel")
    public String panelBodeguero(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("nombre", usuario.getNombre());
        return "bodeguero/panel_bodeguero";
    }
}