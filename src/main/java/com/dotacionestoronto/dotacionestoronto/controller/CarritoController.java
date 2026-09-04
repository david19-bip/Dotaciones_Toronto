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

/**
 * Controlador del carrito de compras del cliente.
 * <p>
 * A diferencia de las entidades {@code Carrito}/{@code DetalleCarrito} (que
 * existen en el modelo de datos pero no se usan aquí), este controlador
 * mantiene el carrito completamente en la sesión HTTP del usuario
 * ({@link HttpSession}), como un mapa en memoria. El carrito solo se
 * convierte en datos persistentes (un {@code Pedido} real con sus
 * {@code DetallePedido}) en el momento de finalizar la compra.
 */
@Controller
@RequestMapping("/cliente/carrito")
public class CarritoController {

    /** Clave bajo la cual se guarda el carrito dentro de la sesión HTTP. */
    private static final String SESSION_KEY = "carrito";

    @Autowired private ProductoRepository productoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private DetallePedidoRepository detallePedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    /** Obtiene el {@link Usuario} actualmente autenticado a partir del contexto de seguridad. */
    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    /**
     * Obtiene el carrito guardado en la sesión del usuario, creando uno
     * vacío (y guardándolo en sesión) si todavía no existe.
     */
    @SuppressWarnings("unchecked")
    private Map<Long, CarritoItem> getCarrito(HttpSession session) {
        Map<Long, CarritoItem> carrito = (Map<Long, CarritoItem>) session.getAttribute(SESSION_KEY);
        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            session.setAttribute(SESSION_KEY, carrito);
        }
        return carrito;
    }

    /**
     * Devuelve en formato JSON el estado actual del carrito de la sesión:
     * items, total en dinero y total de unidades.
     */
    @GetMapping
    @ResponseBody
    public Map<String, Object> verCarrito(HttpSession session) {
        return construirRespuesta(getCarrito(session));
    }

    /**
     * Agrega un producto al carrito de la sesión. Si el producto ya estaba
     * en el carrito, suma la cantidad indicada a la cantidad existente en
     * lugar de duplicar la línea.
     *
     * @param idProducto id del producto a agregar
     * @param cantidad   unidades a agregar (por defecto 1)
     * @return el estado actualizado del carrito, en JSON
     */
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

    /**
     * Actualiza la cantidad de un ítem del carrito. Si la nueva cantidad es
     * cero o negativa, el ítem se elimina del carrito.
     *
     * @param idDetalle id del producto dentro del carrito (ver nota en
     *                  {@link #construirRespuesta} sobre este identificador)
     * @param cantidad  nueva cantidad deseada
     * @return el estado actualizado del carrito, en JSON
     */
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

    /** Elimina un ítem específico del carrito de la sesión. */
    @PostMapping("/eliminar")
    @ResponseBody
    public Map<String, Object> eliminar(@RequestParam Long idDetalle, HttpSession session) {
        Map<Long, CarritoItem> carrito = getCarrito(session);
        carrito.remove(idDetalle);
        return construirRespuesta(carrito);
    }

    /**
     * Finaliza la compra: convierte el carrito en sesión en un
     * {@code Pedido} real (estado "Pendiente") junto con sus
     * {@code DetallePedido}, uno por cada producto del carrito. Si el
     * carrito está vacío, no crea nada y muestra un mensaje de error.
     * Al terminar, limpia el carrito de la sesión y redirige al listado de
     * pedidos del cliente.
     */
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

    /**
     * Traduce el mapa interno del carrito a la estructura JSON que consume
     * el frontend (lista de items con subtotal, total general y total de
     * unidades). Nota: como el carrito solo existe en sesión, se usa el id
     * del producto como "idDetalle" temporal, ya que todavía no existe una
     * fila real de detalle en base de datos.
     */
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

    /** Ítem del carrito almacenado en sesión: producto, precio y cantidad. */
    private static class CarritoItem implements Serializable {
        Long idProducto;
        String nombre;
        BigDecimal precioUnitario;
        Integer cantidad;
    }
}