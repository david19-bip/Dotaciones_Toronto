package com.dotacionestoronto.dotacionestoronto.controller;

import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/vendedor")
public class VendedorController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    // ==========================================
    // PANEL PRINCIPAL DEL VENDEDOR
    // ==========================================

    @GetMapping("/panel_vendedor")
    public String panelVendedor(Model model) {
        Usuario usuario = getUsuarioActual();

        model.addAttribute("nombre", usuario.getNombre());

        return "vendedor/panel_vendedor";
    }

    // ==========================================
    // PEDIDOS
    // ==========================================

    @GetMapping("/pedidos")
    public String pedidos(Model model) {

        List<Pedido> pedidos =
                pedidoRepository.findByOcultoFalseOrderByFechaPedidoDesc();

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("nombre", getUsuarioActual().getNombre());

        return "vendedor/pedidos_vendedor";
    }

    @PostMapping("/pedidos/cambiarEstado")
    public String cambiarEstado(
            @RequestParam Long idPedido,
            @RequestParam String estado) {

        Pedido pedido =
                pedidoRepository.findById(idPedido).orElse(null);

        if (pedido != null) {
            pedido.setEstado(estado);
            pedidoRepository.save(pedido);
        }

        return "redirect:/vendedor/pedidos";
    }

    // ==========================================
    // VENTAS
    // ==========================================

    @GetMapping("/ventas")
    public String ventas(Model model) {

        List<Venta> ventas =
                ventaRepository.findAllByOrderByFechaVentaDesc();

        model.addAttribute("ventas", ventas);
        model.addAttribute("nombre", getUsuarioActual().getNombre());

        return "vendedor/ventas_vendedor";
    }

    // ==========================================
    // DEVOLUCIONES
    // ==========================================

    @GetMapping("/devoluciones")
    public String devoluciones(Model model) {

        List<Devolucion> devoluciones =
                devolucionRepository.findAllByOrderByFechaDevolucionDesc();

        model.addAttribute("devoluciones", devoluciones);
        model.addAttribute("nombre", getUsuarioActual().getNombre());

        return "vendedor/devoluciones_vendedor";
    }

    @PostMapping("/devoluciones/cambiarEstado")
    public String cambiarEstadoDevolucion(
            @RequestParam Long idDevolucion,
            @RequestParam String estado) {

        Devolucion devolucion =
                devolucionRepository.findById(idDevolucion).orElse(null);

        if (devolucion != null) {
            devolucion.setEstado(estado);
            devolucionRepository.save(devolucion);
        }

        return "redirect:/vendedor/devoluciones";
    }

    // ==========================================
    // PERFIL
    // ==========================================

    @GetMapping("/perfil")
    public String perfil(Model model) {

        Usuario usuario = getUsuarioActual();

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());

        return "vendedor/perfil_vendedor";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String password,
            Model model) {

        Usuario usuario = getUsuarioActual();

        if (!nombre.isEmpty()) {
            usuario.setNombre(nombre);
        }

        if (!correo.isEmpty()) {
            usuario.setCorreo(correo);
        }

        if (!telefono.isEmpty()) {
            usuario.setTelefono(telefono);
        }

        if (!direccion.isEmpty()) {
            usuario.setDireccion(direccion);
        }

        if (password != null && !password.isEmpty()) {
            usuario.setContrasena(
                    passwordEncoder.encode(password)
            );
        }

        usuarioRepository.save(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute(
                "exito",
                "Perfil actualizado correctamente."
        );

        return "vendedor/perfil_vendedor";
    }
}