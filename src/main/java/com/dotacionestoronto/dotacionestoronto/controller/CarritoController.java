package com.dotacionestoronto.dotacionestoronto.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dotacionestoronto.dotacionestoronto.entity.DetallePedido;
import com.dotacionestoronto.dotacionestoronto.entity.Pedido;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.DetallePedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.PedidoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cliente/carrito")
public class CarritoController {

    private static final String SESSION_KEY = "carrito";

    @Autowired private ProductoRepository productoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private DetallePedidoRepository detallePedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    @SuppressWarnings("unchecked")
    private Map<Long, CarritoItem> getCarrito(HttpSession session) {
        Map<Long, CarritoItem> carrito = (Map<Long, CarritoItem>) session.getAttribute(SESSION_KEY);
        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            session.setAttribute(SESSION_KEY, carrito);
        }
        return carrito;
    }

    // GET /cliente/carrito -> estado actual del carrito
    @GetMapping
    @ResponseBody
    public Map<String, Object> verCarrito(HttpSession session) {
        return construirRespuesta(getCarrito(session));
    }

    // POST /cliente/carrito/agregar
    @PostMapping("/agregar")
    @ResponseBody
    public Map<String, Object> agregar(@RequestParam Long idProducto,
                                        @RequestParam(defaultValue = "1") Integer cantidad,
                                        HttpSession session) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Map<Long, CarritoItem> carrito = getCarrito(session);
        CarritoItem item = carrito.get(idProducto);
        if (item == null) {
            item = new CarritoItem();
            item.idProducto = idProducto;
            item.nombre = producto.getNombre();
            item.precioUnitario = producto.getPrecio();
            item.cantidad = 0;
            carrito.put(idProducto, item);
        }
        item.cantidad += cantidad;

        return construirRespuesta(carrito);
    }

    // POST /cliente/carrito/actualizar
    @PostMapping("/actualizar")
    @ResponseBody
    public Map<String, Object> actualizar(@RequestParam Long idDetalle,
                                           @RequestParam Integer cantidad,
                                           HttpSession session) {
        Map<Long, CarritoItem> carrito = getCarrito(session);
        if (cantidad <= 0) {
            carrito.remove(idDetalle);
        } else {
            CarritoItem item = carrito.get(idDetalle);
            if (item != null) {
                item.cantidad = cantidad;
            }
        }
        return construirRespuesta(carrito);
    }

    // POST /cliente/carrito/eliminar
    @PostMapping("/eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam Long idDetalle, HttpSession session) {
        Map<Long, CarritoItem> carrito = getCarrito(session);
        carrito.remove(idDetalle);
        return construirRespuesta(carrito);
    }

    // POST /cliente/carrito/finalizar -> crea Pedido + DetallePedido reales
    @PostMapping("/finalizar")
    public String finalizar(HttpSession session, RedirectAttributes redirectAttributes) {
        Map<Long, CarritoItem> carrito = getCarrito(session);

        if (carrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tu carrito está vacío.");
            return "redirect:/cliente/productos";
        }

        Usuario usuario = getUsuarioActual();

        Pedido pedido = new Pedido();
        pedido.setIdUsuario(usuario.getId());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("Pendiente");
        pedido.setOculto(false);
        pedido = pedidoRepository.save(pedido);

        for (CarritoItem item : carrito.values()) {
            DetallePedido detalle = new DetallePedido();
            detalle.setIdPedido(pedido.getId());
            detalle.setIdProducto(item.idProducto);
            detalle.setPrecioUnitario(item.precioUnitario);
            detalle.setCantidad(item.cantidad);
            detallePedidoRepository.save(detalle);
        }

        session.removeAttribute(SESSION_KEY);

        redirectAttributes.addFlashAttribute("exito", "Tu pedido fue registrado correctamente.");
        return "redirect:/cliente/pedidos";
    }

    private Map<String, Object> construirRespuesta(Map<Long, CarritoItem> carrito) {
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalUnidades = 0;

        for (CarritoItem item : carrito.values()) {
            BigDecimal subtotal = item.precioUnitario.multiply(BigDecimal.valueOf(item.cantidad));
            total = total.add(subtotal);
            totalUnidades += item.cantidad;

            Map<String, Object> mapItem = new LinkedHashMap<>();
            mapItem.put("idDetalle", item.idProducto); // aún no existe fila real; usamos idProducto como id temporal
            mapItem.put("nombre", item.nombre);
            mapItem.put("cantidad", item.cantidad);
            mapItem.put("subtotal", subtotal);
            items.add(mapItem);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("items", items);
        resp.put("total", total);
        resp.put("totalUnidades", totalUnidades);
        return resp;
    }

    private static class CarritoItem implements Serializable {
        Long idProducto;
        String nombre;
        BigDecimal precioUnitario;
        Integer cantidad;
    }
}