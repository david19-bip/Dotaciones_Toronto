package com.dotacionestoronto.dotacionestoronto.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import com.dotacionestoronto.dotacionestoronto.entity.Inventario;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.InventarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;

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

    // --- NUEVO: Mostrar formulario de edición ---
    @GetMapping("/usuarios/editar")
    public String editarUsuarioForm(@RequestParam("id") Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return "redirect:/admin/usuarios";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/editar_usuario_admin";
    }

    // --- NUEVO: Procesar actualización de usuario ---
    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(
            @RequestParam Long idUsuario,
            @RequestParam String nombre,
            @RequestParam String documento,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String password,
            @RequestParam Integer rol) {

        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario != null) {
            usuario.setNombre(nombre);
            usuario.setDocumento(documento);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            usuario.setIdRol(rol);

            // Si se ingresó una nueva contraseña, la encripta y actualiza
            if (password != null && !password.trim().isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(password));
            }

            usuarioRepository.save(usuario);
        }

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
            @RequestParam String color,
            @RequestParam(required = false) MultipartFile imagen) throws IOException {

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setTalla(talla);
        producto.setColor(color);
        producto.setEstado("Disponible");
        producto.setInhabilitado(false);

        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
            Path ruta = Paths.get("uploads/productos/" + nombreArchivo);

            // TEMPORAL: para ver dónde se está guardando realmente
            System.out.println(">>> Guardando imagen en: " + ruta.toAbsolutePath());

            Files.createDirectories(ruta.getParent());
            Files.write(ruta, imagen.getBytes());
            producto.setImagen("images/productos/" + nombreArchivo);
        }

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

    // ===== INVENTARIO =====
    @GetMapping("/inventario")
    public String inventario(Model model) {
        model.addAttribute("inventarios", inventarioRepository.findAll());
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/inventario_admin";
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

        return "redirect:/admin/inventario";
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
        return "redirect:/admin/inventario";
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

    // ===== PERFIL =====
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Usuario usuario = getUsuarioActual();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        return "admin/perfil_admin";
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
        if (!nombre.isEmpty()) usuario.setNombre(nombre);
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
        return "admin/perfil_admin";
    }
}