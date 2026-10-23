package com.dotacionestoronto.dotacionestoronto.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.dotacionestoronto.dotacionestoronto.entity.MovimientoInventario;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import com.dotacionestoronto.dotacionestoronto.repository.DetallePedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DetalleVentaRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DevolucionRepository;
import com.dotacionestoronto.dotacionestoronto.repository.InventarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.MovimientoInventarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.VentaRepository;
import com.dotacionestoronto.dotacionestoronto.service.ReporteProductosExcelService;
import com.dotacionestoronto.dotacionestoronto.service.ReporteProductosService;
import com.dotacionestoronto.dotacionestoronto.service.ReporteVentasExcelService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

/**
 * Controlador del panel de administrador.
 *
 * Complementa a {@link AdminController} (que cubre el panel y la gestión de
 * usuarios) con el resto de secciones del menú lateral: perfil, productos,
 * inventario, pedidos, ventas y devoluciones.
 *
 * Antes de existir esta clase esas plantillas ya estaban en
 * src/main/resources/templates/admin/, pero no había ningún controlador que
 * respondiera a sus rutas, por eso el navegador mostraba error 404.
 */
@Controller
@RequestMapping("/admin")
public class AdminPanelController {

    private static final String CARPETA_IMAGENES = "uploads/productos";
    private static final String PREFIJO_IMAGENES = "images/productos";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReporteProductosService reporteProductosService;

    @Autowired
    private ReporteProductosExcelService reporteProductosExcelService;

    @Autowired
    private ReporteVentasExcelService reporteVentasExcelService;

    @Autowired
    private HttpSession session;

    // =========================================================
    // UTILIDADES
    // =========================================================

    private Usuario getUsuarioActual() {
        return (Usuario) session.getAttribute("usuarioLogueado");
    }

    /** Agrega el nombre del usuario logueado al modelo (lo usa el header). */
    private void agregarNombre(Model model) {
        Usuario usuario = getUsuarioActual();
        model.addAttribute("nombre", usuario != null ? usuario.getNombre() : "Administrador");
    }

    private String descripcionProducto(Producto p) {
        if (p == null) {
            return "Producto";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(p.getNombre() != null ? p.getNombre() : "Producto");
        if (p.getTalla() != null && !p.getTalla().isBlank()) {
            sb.append(" - ").append(p.getTalla());
        }
        if (p.getColor() != null && !p.getColor().isBlank()) {
            sb.append(" - ").append(p.getColor());
        }
        return sb.toString();
    }

    /** Guarda la imagen subida en uploads/productos y devuelve su ruta web. */
    private String guardarImagen(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            return null;
        }

        try {
            Path carpeta = Paths.get(CARPETA_IMAGENES);
            Files.createDirectories(carpeta);

            String original = imagen.getOriginalFilename() != null ? imagen.getOriginalFilename() : "imagen.jpg";
            original = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            String nombreArchivo = System.currentTimeMillis() + "_" + original;

            Files.copy(imagen.getInputStream(), carpeta.resolve(nombreArchivo), StandardCopyOption.REPLACE_EXISTING);

            return PREFIJO_IMAGENES + "/" + nombreArchivo;

        } catch (IOException e) {
            return null;
        }
    }

