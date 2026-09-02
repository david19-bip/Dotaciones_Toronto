package com.dotacionestoronto.dotacionestoronto.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dotacionestoronto.dotacionestoronto.entity.DetallePedido;
import com.dotacionestoronto.dotacionestoronto.entity.DetalleVenta;
import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import com.dotacionestoronto.dotacionestoronto.repository.DetallePedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DetalleVentaRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.VentaRepository;

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
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

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

        List<DevolucionResumen> resumenes = new ArrayList<>();
        for (Devolucion d : devoluciones) {
            String pedidoRef = "N/D";
            String productoNombre = "Producto eliminado";

            DetalleVenta detalleVenta = detalleVentaRepository.findById(d.getIdDetalleVenta()).orElse(null);
            if (detalleVenta != null) {
                Venta venta = ventaRepository.findById(detalleVenta.getIdVenta()).orElse(null);
                if (venta != null && venta.getIdPedido() != null) {
                    pedidoRef = "PED-" + venta.getIdPedido();
                }
                Producto producto = productoRepository.findById(detalleVenta.getIdProducto()).orElse(null);
                if (producto != null) {
                    productoNombre = producto.getNombre();
                }
            }

            resumenes.add(new DevolucionResumen(
                    d.getId(), pedidoRef, productoNombre, d.getMotivo(), d.getFechaDevolucion(), d.getEstado()));
        }

        // Solo pedidos ya entregados pueden tener una devolución solicitada
        List<Pedido> pedidosEntregados =
                pedidoRepository.findByIdUsuarioAndEstado(usuario.getId(), "Entregado");

        model.addAttribute("devoluciones", resumenes);
        model.addAttribute("pedidosEntregados", pedidosEntregados);
        model.addAttribute("nombre", usuario.getNombre());

        return "cliente/devoluciones_cliente";
    }

    // Devuelve los productos de un pedido entregado, con la cantidad aún disponible para devolver
    @GetMapping("/devoluciones/productos/{idPedido}")
    @ResponseBody
    public List<Map<String, Object>> productosDePedido(@PathVariable Long idPedido) {
        Usuario usuario = getUsuarioActual();

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido == null || !pedido.getIdUsuario().equals(usuario.getId())) {
            return new ArrayList<>();
        }

        Venta venta = ventaRepository.findByIdPedido(idPedido);
        if (venta == null) {
            return new ArrayList<>();
        }

        List<DetalleVenta> detalles = detalleVentaRepository.findByIdVenta(venta.getId());
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (DetalleVenta detalle : detalles) {
            int yaDevuelta = devolucionRepository.findByIdDetalleVenta(detalle.getId()).stream()
                    .mapToInt(Devolucion::getCantidad)
                    .sum();
            int disponible = detalle.getCantidad() - yaDevuelta;

            if (disponible > 0) {
                Producto producto = productoRepository.findById(detalle.getIdProducto()).orElse(null);

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("idProducto", detalle.getIdProducto());
                item.put("nombre", producto != null ? producto.getNombre() : "Producto eliminado");
                item.put("cantidadComprada", disponible);
                resultado.add(item);
            }
        }

        return resultado;
    }

    @PostMapping("/devoluciones/crear")
    public String crearDevolucion(
            @RequestParam Long idPedido,
            @RequestParam Long idProducto,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam(required = false) String detalle,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = getUsuarioActual();

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido == null || !pedido.getIdUsuario().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("error", "Pedido no válido.");
            return "redirect:/cliente/devoluciones";
        }

        Venta venta = ventaRepository.findByIdPedido(idPedido);
        if (venta == null) {
            redirectAttributes.addFlashAttribute("error", "Este pedido aún no tiene una venta asociada.");
            return "redirect:/cliente/devoluciones";
        }

        DetalleVenta detalleVenta = detalleVentaRepository.findByIdVentaAndIdProducto(venta.getId(), idProducto);
        if (detalleVenta == null) {
            redirectAttributes.addFlashAttribute("error", "El producto seleccionado no pertenece a este pedido.");
            return "redirect:/cliente/devoluciones";
        }

        int yaDevuelta = devolucionRepository.findByIdDetalleVenta(detalleVenta.getId()).stream()
                .mapToInt(Devolucion::getCantidad)
                .sum();
        int disponible = detalleVenta.getCantidad() - yaDevuelta;

        if (cantidad <= 0 || cantidad > disponible) {
            redirectAttributes.addFlashAttribute("error",
                    "La cantidad a devolver supera lo disponible para este producto.");
            return "redirect:/cliente/devoluciones";
        }

        String motivoCompleto = (detalle != null && !detalle.isBlank())
                ? motivo + ": " + detalle.trim()
                : motivo;

        Devolucion nueva = new Devolucion();
        nueva.setIdDetalleVenta(detalleVenta.getId());
        nueva.setCantidad(cantidad);
        nueva.setMotivo(motivoCompleto);
        nueva.setFechaDevolucion(LocalDate.now());
        nueva.setEstado("Pendiente");
        devolucionRepository.save(nueva);

        redirectAttributes.addFlashAttribute("exito", "Tu solicitud de devolución fue enviada correctamente.");
        return "redirect:/cliente/devoluciones";
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
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String password,
            Model model) {

        Usuario usuario = getUsuarioActual();

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

        Usuario usuario = getUsuarioActual();

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("exito", "Tu mensaje fue enviado. Te contactaremos pronto.");

        return "cliente/nosotros_cliente";
    }

    // ==========================================
    // CLASES AUXILIARES INTERNAS PARA LAS VISTAS
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

    public static class DevolucionResumen {
        private Long id;
        private String pedidoRef;
        private String producto;
        private String motivo;
        private LocalDate fecha;
        private String estado;

        public DevolucionResumen(Long id, String pedidoRef, String producto, String motivo, LocalDate fecha, String estado) {
            this.id = id;
            this.pedidoRef = pedidoRef;
            this.producto = producto;
            this.motivo = motivo;
            this.fecha = fecha;
            this.estado = estado;
        }

        public Long getId() { return id; }
        public String getPedidoRef() { return pedidoRef; }
        public String getProducto() { return producto; }
        public String getMotivo() { return motivo; }
        public LocalDate getFecha() { return fecha; }
        public String getEstado() { return estado; }
    }
}