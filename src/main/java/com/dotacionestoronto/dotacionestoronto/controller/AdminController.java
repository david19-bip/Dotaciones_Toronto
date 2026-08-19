package com.dotacionestoronto.dotacionestoronto.controller;

import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    // ===== PANEL =====
    @GetMapping("/panel")
    public String panel(Model model) {
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalPedidos", pedidoRepository.count());
        model.addAttribute("totalDevoluciones", devolucionRepository.count());
        model.addAttribute("totalProductos", productoRepository.count());
        return "admin/panel_admin";
    }

    // ===== USUARIOS =====
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/usuarios_admin";
    }

    @PostMapping("/usuarios/crear")
    public String crearUsuario(
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
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("nombre", getUsuarioActual().getNombre());
            return "admin/usuarios_admin";
        }

        if (usuarioRepository.findByDocumento(documento) != null) {
            model.addAttribute("error", "Ya existe un usuario con ese documento.");
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("nombre", getUsuarioActual().getNombre());
            return "admin/usuarios_admin";
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

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/toggleEstado")
    public String toggleEstadoUsuario(@RequestParam Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario != null) {
            usuario.setEstado(usuario.getEstado().equals("Habilitado") ? "Inhabilitado" : "Habilitado");
            usuarioRepository.save(usuario);
        }
        return "redirect:/admin/usuarios";
    }

    // ===== PRODUCTOS =====
    @GetMapping("/productos")
    public String productos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/productos_admin";
    }

    @PostMapping("/productos/crear")
    public String crearProducto(
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam String talla,
            @RequestParam String color) {

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setTalla(talla);
        producto.setColor(color);
        producto.setEstado("Disponible");
        producto.setInhabilitado(false);
        productoRepository.save(producto);

        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/toggleEstado")
    public String toggleEstadoProducto(@RequestParam Long idProducto) {
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto != null) {
            producto.setEstado(producto.getEstado().equals("Disponible") ? "Agotado" : "Disponible");
            productoRepository.save(producto);
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/eliminar")
    public String eliminarProducto(@RequestParam Long idProducto) {
        productoRepository.deleteById(idProducto);
        return "redirect:/admin/productos";
    }

    // ===== PEDIDOS =====
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        List<Pedido> pedidos = pedidoRepository.findByOcultoFalseOrderByFechaPedidoDesc();
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/pedidos_admin";
    }

    @PostMapping("/pedidos/cambiarEstado")
    public String cambiarEstadoPedido(@RequestParam Long idPedido, @RequestParam String estado) {
        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido != null) {
            pedido.setEstado(estado);
            pedidoRepository.save(pedido);
        }
        return "redirect:/admin/pedidos";
    }

    @PostMapping("/pedidos/ocultar")
    public String ocultarPedido(@RequestParam Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido != null) {
            pedido.setOculto(true);
            pedidoRepository.save(pedido);
        }
        return "redirect:/admin/pedidos";
    }

    // ===== DEVOLUCIONES =====
    @GetMapping("/devoluciones")
    public String devoluciones(Model model) {
        List<Devolucion> devoluciones = devolucionRepository.findAllByOrderByFechaDevolucionDesc();
        model.addAttribute("devoluciones", devoluciones);
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/devoluciones_admin";
    }

    @PostMapping("/devoluciones/cambiarEstado")
    public String cambiarEstadoDevolucion(@RequestParam Long idDevolucion, @RequestParam String estado) {
        Devolucion devolucion = devolucionRepository.findById(idDevolucion).orElse(null);
        if (devolucion != null) {
            devolucion.setEstado(estado);
            devolucionRepository.save(devolucion);
        }
        return "redirect:/admin/devoluciones";
    }
}