    private static ResponseEntity<byte[]> archivo(byte[] contenido, String nombre, String tipo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(tipo));
        headers.setContentDisposition(ContentDisposition.attachment().filename(nombre).build());
        return ResponseEntity.ok().headers(headers).body(contenido);
    }

    // =========================================================
    // PERFIL
    // =========================================================

    @GetMapping("/perfil")
    public String perfil(Model model) {

        Usuario usuario = getUsuarioActual();

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        agregarNombre(model);

        return "admin/perfil_admin";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String password,
            Model model) {

        Usuario usuario = getUsuarioActual();

        if (usuario == null) {
            return "redirect:/login";
        }

        String correoLimpio = correo != null ? correo.trim() : "";
        String telefonoLimpio = telefono != null ? telefono.trim() : "";
        String direccionLimpia = direccion != null ? direccion.trim() : "";

        // ===== Validaciones de servidor (las mismas del panel del bodeguero) =====

        if (!correoLimpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return volverAlPerfil(model, usuario, "El correo electrónico no tiene un formato válido.");
        }

        if (!telefonoLimpio.matches("^[0-9]{7,15}$")) {
            return volverAlPerfil(model, usuario, "El teléfono debe contener solo números, entre 7 y 15 dígitos.");
        }

        if (direccionLimpia.length() < 5) {
            return volverAlPerfil(model, usuario, "La dirección debe tener al menos 5 caracteres.");
        }

        if (password != null && !password.isEmpty() && password.length() < 6) {
            return volverAlPerfil(model, usuario, "La contraseña debe tener al menos 6 caracteres.");
        }

        boolean correoCambio = !correoLimpio.equals(usuario.getCorreo());

        if (correoCambio) {
            Usuario existente = usuarioRepository.findByCorreo(correoLimpio);
            if (existente != null && !existente.getId().equals(usuario.getId())) {
                return volverAlPerfil(model, usuario, "Ese correo ya está en uso por otro usuario.");
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

            // Se re-autentica con el nuevo correo para no perder la sesión
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        correoLimpio,
                        auth.getCredentials(),
                        auth.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        } else {
            usuarioRepository.save(usuario);
        }

        session.setAttribute("usuarioLogueado", usuario);

        model.addAttribute("usuario", usuario);
        agregarNombre(model);
        model.addAttribute("exito", "Perfil actualizado correctamente.");

        return "admin/perfil_admin";
    }

    private String volverAlPerfil(Model model, Usuario usuario, String error) {
        model.addAttribute("usuario", usuario);
        agregarNombre(model);
        model.addAttribute("error", error);
        return "admin/perfil_admin";
    }

    // =========================================================
    // PRODUCTOS
    // =========================================================

    @GetMapping("/productos")
    public String productos(Model model) {

        model.addAttribute("productos", productoRepository.findByInhabilitadoFalseOrderByIdDesc());
        model.addAttribute("productosInhabilitados", productoRepository.findByInhabilitadoTrueOrderByIdDesc());
        agregarNombre(model);

        return "admin/productos_admin";
    }

    @PostMapping("/productos/crear")
    public String crearProducto(
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) MultipartFile imagen,
            RedirectAttributes redirect) {

        if (nombre == null || nombre.isBlank()) {
            redirect.addFlashAttribute("error", "El nombre del producto es obligatorio.");
            return "redirect:/admin/productos";
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            redirect.addFlashAttribute("error", "El precio debe ser mayor que cero.");
            return "redirect:/admin/productos";
        }

        Producto producto = new Producto();
        producto.setNombre(nombre.trim());
        producto.setPrecio(precio);
        producto.setTalla(talla != null ? talla.trim() : null);
        producto.setColor(color != null ? color.trim() : null);
        producto.setEstado("Disponible");
        producto.setInhabilitado(false);
        producto.setCategoria("General");
        producto.setImagen(guardarImagen(imagen));

        productoRepository.save(producto);

        // Si el producto se crea y no tiene inventario, se crea en cero
        // para que aparezca de inmediato en la sección de inventario.
        boolean tieneInventario = inventarioRepository.findAll().stream()
                .anyMatch(inv -> producto.getId().equals(inv.getIdProducto()));

        if (!tieneInventario) {
            Inventario inventario = new Inventario();
            inventario.setIdProducto(producto.getId());
            inventario.setCantidadActual(0);
            inventario.setStockMinimo(0);
            inventario.setInhabilitado(false);
            inventarioRepository.save(inventario);
        }

        redirect.addFlashAttribute("exito", "Producto creado correctamente.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/actualizar")
    public String actualizarProducto(
            @RequestParam Long idProducto,
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) MultipartFile imagen,
            RedirectAttributes redirect) {

        Producto producto = productoRepository.findById(idProducto).orElse(null);

        if (producto == null) {
            redirect.addFlashAttribute("error", "El producto no existe.");
            return "redirect:/admin/productos";
        }

        if (nombre == null || nombre.isBlank()) {
            redirect.addFlashAttribute("error", "El nombre del producto es obligatorio.");
            return "redirect:/admin/productos";
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            redirect.addFlashAttribute("error", "El precio debe ser mayor que cero.");
            return "redirect:/admin/productos";
        }

        producto.setNombre(nombre.trim());
        producto.setPrecio(precio);
        producto.setTalla(talla != null ? talla.trim() : null);
        producto.setColor(color != null ? color.trim() : null);

        String nuevaImagen = guardarImagen(imagen);
        if (nuevaImagen != null) {
            producto.setImagen(nuevaImagen);
        }

        productoRepository.save(producto);

        redirect.addFlashAttribute("exito", "Producto actualizado correctamente.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/toggleEstado")
    public String toggleEstadoProducto(@RequestParam Long idProducto, RedirectAttributes redirect) {

        Producto producto = productoRepository.findById(idProducto).orElse(null);

        if (producto != null) {
            boolean disponible = !"Disponible".equalsIgnoreCase(producto.getEstado());
            producto.setEstado(disponible ? "Disponible" : "Agotado");
            productoRepository.save(producto);

            redirect.addFlashAttribute("exito",
                    "Estado del producto cambiado a " + producto.getEstado() + ".");
        }

        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/inhabilitar")
    public String inhabilitarProducto(@RequestParam Long idProducto, RedirectAttributes redirect) {

        Producto producto = productoRepository.findById(idProducto).orElse(null);

        if (producto != null) {
            producto.setInhabilitado(true);
            productoRepository.save(producto);
            redirect.addFlashAttribute("exito", "Producto inhabilitado.");
        }

        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/habilitar")
    public String habilitarProducto(@RequestParam Long idProducto, RedirectAttributes redirect) {

        Producto producto = productoRepository.findById(idProducto).orElse(null);

        if (producto != null) {
            producto.setInhabilitado(false);
            productoRepository.save(producto);
            redirect.addFlashAttribute("exito", "Producto habilitado.");
        }

        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/reporte")
    public ResponseEntity<byte[]> reporteProductos() {
        byte[] pdf = reporteProductosService.generarReporte(productoRepository.findAll());
        return archivo(pdf, "reporte_productos.pdf", "application/pdf");
    }

    @GetMapping("/productos/reporte/excel")
    public ResponseEntity<byte[]> reporteProductosExcel() {
        byte[] excel = reporteProductosExcelService.generarReporte(productoRepository.findAll());
        return archivo(excel, "reporte_productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // =========================================================
    // INVENTARIO
    // =========================================================

    @GetMapping("/inventario")
    public String inventario(Model model) {

        model.addAttribute("inventarios", inventarioRepository.findAll());
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("movimientos", movimientoInventarioRepository.findTop50ByOrderByIdDesc());
        agregarNombre(model);

        return "admin/inventario_admin";
    }

    @Transactional
    @PostMapping("/inventario/crear")
    public String crearInventario(
            @RequestParam Long idProducto,
            @RequestParam Integer cantidadActual,
            @RequestParam Integer stockMinimo,
            RedirectAttributes redirect) {

        boolean yaExiste = inventarioRepository.findAll().stream()
                .anyMatch(inv -> idProducto.equals(inv.getIdProducto()));

        if (yaExiste) {
            redirect.addFlashAttribute("error",
                    "Ese producto ya tiene un registro de inventario. Edítalo en lugar de crearlo.");
            return "redirect:/admin/inventario";
        }

        Inventario inventario = new Inventario();
        inventario.setIdProducto(idProducto);
        inventario.setCantidadActual(cantidadActual != null ? cantidadActual : 0);
        inventario.setStockMinimo(stockMinimo != null ? stockMinimo : 0);
        inventario.setInhabilitado(false);
        inventarioRepository.save(inventario);

        registrarMovimiento(inventario.getId(), idProducto, "ENTRADA",
                inventario.getCantidadActual(), "Registro inicial de inventario");

        redirect.addFlashAttribute("exito", "Registro de inventario creado.");
        return "redirect:/admin/inventario";
    }

    @Transactional
    @PostMapping("/inventario/editar")
    public String editarInventario(
            @RequestParam Long idInventario,
            @RequestParam Integer cantidadActual,
            @RequestParam Integer stockMinimo,
            @RequestParam(required = false, defaultValue = "Ajuste manual de inventario") String motivo,
            RedirectAttributes redirect) {

        Inventario inventario = inventarioRepository.findById(idInventario).orElse(null);

        if (inventario == null) {
            redirect.addFlashAttribute("error", "El registro de inventario no existe.");
            return "redirect:/admin/inventario";
        }

        int anterior = inventario.getCantidadActual() != null ? inventario.getCantidadActual() : 0;
        int nueva = cantidadActual != null ? cantidadActual : 0;
        int diferencia = nueva - anterior;

        inventario.setCantidadActual(nueva);
        inventario.setStockMinimo(stockMinimo != null ? stockMinimo : 0);
        inventarioRepository.save(inventario);

        String tipo = "AJUSTE";
        if (diferencia > 0) {
            tipo = "ENTRADA";
        } else if (diferencia < 0) {
            tipo = "SALIDA";
        }

        registrarMovimiento(idInventario, inventario.getIdProducto(), tipo, Math.abs(diferencia), motivo);

        redirect.addFlashAttribute("exito", "Inventario actualizado.");
        return "redirect:/admin/inventario";
    }

    @PostMapping("/inventario/cambiar-estado")
    public String cambiarEstadoInventario(@RequestParam Long idInventario, RedirectAttributes redirect) {

        Inventario inventario = inventarioRepository.findById(idInventario).orElse(null);

        if (inventario != null) {
            boolean nuevoEstado = inventario.getInhabilitado() == null || !inventario.getInhabilitado();
            inventario.setInhabilitado(nuevoEstado);
            inventarioRepository.save(inventario);

            redirect.addFlashAttribute("exito", nuevoEstado
                    ? "Registro de inventario inhabilitado."
                    : "Registro de inventario habilitado.");
        }

        return "redirect:/admin/inventario";
    }

    private void registrarMovimiento(Long idInventario, Long idProducto, String tipo, int cantidad, String motivo) {

        Producto producto = idProducto != null ? productoRepository.findById(idProducto).orElse(null) : null;

        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdInventario(idInventario);
        mov.setNombreProducto(descripcionProducto(producto));
        mov.setTipoMovimiento(tipo);
        mov.setCantidad(cantidad);
        mov.setMotivo(motivo);

        Usuario usuario = getUsuarioActual();
        mov.setUsuarioResponsable(usuario != null ? usuario.getNombre() : "Administrador");

        movimientoInventarioRepository.save(mov);
    }

    // =========================================================
    // PEDIDOS
    // =========================================================

    @GetMapping("/pedidos")
    public String pedidos(Model model) {

        List<Pedido> pedidos = pedidoRepository.findAll();
        List<PedidoVista> lista = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            if (Boolean.TRUE.equals(pedido.getOculto())) {
                continue;
            }

            PedidoVista vista = new PedidoVista();
            vista.id = pedido.getId();
            vista.estado = pedido.getEstado();
            vista.fecha = pedido.getFechaPedido();

            Usuario cliente = pedido.getIdUsuario() != null
                    ? usuarioRepository.findById(pedido.getIdUsuario()).orElse(null)
                    : null;

            if (cliente != null) {
                vista.clienteNombre = cliente.getNombre();
            }

            List<DetallePedido> detalles = detallePedidoRepository.findByIdPedido(pedido.getId());
            double total = 0;
            int unidades = 0;

            for (DetallePedido detalle : detalles) {
                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                unidades += cantidad;

                if (detalle.getPrecioUnitario() != null) {
                    total += detalle.getPrecioUnitario().doubleValue() * cantidad;
                }
            }

            vista.cantidadProductos = unidades;
            vista.total = total;

            lista.add(vista);
        }

        // Los más recientes primero
        lista.sort((a, b) -> {
            if (a.fecha == null && b.fecha == null) {
                return 0;
            }
            if (a.fecha == null) {
                return 1;
            }
            if (b.fecha == null) {
                return -1;
            }
            return b.fecha.compareTo(a.fecha);
        });

        model.addAttribute("pedidos", lista);
        agregarNombre(model);

        return "admin/pedidos_admin";
    }

    @GetMapping("/pedidos/{id}/detalle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detallePedido(@PathVariable Long id) {

        Pedido pedido = pedidoRepository.findById(id).orElse(null);

        if (pedido == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", pedido.getId());
        respuesta.put("estado", pedido.getEstado());
        respuesta.put("fecha", pedido.getFechaPedido() != null ? pedido.getFechaPedido().toString() : null);

        Usuario cliente = pedido.getIdUsuario() != null
                ? usuarioRepository.findById(pedido.getIdUsuario()).orElse(null)
                : null;

        respuesta.put("clienteNombre", cliente != null ? cliente.getNombre() : "Cliente #" + pedido.getIdUsuario());
        respuesta.put("clienteCorreo", cliente != null ? cliente.getCorreo() : null);
        respuesta.put("clienteTelefono", cliente != null ? cliente.getTelefono() : null);
        respuesta.put("clienteDireccion", cliente != null ? cliente.getDireccion() : null);

        List<Map<String, Object>> items = new ArrayList<>();
        double total = 0;

        for (DetallePedido detalle : detallePedidoRepository.findByIdPedido(id)) {
            Producto producto = detalle.getIdProducto() != null
                    ? productoRepository.findById(detalle.getIdProducto()).orElse(null)
                    : null;

            int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
            double precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario().doubleValue() : 0;
            double subtotal = precio * cantidad;
            total += subtotal;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("producto", producto != null && producto.getNombre() != null
                    ? producto.getNombre()
                    : "Producto");
            item.put("talla", producto != null ? producto.getTalla() : null);
            item.put("color", producto != null ? producto.getColor() : null);
            item.put("imagen", producto != null ? producto.getImagen() : null);
            item.put("cantidad", cantidad);
            item.put("precioUnitario", precio);
            item.put("subtotal", subtotal);
            items.add(item);
        }

        respuesta.put("items", items);
        respuesta.put("total", total);

        return ResponseEntity.ok(respuesta);
    }

    // =========================================================
    // VENTAS
    // =========================================================

    @GetMapping("/ventas")
    public String ventas(Model model) {

        List<VentaAdminVista> lista = new ArrayList<>();

        for (Venta venta : ventaRepository.findAllByOrderByFechaVentaDesc()) {
            VentaAdminVista vista = new VentaAdminVista();
            vista.id = venta.getId();
            vista.idPedido = venta.getIdPedido();
            vista.fecha = venta.getFechaVenta();
            vista.total = venta.getTotalPagado() != null ? venta.getTotalPagado() : 0;

            Usuario cliente = venta.getIdUsuario() != null
                    ? usuarioRepository.findById(venta.getIdUsuario()).orElse(null)
                    : null;

            if (cliente != null) {
                vista.clienteNombre = cliente.getNombre();
            }

            int unidades = 0;
            for (DetalleVenta detalle : detalleVentaRepository.findByIdVenta(venta.getId())) {
                unidades += detalle.getCantidad() != null ? detalle.getCantidad() : 0;
            }
            vista.cantidadProductos = unidades;

            lista.add(vista);
        }

        model.addAttribute("ventas", lista);
        agregarNombre(model);

        return "admin/ventas_admin";
    }

    @GetMapping("/ventas/grafico")
    @ResponseBody
    public List<Map<String, Object>> graficoVentas() {

        List<Venta> ventas = ventaRepository.findAllByOrderByFechaVentaDesc();

        Map<LocalDate, Double> acumulado = new TreeMap<>();

        for (Venta venta : ventas) {
            if (venta.getFechaVenta() == null) {
                continue;
            }
            LocalDate dia = venta.getFechaVenta().toLocalDate();
            double valor = venta.getTotalPagado() != null ? venta.getTotalPagado() : 0;
            acumulado.merge(dia, valor, Double::sum);
        }

        List<Map<String, Object>> puntos = new ArrayList<>();

        for (Map.Entry<LocalDate, Double> entrada : acumulado.entrySet()) {
            Map<String, Object> punto = new LinkedHashMap<>();
            punto.put("fecha", entrada.getKey().format(FORMATO_FECHA));
            punto.put("total", entrada.getValue());
            puntos.add(punto);
        }

        return puntos;
    }

    @GetMapping("/ventas/{id}/detalle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detalleVenta(@PathVariable Long id) {

        Venta venta = ventaRepository.findById(id).orElse(null);

        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", venta.getId());
        respuesta.put("idPedido", venta.getIdPedido());
        respuesta.put("fecha", venta.getFechaVenta() != null ? venta.getFechaVenta().toString() : null);

        Usuario cliente = venta.getIdUsuario() != null
                ? usuarioRepository.findById(venta.getIdUsuario()).orElse(null)
                : null;

        respuesta.put("clienteNombre", cliente != null ? cliente.getNombre() : "Cliente #" + venta.getIdUsuario());
        respuesta.put("clienteCorreo", cliente != null ? cliente.getCorreo() : null);
        respuesta.put("clienteTelefono", cliente != null ? cliente.getTelefono() : null);
        respuesta.put("clienteDireccion", cliente != null ? cliente.getDireccion() : null);

        Pedido pedido = venta.getIdPedido() != null
                ? pedidoRepository.findById(venta.getIdPedido()).orElse(null)
                : null;

        respuesta.put("pedidoEstado", pedido != null ? pedido.getEstado() : null);

        List<Map<String, Object>> items = new ArrayList<>();
        double total = 0;

        for (DetalleVenta detalle : detalleVentaRepository.findByIdVenta(id)) {
            Producto producto = detalle.getIdProducto() != null
                    ? productoRepository.findById(detalle.getIdProducto()).orElse(null)
                    : null;

            int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
            double precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario().doubleValue() : 0;
            double subtotal = precio * cantidad;
            total += subtotal;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("producto", producto != null && producto.getNombre() != null
                    ? producto.getNombre()
                    : "Producto");
            item.put("talla", producto != null ? producto.getTalla() : null);
            item.put("color", producto != null ? producto.getColor() : null);
            item.put("imagen", producto != null ? producto.getImagen() : null);
            item.put("cantidad", cantidad);
            item.put("precioUnitario", precio);
            item.put("subtotal", subtotal);
            items.add(item);
        }

        respuesta.put("items", items);
        respuesta.put("total", total > 0 ? total : venta.getTotalPagado());

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/ventas/reporte/excel")
    public ResponseEntity<byte[]> reporteVentasExcel() {
        byte[] excel = reporteVentasExcelService.generarReporte(ventaRepository.findAllByOrderByFechaVentaDesc());
        return archivo(excel, "reporte_ventas.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // =========================================================
    // DEVOLUCIONES
    // =========================================================

    @GetMapping("/devoluciones")
    public String devoluciones(Model model) {

        model.addAttribute("devoluciones", devolucionRepository.findAllByOrderByFechaDevolucionDesc());
        agregarNombre(model);

        return "admin/devoluciones_admin";
    }

    @PostMapping("/devoluciones/cambiarEstado")
    public String cambiarEstadoDevolucion(
            @RequestParam Long idDevolucion,
            @RequestParam String estado,
            RedirectAttributes redirect) {

        Devolucion devolucion = devolucionRepository.findById(idDevolucion).orElse(null);

        if (devolucion == null) {
            redirect.addFlashAttribute("error", "La devolución no existe.");
            return "redirect:/admin/devoluciones";
        }

        List<String> permitidos = List.of("Pendiente", "Aprobada", "Rechazada");

        if (!permitidos.contains(estado)) {
            redirect.addFlashAttribute("error", "Estado de devolución no válido.");
            return "redirect:/admin/devoluciones";
        }

        devolucion.setEstado(estado);
        devolucionRepository.save(devolucion);

        redirect.addFlashAttribute("exito", "Devolución #" + idDevolucion + " marcada como " + estado + ".");
        return "redirect:/admin/devoluciones";
    }

    // =========================================================
    // VISTAS AUXILIARES (públicas para que Thymeleaf las lea)
    // =========================================================

    public static class PedidoVista {
        public Long id;
        public String clienteNombre;
        public java.time.LocalDateTime fecha;
        public Integer cantidadProductos;
        public Double total;
        public String estado;
    }

    public static class VentaAdminVista {
        public Long id;
        public Long idPedido;
        public String clienteNombre;
        public java.time.LocalDateTime fecha;
        public Integer cantidadProductos;
        public Double total;
    }
}
