package com.dotacionestoronto.dotacionestoronto.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dotacionestoronto.dotacionestoronto.entity.DetallePedido;
import com.dotacionestoronto.dotacionestoronto.entity.DetalleVenta;
import com.dotacionestoronto.dotacionestoronto.entity.Devolucion;
import com.dotacionestoronto.dotacionestoronto.entity.Inventario;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import com.dotacionestoronto.dotacionestoronto.repository.DetallePedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DetalleVentaRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.InventarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.VentaRepository;
import com.dotacionestoronto.dotacionestoronto.service.ReporteProductosExcelService;
import com.dotacionestoronto.dotacionestoronto.service.ReporteProductosService;
import com.dotacionestoronto.dotacionestoronto.service.ReporteUsuariosExcelService;
import com.dotacionestoronto.dotacionestoronto.service.ReporteUsuariosService;

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
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReporteUsuariosService reporteUsuariosService;

    @Autowired
    private ReporteUsuariosExcelService reporteUsuariosExcelService;

    @Autowired
    private ReporteProductosService reporteProductosService;

    @Autowired
    private ReporteProductosExcelService reporteProductosExcelService;

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

    @GetMapping("/usuarios/reporte")
    public ResponseEntity<byte[]> reporteUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        byte[] pdf = reporteUsuariosService.generarReporte(usuarios);

        String nombreArchivo = "reporte-usuarios-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(nombreArchivo).build());

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/usuarios/reporte/excel")
    public ResponseEntity<byte[]> reporteUsuariosExcel() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        byte[] excel = reporteUsuariosExcelService.generarReporte(usuarios);

        String nombreArchivo = "reporte-usuarios-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(nombreArchivo).build());

        return ResponseEntity.ok().headers(headers).body(excel);
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

    @GetMapping("/usuarios/editar")
    public String editarUsuarioForm(@RequestParam Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return "redirect:/admin/usuarios";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/editar_usuario_admin";
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuarioAdmin(
            @RequestParam Long idUsuario,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam Integer rol,
            Model model) {

        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario != null) {
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            usuario.setIdRol(rol);

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
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, imagen.getBytes());
            producto.setImagen("images/productos/" + nombreArchivo);
        }

        productoRepository.save(producto);
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/actualizar")
    public String actualizarProducto(
            @RequestParam Long idProducto,
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam String talla,
            @RequestParam String color,
            @RequestParam(required = false) MultipartFile imagen,
            RedirectAttributes redirectAttributes) throws IOException {

        Producto producto = productoRepository.findById(idProducto).orElse(null);

        if (producto == null) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el producto a actualizar.");
            return "redirect:/admin/productos";
        }

        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setTalla(talla);
        producto.setColor(color);

        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
            Path ruta = Paths.get("uploads/productos/" + nombreArchivo);
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, imagen.getBytes());
            producto.setImagen("images/productos/" + nombreArchivo);
        }

        productoRepository.save(producto);

        redirectAttributes.addFlashAttribute("exito", "Producto actualizado correctamente.");
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

    @GetMapping("/productos/reporte")
    public ResponseEntity<byte[]> reporteProductos() {
        List<Producto> productos = productoRepository.findAll();
        byte[] pdf = reporteProductosService.generarReporte(productos);

        String nombreArchivo = "reporte-productos-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(nombreArchivo).build());

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/productos/reporte/excel")
    public ResponseEntity<byte[]> reporteProductosExcel() {
        List<Producto> productos = productoRepository.findAll();
        byte[] excel = reporteProductosExcelService.generarReporte(productos);

        String nombreArchivo = "reporte-productos-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(nombreArchivo).build());

        return ResponseEntity.ok().headers(headers).body(excel);
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

    // ===== PEDIDOS (SOLO LECTURA PARA EL ADMIN) =====
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        List<Pedido> pedidos = pedidoRepository.findByOcultoFalseOrderByFechaPedidoDesc();

        List<PedidoVistaAdmin> vista = new ArrayList<>();
        for (Pedido p : pedidos) {
            PedidoVistaAdmin v = new PedidoVistaAdmin();
            v.id = p.getId();
            v.fecha = p.getFechaPedido();
            v.estado = p.getEstado();

            Usuario cliente = usuarioRepository.findById(p.getIdUsuario()).orElse(null);
            v.clienteNombre = cliente != null ? cliente.getNombre() : "Cliente eliminado";

            List<DetallePedido> detalles = detallePedidoRepository.findByIdPedido(p.getId());
            v.cantidadProductos = detalles.stream().mapToInt(DetallePedido::getCantidad).sum();
            v.total = detalles.stream()
                    .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vista.add(v);
        }

        model.addAttribute("pedidos", vista);
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/pedidos_admin";
    }

    @GetMapping("/pedidos/{id}/detalle")
    @ResponseBody
    public Map<String, Object> detallePedidoAdmin(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Usuario cliente = usuarioRepository.findById(pedido.getIdUsuario()).orElse(null);

        List<DetallePedido> detalles = detallePedidoRepository.findByIdPedido(id);
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedido d : detalles) {
            Producto producto = productoRepository.findById(d.getIdProducto()).orElse(null);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("producto", producto != null ? producto.getNombre() : "Producto eliminado");
            item.put("talla", producto != null ? producto.getTalla() : null);
            item.put("color", producto != null ? producto.getColor() : null);
            item.put("imagen", producto != null ? producto.getImagen() : null);
            item.put("cantidad", d.getCantidad());
            item.put("precioUnitario", d.getPrecioUnitario());

            BigDecimal subtotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
            item.put("subtotal", subtotal);
            total = total.add(subtotal);

            items.add(item);
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idPedido", pedido.getId());
        resultado.put("estado", pedido.getEstado());
        resultado.put("clienteNombre", cliente != null ? cliente.getNombre() : "Cliente eliminado");
        resultado.put("clienteCorreo", cliente != null ? cliente.getCorreo() : null);
        resultado.put("clienteTelefono", cliente != null ? cliente.getTelefono() : null);
        resultado.put("clienteDireccion", cliente != null ? cliente.getDireccion() : null);
        resultado.put("items", items);
        resultado.put("total", total);

        return resultado;
    }

    public static class PedidoVistaAdmin {
        public Long id;
        public LocalDateTime fecha;
        public String estado;
        public String clienteNombre;
        public Integer cantidadProductos;
        public BigDecimal total;

        public Long getId() { return id; }
        public LocalDateTime getFecha() { return fecha; }
        public String getEstado() { return estado; }
        public String getClienteNombre() { return clienteNombre; }
        public Integer getCantidadProductos() { return cantidadProductos; }
        public BigDecimal getTotal() { return total; }
    }

    // ===== VENTAS (SOLO LECTURA PARA EL ADMIN — se generan al entregar un pedido) =====
    @GetMapping("/ventas")
    public String ventas(Model model) {
        List<Venta> ventas = ventaRepository.findAllByOrderByFechaVentaDesc();

        List<VentaVistaAdmin> vista = new ArrayList<>();
        for (Venta v : ventas) {
            VentaVistaAdmin vv = new VentaVistaAdmin();
            vv.id = v.getId();
            vv.idPedido = v.getIdPedido();
            vv.fecha = v.getFechaVenta();

            Usuario cliente = usuarioRepository.findById(v.getIdUsuario()).orElse(null);
            vv.clienteNombre = cliente != null ? cliente.getNombre() : "Cliente eliminado";

            List<DetalleVenta> detalles = detalleVentaRepository.findByIdVenta(v.getId());
            vv.cantidadProductos = detalles.stream().mapToInt(DetalleVenta::getCantidad).sum();
            vv.total = detalles.stream()
                    .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            vista.add(vv);
        }

        model.addAttribute("ventas", vista);
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "admin/ventas_admin";
    }

    @GetMapping("/ventas/grafico")
    @ResponseBody
    public List<Map<String, Object>> graficoVentas() {
        List<Venta> ventasDesc = ventaRepository.findAllByOrderByFechaVentaDesc();
        List<Venta> ventasAsc = new ArrayList<>(ventasDesc);
        Collections.reverse(ventasAsc);

        List<Map<String, Object>> datos = new ArrayList<>();
        for (Venta v : ventasAsc) {
            List<DetalleVenta> detalles = detalleVentaRepository.findByIdVenta(v.getId());
            BigDecimal total = detalles.stream()
                    .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> punto = new LinkedHashMap<>();
            punto.put("fecha", v.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            punto.put("total", total);
            datos.add(punto);
        }
        return datos;
    }

    @GetMapping("/ventas/{id}/detalle")
    @ResponseBody
    public Map<String, Object> detalleVentaAdmin(@PathVariable Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        Usuario cliente = usuarioRepository.findById(venta.getIdUsuario()).orElse(null);
        Pedido pedido = venta.getIdPedido() != null
                ? pedidoRepository.findById(venta.getIdPedido()).orElse(null)
                : null;

        List<DetalleVenta> detalles = detalleVentaRepository.findByIdVenta(id);
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVenta d : detalles) {
            Producto producto = productoRepository.findById(d.getIdProducto()).orElse(null);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("producto", producto != null ? producto.getNombre() : "Producto eliminado");
            item.put("talla", producto != null ? producto.getTalla() : null);
            item.put("color", producto != null ? producto.getColor() : null);
            item.put("imagen", producto != null ? producto.getImagen() : null);
            item.put("cantidad", d.getCantidad());
            item.put("precioUnitario", d.getPrecioUnitario());

            BigDecimal subtotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
            item.put("subtotal", subtotal);
            total = total.add(subtotal);

            items.add(item);
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idVenta", venta.getId());
        resultado.put("idPedido", venta.getIdPedido());
        resultado.put("pedidoEstado", pedido != null ? pedido.getEstado() : null);
        resultado.put("clienteNombre", cliente != null ? cliente.getNombre() : "Cliente eliminado");
        resultado.put("clienteCorreo", cliente != null ? cliente.getCorreo() : null);
        resultado.put("clienteTelefono", cliente != null ? cliente.getTelefono() : null);
        resultado.put("clienteDireccion", cliente != null ? cliente.getDireccion() : null);
        resultado.put("items", items);
        resultado.put("total", total);

        return resultado;
    }

    public static class VentaVistaAdmin {
        public Long id;
        public Long idPedido;
        public LocalDateTime fecha;
        public String clienteNombre;
        public Integer cantidadProductos;
        public BigDecimal total;

        public Long getId() { return id; }
        public Long getIdPedido() { return idPedido; }
        public LocalDateTime getFecha() { return fecha; }
        public String getClienteNombre() { return clienteNombre; }
        public Integer getCantidadProductos() { return cantidadProductos; }
        public BigDecimal getTotal() { return total; }
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
            @RequestParam(required = false) String nombre,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String password,
            Model model) {

        Usuario usuario = getUsuarioActual();
        if (nombre != null && !nombre.isEmpty()) usuario.setNombre(nombre);
        if (!telefono.isEmpty()) usuario.setTelefono(telefono);
        if (!direccion.isEmpty()) usuario.setDireccion(direccion);
        if (password != null && !password.isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(password));
        }

        if (!correo.isEmpty() && !correo.equals(usuario.getCorreo())) {
            usuario.setCorreo(correo);
            usuarioRepository.save(usuario);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    correo,
                    auth.getCredentials(),
                    auth.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        } else {
            usuarioRepository.save(usuario);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("exito", "Perfil actualizado correctamente.");
        return "admin/perfil_admin";
    }
}