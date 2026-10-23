package com.dotacionestoronto.dotacionestoronto.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
import com.dotacionestoronto.dotacionestoronto.service.ReporteVentasExcelService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.FacturacionService;

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
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ReporteVentasExcelService reporteVentasExcelService;

    // Módulo de facturación electrónica (DIAN)
    @Autowired
    private FacturacionService facturacionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    // ==========================================
    // PANEL PRINCIPAL DEL VENDEDOR
    // ==========================================

    @GetMapping("/panel")
    public String panelVendedor(Model model) {
        Usuario usuario = getUsuarioActual();

        model.addAttribute("nombre", usuario.getNombre());

        long pedidosPendientes = pedidoRepository.countByEstadoAndOcultoFalse("Pendiente");

        LocalDateTime ahora = LocalDateTime.now();
        long ventasDelMes = ventaRepository.findAllByOrderByFechaVentaDesc().stream()
                .filter(v -> v.getFechaVenta() != null
                        && v.getFechaVenta().getMonthValue() == ahora.getMonthValue()
                        && v.getFechaVenta().getYear() == ahora.getYear())
                .count();

        long devolucionesPendientes = devolucionRepository.findByEstado("Pendiente").size();

        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("ventasDelMes", ventasDelMes);
        model.addAttribute("devolucionesPendientes", devolucionesPendientes);

        return "vendedor/panel_vendedor";
    }

    // ==========================================
    // PEDIDOS
    // ==========================================

    @GetMapping("/pedidos")
    public String pedidos(
            @RequestParam(required = false) String estado,
            Model model) {

        List<Pedido> pedidos;

        if (estado != null && !estado.isEmpty()) {
            pedidos = pedidoRepository.findByEstadoAndOcultoFalse(estado);
        } else {
            pedidos = pedidoRepository.findByOcultoFalseOrderByFechaPedidoDesc();
        }

        List<PedidoVista> pedidosVista = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            PedidoVista pv = new PedidoVista();
            pv.pedido = pedido;

            Usuario cliente = usuarioRepository.findById(pedido.getIdUsuario()).orElse(null);
            if (cliente != null) {
                pv.clienteNombre = cliente.getNombre();
                pv.clienteCorreo = cliente.getCorreo();
                pv.clienteTelefono = cliente.getTelefono();
                pv.clienteDireccion = cliente.getDireccion();
            }

            List<DetallePedido> detalles = detallePedidoRepository.findByIdPedido(pedido.getId());
            BigDecimal total = BigDecimal.ZERO;
            for (DetallePedido detalle : detalles) {
                ItemPedido item = new ItemPedido();
                Producto producto = productoRepository.findById(detalle.getIdProducto()).orElse(null);
                if (producto != null) {
                    item.nombreProducto = producto.getNombre();
                    item.talla = producto.getTalla();
                    item.color = producto.getColor();
                    item.imagen = producto.getImagen();
                }
                item.cantidad = detalle.getCantidad();
                item.precioUnitario = detalle.getPrecioUnitario();
                if (detalle.getPrecioUnitario() != null && detalle.getCantidad() != null) {
                    item.subtotal = detalle.getPrecioUnitario()
                            .multiply(BigDecimal.valueOf(detalle.getCantidad()));
                    total = total.add(item.subtotal);
                }
                pv.items.add(item);
            }
            pv.total = total;

            pedidosVista.add(pv);
        }

        model.addAttribute("pedidos", pedidosVista);
        model.addAttribute("estadoActivo", estado);
        model.addAttribute("nombre", getUsuarioActual().getNombre());

        return "vendedor/pedidos_vendedor";
    }

    public static class PedidoVista {
        public Pedido pedido;
        public String clienteNombre;
        public String clienteCorreo;
        public String clienteTelefono;
        public String clienteDireccion;
        public List<ItemPedido> items = new ArrayList<>();
        public BigDecimal total;
    }

    public static class ItemPedido {
        public String nombreProducto;
        public String talla;
        public String color;
        public String imagen;
        public Integer cantidad;
        public BigDecimal precioUnitario;
        public BigDecimal subtotal;
    }

    @PostMapping("/pedidos/cambiarEstado")
    public String cambiarEstado(
            @RequestParam Long idPedido,
            @RequestParam String estado) {

        Pedido pedido =
                pedidoRepository.findById(idPedido).orElse(null);

        if (pedido != null) {
            pedido.setEstado(estado);

            if ("Entregado".equals(estado)) {
                Venta ventaGenerada = generarVentaDesdePedido(pedido);
                pedido.setOculto(true);

                // Facturación automática: se emite la factura electrónica de venta
                // (el servicio nunca lanza excepciones, deja la factura en ERROR si falla)
                if (ventaGenerada != null && ventaGenerada.getId() != null) {
                    facturacionService.emitirDesdeVenta(ventaGenerada.getId());
                }
            }

            pedidoRepository.save(pedido);
        }

        return "redirect:/vendedor/pedidos";
    }

    private Venta generarVentaDesdePedido(Pedido pedido) {

        Venta ventaExistente = ventaRepository.findByIdPedido(pedido.getId());
        if (ventaExistente != null) {
            return ventaExistente;
        }

        List<DetallePedido> detallesPedido = detallePedidoRepository.findByIdPedido(pedido.getId());

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido dp : detallesPedido) {
            if (dp.getPrecioUnitario() != null && dp.getCantidad() != null) {
                total = total.add(dp.getPrecioUnitario().multiply(BigDecimal.valueOf(dp.getCantidad())));
            }
        }

        Venta nuevaVenta = new Venta();
        nuevaVenta.setIdUsuario(pedido.getIdUsuario());
        nuevaVenta.setIdPedido(pedido.getId());
        nuevaVenta.setFechaVenta(LocalDateTime.now());
        nuevaVenta.setTotalPagado(total.doubleValue());
        nuevaVenta = ventaRepository.save(nuevaVenta);

        for (DetallePedido dp : detallesPedido) {
            DetalleVenta dv = new DetalleVenta();
            dv.setIdVenta(nuevaVenta.getId());
            dv.setIdProducto(dp.getIdProducto());
            dv.setCantidad(dp.getCantidad());
            dv.setPrecioUnitario(dp.getPrecioUnitario());
            detalleVentaRepository.save(dv);
        }

        return nuevaVenta;
    }

    // ==========================================
    // VENTAS
    // ==========================================

    @GetMapping("/ventas")
    public String ventas(Model model) {

        List<Venta> ventas =
                ventaRepository.findAllByOrderByFechaVentaDesc();

        List<VentaVista> ventasVista = new ArrayList<>();

        for (Venta venta : ventas) {
            VentaVista vv = new VentaVista();
            vv.venta = venta;

            Usuario cliente = usuarioRepository.findById(venta.getIdUsuario()).orElse(null);
            if (cliente != null) {
                vv.clienteNombre = cliente.getNombre();
                vv.clienteCorreo = cliente.getCorreo();
                vv.clienteTelefono = cliente.getTelefono();
                vv.clienteDireccion = cliente.getDireccion();
            }

            Pedido pedido = pedidoRepository.findById(venta.getIdPedido()).orElse(null);
            if (pedido != null) {
                vv.pedidoEstado = pedido.getEstado();
            }

            List<DetalleVenta> detalles = detalleVentaRepository.findByIdVenta(venta.getId());
            for (DetalleVenta detalle : detalles) {
                ItemVenta item = new ItemVenta();
                Producto producto = productoRepository.findById(detalle.getIdProducto()).orElse(null);
                if (producto != null) {
                    item.nombreProducto = producto.getNombre();
                    item.talla = producto.getTalla();
                    item.color = producto.getColor();
                    item.imagen = producto.getImagen();
                }
                item.cantidad = detalle.getCantidad();
                item.precioUnitario = detalle.getPrecioUnitario();
                if (detalle.getPrecioUnitario() != null && detalle.getCantidad() != null) {
                    item.subtotal = detalle.getPrecioUnitario()
                            .multiply(BigDecimal.valueOf(detalle.getCantidad()));
                }
                vv.items.add(item);
            }

            ventasVista.add(vv);
        }

        model.addAttribute("ventas", ventasVista);
        model.addAttribute("nombre", getUsuarioActual().getNombre());

        return "vendedor/ventas_vendedor";
    }

    @GetMapping("/ventas/reporte/excel")
    public ResponseEntity<byte[]> reporteVentasExcel() {
        byte[] excel = reporteVentasExcelService.generarReporte(ventaRepository.findAllByOrderByFechaVentaDesc());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte_ventas.xlsx")
                .build());
        return ResponseEntity.ok().headers(headers).body(excel);
    }

    public static class VentaVista {
        public Venta venta;
        public String clienteNombre;
        public String clienteCorreo;
        public String clienteTelefono;
        public String clienteDireccion;
        public String pedidoEstado;
        public List<ItemVenta> items = new ArrayList<>();
    }

    public static class ItemVenta {
        public String nombreProducto;
        public String talla;
        public String color;
        public String imagen;
        public Integer cantidad;
        public BigDecimal precioUnitario;
        public BigDecimal subtotal;
    }

    // ==========================================
    // DEVOLUCIONES
    // ==========================================

    @GetMapping("/devoluciones")
    public String devoluciones(
            @RequestParam(required = false) String estado,
            Model model) {

        List<Devolucion> devoluciones;

        if (estado != null && !estado.isEmpty()) {
            devoluciones = devolucionRepository.findByEstado(estado);
        } else {
            devoluciones = devolucionRepository.findAllByOrderByFechaDevolucionDesc();
        }

        // findByEstado no trae orden garantizado, así que se ordena aquí
        // por fecha descendente en ambos casos.
        devoluciones.sort((a, b) -> {
            if (a.getFechaDevolucion() == null || b.getFechaDevolucion() == null) return 0;
            return b.getFechaDevolucion().compareTo(a.getFechaDevolucion());
        });

        List<DevolucionVista> devolucionesVista = new ArrayList<>();

        for (Devolucion d : devoluciones) {
            DevolucionVista dv = new DevolucionVista();
            dv.devolucion = d;

            DetalleVenta detalleVenta = detalleVentaRepository.findById(d.getIdDetalleVenta()).orElse(null);
            if (detalleVenta != null) {
                Producto producto = productoRepository.findById(detalleVenta.getIdProducto()).orElse(null);
                dv.productoNombre = producto != null ? producto.getNombre() : "Producto eliminado";

                Venta venta = ventaRepository.findById(detalleVenta.getIdVenta()).orElse(null);
                if (venta != null) {
                    dv.pedidoRef = venta.getIdPedido() != null ? "PED-" + venta.getIdPedido() : "N/D";

                    Usuario cliente = usuarioRepository.findById(venta.getIdUsuario()).orElse(null);
                    if (cliente != null) {
                        dv.clienteNombre = cliente.getNombre();
                        dv.clienteCorreo = cliente.getCorreo();
                        dv.clienteTelefono = cliente.getTelefono();
                        dv.clienteDireccion = cliente.getDireccion();
                    }
                }
            }

            devolucionesVista.add(dv);
        }

        model.addAttribute("devoluciones", devolucionesVista);
        model.addAttribute("estadoActivo", estado);
        model.addAttribute("nombre", getUsuarioActual().getNombre());

        return "vendedor/devoluciones_vendedor";
    }

    public static class DevolucionVista {
        public Devolucion devolucion;
        public String productoNombre;
        public String pedidoRef;
        public String clienteNombre;
        public String clienteCorreo;
        public String clienteTelefono;
        public String clienteDireccion;
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
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String password,
            Model model) {

        Usuario usuario = getUsuarioActual();

        String correoLimpio = correo != null ? correo.trim() : usuario.getCorreo();
        String telefonoLimpio = telefono != null ? telefono.trim() : usuario.getTelefono();
        String direccionLimpia = direccion != null ? direccion.trim() : usuario.getDireccion();

        // ===== Validaciones de servidor =====

        if (!correoLimpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "El correo electrónico no tiene un formato válido.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "vendedor/perfil_vendedor";
        }

        if (!telefonoLimpio.matches("^[0-9]{7,15}$")) {
            model.addAttribute("error", "El teléfono debe contener solo números, entre 7 y 15 dígitos.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "vendedor/perfil_vendedor";
        }

        if (direccionLimpia == null || direccionLimpia.trim().length() < 5) {
            model.addAttribute("error", "La dirección debe tener al menos 5 caracteres.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "vendedor/perfil_vendedor";
        }

        if (password != null && !password.isEmpty() && password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "vendedor/perfil_vendedor";
        }

        boolean correoCambio = !correoLimpio.equals(usuario.getCorreo());

        if (correoCambio) {
            Usuario existente = usuarioRepository.findByCorreo(correoLimpio);
            if (existente != null && !existente.getId().equals(usuario.getId())) {
                model.addAttribute("error", "Ese correo ya está en uso por otro usuario.");
                model.addAttribute("usuario", usuario);
                model.addAttribute("nombre", usuario.getNombre());
                return "vendedor/perfil_vendedor";
            }
        }

        usuario.setTelefono(telefonoLimpio);
        usuario.setDireccion(direccionLimpia);

        if (password != null && !password.isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(password));
        }

        if (correoCambio) {
            usuario.setCorreo(correoLimpio);
            usuarioRepository.save(usuario);

            Authentication authActual = SecurityContextHolder.getContext().getAuthentication();
            Authentication nuevaAuth = new UsernamePasswordAuthenticationToken(
                    usuario.getCorreo(),
                    authActual.getCredentials(),
                    authActual.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(nuevaAuth);
        } else {
            usuarioRepository.save(usuario);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("exito", "Perfil actualizado correctamente.");

        return "vendedor/perfil_vendedor";
    }
}