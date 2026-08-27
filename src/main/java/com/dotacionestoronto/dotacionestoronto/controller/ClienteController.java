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

import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    // ==========================================
    // PANEL PRINCIPAL DEL CLIENTE
    // ==========================================

    @GetMapping("/panel")
    public String panelCliente(Model model) {
        Usuario usuario = getUsuarioActual();
        Long idUsuario = usuario.getId();

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("pedidosPendientes", pedidoRepository.countByIdUsuarioAndEstado(idUsuario, "Pendiente"));
        model.addAttribute("pedidosProceso", pedidoRepository.countByIdUsuarioAndEstado(idUsuario, "En proceso"));
        model.addAttribute("pedidosEntregados", pedidoRepository.countByIdUsuarioAndEstado(idUsuario, "Entregado"));
        model.addAttribute("totalPedidos", pedidoRepository.countByIdUsuario(idUsuario));

        return "cliente/panel_cliente";
    }

    // ==========================================
    // PRODUCTOS
    // ==========================================

    @GetMapping("/productos")
    public String productos(Model model) {

        List<Producto> productos = productoRepository.findByInhabilitadoFalseOrderByIdDesc();

        model.addAttribute("productos", productos);
        model.addAttribute("nombre", getUsuarioActual().getNombre());

        return "cliente/productos_cliente";
    }

    // ==========================================
    // PEDIDOS
    // ==========================================

    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        Usuario usuario = getUsuarioActual();

        List<Pedido> pedidos =
                pedidoRepository.findByIdUsuarioAndOcultoFalseOrderByFechaPedidoDesc(usuario.getId());

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("nombre", usuario.getNombre());

        return "cliente/pedidos_cliente";
    }

    // ==========================================
    // DEVOLUCIONES
    // ==========================================

    @GetMapping("/devoluciones")
    public String devoluciones(Model model) {
        Usuario usuario = getUsuarioActual();

        List<Devolucion> devoluciones = devolucionRepository.findByUsuario(usuario.getId());

        model.addAttribute("devoluciones", devoluciones);
        model.addAttribute("nombre", usuario.getNombre());

        return "cliente/devoluciones_cliente";
    }

    // ==========================================
    // PERFIL
    // ==========================================

    @GetMapping("/perfil")
    public String perfil(Model model) {
        Usuario usuario = getUsuarioActual();

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());

        return "cliente/perfil_cliente";
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
            usuario.setContrasena(passwordEncoder.encode(password));
        }

        usuarioRepository.save(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("exito", "Perfil actualizado correctamente.");

        return "cliente/perfil_cliente";
    }

    // ==========================================
    // NOSOTROS + CONTÁCTANOS
    // ==========================================

    @GetMapping("/nosotros")
    public String verNosotros(Model model) {
        // Se usa para precargar el formulario de contacto con los datos reales
        // del cliente logueado en vez de dejarlo con datos quemados.
        Usuario usuario = getUsuarioActual();

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());

        return "cliente/nosotros_cliente";
    }

    @PostMapping("/nosotros/contacto")
    public String enviarContacto(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String mensaje,
            Model model) {

        // No existe una tabla para mensajes de contacto en la base de datos, así
        // que por ahora no hay dónde persistir esto. Si quieres guardarlos, crea
        // una tabla (p. ej. "mensaje_contacto") con su Entity/Repository y aquí
        // solo llamas al repository para guardar antes de retornar la vista.
        Usuario usuario = getUsuarioActual();

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("exito", "Tu mensaje fue enviado. Te contactaremos pronto.");

        return "cliente/nosotros_cliente";
    }
}