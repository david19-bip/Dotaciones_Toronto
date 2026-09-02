package com.dotacionestoronto.dotacionestoronto.controller;
import com.dotacionestoronto.dotacionestoronto.entity.Inventario;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.InventarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/bodeguero")
public class BodegueroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    // ===== PANEL =====
    @GetMapping("/panel")
    public String panel(Model model) {
        List<Inventario> inventarios = inventarioRepository.findAll();
        long totalStockBajo = inventarios.stream()
                .filter(inv -> inv.getCantidadActual() != null
                        && inv.getStockMinimo() != null
                        && inv.getCantidadActual() <= inv.getStockMinimo())
                .count();

        model.addAttribute("nombre", getUsuarioActual().getNombre());
        model.addAttribute("totalInventario", inventarios.size());
        model.addAttribute("totalStockBajo", totalStockBajo);
        model.addAttribute("totalProductos", productoRepository.count());
        return "bodeguero/panel_bodeguero";
    }

    // ===== INVENTARIO =====
    @GetMapping("/inventario")
    public String inventario(Model model) {
        model.addAttribute("inventarios", inventarioRepository.findAll());
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "bodeguero/inventario_bodeguero";
    }

    @PostMapping("/inventario/crear")
    public String crearInventario(
            @RequestParam Long idProducto,
            @RequestParam Integer cantidadActual,
            @RequestParam Integer stockMinimo) {

        Inventario inventario = new Inventario();
        inventario.setIdProducto(idProducto);
        inventario.setCantidadActual(cantidadActual);
        inventario.setStockMinimo(stockMinimo);
        inventario.setInhabilitado(false);
        inventarioRepository.save(inventario);

        return "redirect:/bodeguero/inventario";
    }

    @PostMapping("/inventario/actualizar")
    public String actualizarInventario(
            @RequestParam Long idInventario,
            @RequestParam Integer cantidadActual) {

        Inventario inventario = inventarioRepository.findById(idInventario).orElse(null);
        if (inventario != null) {
            inventario.setCantidadActual(cantidadActual);
            inventarioRepository.save(inventario);
        }
        return "redirect:/bodeguero/inventario";
    }

    // ===== PERFIL =====
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Usuario usuario = getUsuarioActual();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        return "bodeguero/perfil_bodeguero";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String password,
            Model model) {

        Usuario usuario = getUsuarioActual();
        if (!correo.isEmpty()) usuario.setCorreo(correo);
        if (!telefono.isEmpty()) usuario.setTelefono(telefono);
        if (!direccion.isEmpty()) usuario.setDireccion(direccion);
        if (password != null && !password.isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(password));
        }
        usuarioRepository.save(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("exito", "Perfil actualizado correctamente.");
        return "bodeguero/perfil_bodeguero";
    }
}