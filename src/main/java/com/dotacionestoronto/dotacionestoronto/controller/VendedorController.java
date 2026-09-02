package com.dotacionestoronto.dotacionestoronto.controller;

import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import com.dotacionestoronto.dotacionestoronto.entity.DetallePedido;
import com.dotacionestoronto.dotacionestoronto.entity.DetalleVenta;
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
import com.dotacionestoronto.dotacionestoronto.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

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

    // DTOs para mostrar el detalle completo de cada pedido
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

            // Cuando el pedido pasa a "Entregado" se convierte en venta
            // y desaparece del listado de pedidos.
            if ("Entregado".equals(estado)) {
                generarVentaDesdePedido(pedido);
                pedido.setOculto(true);
            }

            pedidoRepository.save(pedido);
        }

        return "redirect:/vendedor/pedidos";
    }

    /**
     * Crea la venta y su detalle a partir de un pedido entregado,
     * solo si aún no existe una venta registrada para ese pedido.
     */
    private void generarVentaDesdePedido(Pedido pedido) {

        Venta ventaExistente = ventaRepository.findByIdPedido(pedido.getId());
        if (ventaExistente != null) {
            return; // Ya se generó la venta anteriormente, no se duplica
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

    // DTOs para mostrar el detalle completo de cada venta
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
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String password,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {

        Usuario usuario = getUsuarioActual();
        boolean correoCambio = false;

        if (correo != null && !correo.isEmpty() && !correo.equals(usuario.getCorreo())) {
            usuario.setCorreo(correo);
            correoCambio = true;
        }

        if (telefono != null && !telefono.isEmpty()) {
            usuario.setTelefono(telefono);
        }

        if (direccion != null && !direccion.isEmpty()) {
            usuario.setDireccion(direccion);
        }

        if (password != null && !password.isEmpty()) {
            usuario.setContrasena(
                    passwordEncoder.encode(password)
            );
        }

        usuarioRepository.save(usuario);

        // Si el correo cambió, el "username" que Spring Security tiene guardado
        // en la sesión (auth.getName()) queda desactualizado. Hay que
        // re-autenticar la sesión actual con el nuevo correo para que
        // getUsuarioActual() lo siga encontrando en las siguientes peticiones.
        if (correoCambio) {
            UserDetails userDetails = usuarioService.loadUserByUsername(usuario.getCorreo());

            UsernamePasswordAuthenticationToken nuevaAuth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getPassword(),
                            userDetails.getAuthorities()
                    );

            SecurityContext nuevoContexto = SecurityContextHolder.createEmptyContext();
            nuevoContexto.setAuthentication(nuevaAuth);
            SecurityContextHolder.setContext(nuevoContexto);

            // Persistir el nuevo contexto en la sesión HTTP
            securityContextRepository.saveContext(nuevoContexto, request, response);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute(
                "exito",
                "Perfil actualizado correctamente."
        );

        return "vendedor/perfil_vendedor";
    }
}