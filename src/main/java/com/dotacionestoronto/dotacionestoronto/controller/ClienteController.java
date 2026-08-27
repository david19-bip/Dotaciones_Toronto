package com.dotacionestoronto.dotacionestoronto.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dotacionestoronto.dotacionestoronto.entity.DetallePedido;
import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.DetallePedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;

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
    private DetallePedidoRepository detallePedidoRepository;

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

        List<PedidoResumen> resumenes = new ArrayList<>();
        for (Pedido p : pedidos) {
            List<DetallePedido> detalles = detallePedidoRepository.findByIdPedido(p.getId());

            int cantidadTotal = detalles.stream()
                    .mapToInt(DetallePedido::getCantidad)
                    .sum();

            BigDecimal total = detalles.stream()
                    .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resumenes.add(new PedidoResumen(p.getId(), p.getFechaPedido(), p.getEstado(), cantidadTotal, total));
        }

        model.addAttribute("pedidos", resumenes);
        model.addAttribute("nombre", usuario.getNombre());

        return "cliente/pedidos_cliente";
    }

    @GetMapping("/pedidos/{id}/detalle")
    @ResponseBody
    public List<Map<String, Object>> detallePedido(@PathVariable Long id) {
        Usuario usuario = getUsuarioActual();

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Evita que un cliente vea el detalle de un pedido que no es suyo
        if (!pedido.getIdUsuario().equals(usuario.getId())) {
            throw new RuntimeException("No autorizado para ver este pedido");
        }

        List<DetallePedido> detalles = detallePedidoRepository.findByIdPedido(id);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (DetallePedido d : detalles) {
            Producto producto = productoRepository.findById(d.getIdProducto()).orElse(null);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("producto", producto != null ? producto.getNombre() : "Producto eliminado");
            item.put("cantidad", d.getCantidad());
            item.put("precioUnitario", d.getPrecioUnitario());
            item.put("subtotal", d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())));
            resultado.add(item);
        }

        return resultado;
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

    // ==========================================
    // CLASE AUXILIAR: resumen de pedido para la vista
    // ==========================================
    public static class PedidoResumen {
        private Long id;
        private LocalDateTime fecha;
        private String estado;
        private Integer cantidadProductos;
        private BigDecimal total;

        public PedidoResumen(Long id, LocalDateTime fecha, String estado, Integer cantidadProductos, BigDecimal total) {
            this.id = id;
            this.fecha = fecha;
            this.estado = estado;
            this.cantidadProductos = cantidadProductos;
            this.total = total;
        }

        public Long getId() { return id; }
        public LocalDateTime getFecha() { return fecha; }
        public String getEstado() { return estado; }
        public Integer getCantidadProductos() { return cantidadProductos; }
        public BigDecimal getTotal() { return total; }
    }
}