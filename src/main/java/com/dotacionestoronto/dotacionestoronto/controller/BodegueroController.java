package com.dotacionestoronto.dotacionestoronto.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.dotacionestoronto.dotacionestoronto.entity.Inventario;
import com.dotacionestoronto.dotacionestoronto.entity.MovimientoInventario;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.repository.InventarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.MovimientoInventarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;

@Controller
@RequestMapping("/bodeguero")
public class BodegueroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName());
    }

    @GetMapping("/panel")
    public String panel(Model model) {
        List<Inventario> inventarios = inventarioRepository.findAll();
        List<Producto> productos = productoRepository.findAll();

        List<Map<String, Object>> listaTabla = new ArrayList<>();

        long totalProductos = productos.size();
        long totalStockBajo = 0;
        long totalAgotados = 0;

        for (Producto prod : productos) {
            Long idProd = prod.getId();

            Inventario invEncontrado = null;
            if (idProd != null && inventarios != null) {
                for (Inventario inv : inventarios) {
                    if (inv.getIdProducto() != null && idProd.equals(inv.getIdProducto())) {
                        invEncontrado = inv;
                        break;
                    }
                }
            }

            int cantidadActual = (invEncontrado != null && invEncontrado.getCantidadActual() != null)
                                 ? invEncontrado.getCantidadActual() : 0;

            int stockMinimo = (invEncontrado != null && invEncontrado.getStockMinimo() != null)
                              ? invEncontrado.getStockMinimo() : 0;

            if (cantidadActual == 0) {
                totalAgotados++;
            } else if (cantidadActual <= stockMinimo) {
                totalStockBajo++;
            }

            if (cantidadActual <= stockMinimo) {
                Map<String, Object> fila = new HashMap<>();

                String nombreCompleto = prod.getNombre();
                if (prod.getTalla() != null) nombreCompleto += " - " + prod.getTalla();
                if (prod.getColor() != null) nombreCompleto += " (" + prod.getColor() + ")";

                fila.put("nombre", nombreCompleto);
                fila.put("cantidadActual", cantidadActual);
                fila.put("stockMinimo", stockMinimo);

                listaTabla.add(fila);
            }
        }

        model.addAttribute("nombre", getUsuarioActual().getNombre());
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalStockBajo", totalStockBajo);
        model.addAttribute("totalAgotados", totalAgotados);
        model.addAttribute("totalInventario", inventarios.size());
        model.addAttribute("listaTabla", listaTabla);

        return "bodeguero/panel_bodeguero";
    }

    // ===== INVENTARIO =====
    @GetMapping("/inventario")
    public String inventario(Model model) {
        model.addAttribute("inventarios", inventarioRepository.findAll());
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("movimientos", movimientoInventarioRepository.findTop50ByOrderByIdDesc());
        model.addAttribute("nombre", getUsuarioActual().getNombre());
        return "bodeguero/inventario_bodeguero";
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

        String nombreProd = "#" + idProducto;
        var prodOpt = productoRepository.findById(idProducto);
        if (prodOpt.isPresent()) {
            var p = prodOpt.get();
            nombreProd = p.getNombre() + " - " + p.getTalla() + " - " + p.getColor();
        }

        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdInventario(inventario.getId());
        mov.setNombreProducto(nombreProd);
        mov.setTipoMovimiento("ENTRADA");
        mov.setCantidad(cantidadActual);
        mov.setMotivo("Registro inicial de inventario");
        mov.setUsuarioResponsable(getUsuarioActual().getNombre());
        movimientoInventarioRepository.save(mov);

        return "redirect:/bodeguero/inventario";
    }

    // ===== EDITAR REGISTRO COMPLETO Y REGISTRAR MOVIMIENTO =====
    @PostMapping("/inventario/editar")
    public String editarInventario(
            @RequestParam Long idInventario,
            @RequestParam Integer cantidadActual,
            @RequestParam Integer stockMinimo,
            @RequestParam(required = false, defaultValue = "Ajuste manual de inventario") String motivo) {

        Inventario inventario = inventarioRepository.findById(idInventario).orElse(null);
        if (inventario != null) {
            int diferencia = cantidadActual - (inventario.getCantidadActual() != null ? inventario.getCantidadActual() : 0);

            String tipo = "AJUSTE";
            if (diferencia > 0) tipo = "ENTRADA";
            else if (diferencia < 0) tipo = "SALIDA";

            inventario.setCantidadActual(cantidadActual);
            inventario.setStockMinimo(stockMinimo);
            inventarioRepository.save(inventario);

            String nombreProd = "#" + inventario.getIdProducto();
            var prodOpt = productoRepository.findById(inventario.getIdProducto());
            if (prodOpt.isPresent()) {
                var p = prodOpt.get();
                nombreProd = p.getNombre() + " - " + p.getTalla() + " - " + p.getColor();
            }

            MovimientoInventario mov = new MovimientoInventario();
            mov.setIdInventario(idInventario);
            mov.setNombreProducto(nombreProd);
            mov.setTipoMovimiento(tipo);
            mov.setCantidad(Math.abs(diferencia));
            mov.setMotivo(motivo);
            mov.setUsuarioResponsable(getUsuarioActual().getNombre());
            movimientoInventarioRepository.save(mov);
        }
        return "redirect:/bodeguero/inventario";
    }

    // ===== INHABILITAR / HABILITAR REGISTRO =====
    @PostMapping("/inventario/cambiar-estado")
    public String cambiarEstadoInventario(@RequestParam Long idInventario) {
        Inventario inventario = inventarioRepository.findById(idInventario).orElse(null);
        if (inventario != null) {
            boolean nuevoEstado = inventario.getInhabilitado() == null || !inventario.getInhabilitado();
            inventario.setInhabilitado(nuevoEstado);
            inventarioRepository.save(inventario);
        }
        return "redirect:/bodeguero/inventario";
    }

    // ===== PERFIL =====
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Usuario usuario = getUsuarioActual();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombre", usuario.getNombre());
        return "bodeguero/perfil_bodeguero";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            @RequestParam(required = false) String password,
            Model model) {

        Usuario usuario = getUsuarioActual();

        String correoLimpio = correo != null ? correo.trim() : "";
        String telefonoLimpio = telefono != null ? telefono.trim() : "";
        String direccionLimpia = direccion != null ? direccion.trim() : "";

        // ===== Validaciones de servidor =====

        if (!correoLimpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "El correo electrónico no tiene un formato válido.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "bodeguero/perfil_bodeguero";
        }

        if (!telefonoLimpio.matches("^[0-9]{7,15}$")) {
            model.addAttribute("error", "El teléfono debe contener solo números, entre 7 y 15 dígitos.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "bodeguero/perfil_bodeguero";
        }

        if (direccionLimpia.length() < 5) {
            model.addAttribute("error", "La dirección debe tener al menos 5 caracteres.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "bodeguero/perfil_bodeguero";
        }

        if (password != null && !password.isEmpty() && password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombre", usuario.getNombre());
            return "bodeguero/perfil_bodeguero";
        }

        boolean correoCambio = !correoLimpio.equals(usuario.getCorreo());

        if (correoCambio) {
            Usuario existente = usuarioRepository.findByCorreo(correoLimpio);
            if (existente != null && !existente.getId().equals(usuario.getId())) {
                model.addAttribute("error", "Ese correo ya está en uso por otro usuario.");
                model.addAttribute("usuario", usuario);
                model.addAttribute("nombre", usuario.getNombre());
                return "bodeguero/perfil_bodeguero";
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

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    correoLimpio,
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
        return "bodeguero/perfil_bodeguero";
    }
}