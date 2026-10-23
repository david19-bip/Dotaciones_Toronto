package com.dotacionestoronto.dotacionestoronto.facturacion.controller;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.CufeService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.FacturacionService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.NumeracionService;
import com.dotacionestoronto.dotacionestoronto.facturacion.service.QrService;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.ResolucionFacturacion;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;

import org.springframework.security.core.Authentication;

/**
 * Panel de administración del módulo de facturación electrónica:
 * listado, detalle, descarga del PDF/XML, reenvío y consulta de estado.
 */
@Controller
@RequestMapping("/admin/facturacion")
public class FacturacionController {

    private final FacturacionService facturacionService;
    private final FacturacionProperties props;
    private final QrService qrService;
    private final CufeService cufeService;
    private final NumeracionService numeracionService;
    private final UsuarioRepository usuarioRepository;

    public FacturacionController(FacturacionService facturacionService,
                                 FacturacionProperties props,
                                 QrService qrService,
                                 CufeService cufeService,
                                 NumeracionService numeracionService,
                                 UsuarioRepository usuarioRepository) {
        this.facturacionService = facturacionService;
        this.props = props;
        this.qrService = qrService;
        this.cufeService = cufeService;
        this.numeracionService = numeracionService;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================================
    // LISTADO
    // ==========================================================
    @GetMapping
    public String listado(@RequestParam(required = false) String estado, Model model, Authentication auth) {

        List<FacturaElectronica> facturas = (estado != null && !estado.isBlank())
                ? facturacionService.listarPorEstado(estado)
                : facturacionService.listarTodas();

        Map<String, Long> conteos = new LinkedHashMap<>();
        conteos.put("TODAS", facturacionService.contarTodas());
        conteos.put("ACEPTADA", facturacionService.contar(FacturaElectronica.ESTADO_ACEPTADA));
        conteos.put("ENVIADA", facturacionService.contar(FacturaElectronica.ESTADO_ENVIADA));
        conteos.put("RECHAZADA", facturacionService.contar(FacturaElectronica.ESTADO_RECHAZADA));
        conteos.put("ERROR", facturacionService.contar(FacturaElectronica.ESTADO_ERROR));

        model.addAttribute("facturas", facturas);
        model.addAttribute("conteos", conteos);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("modo", props.getModo());
        model.addAttribute("ambiente", props.getAmbiente());
        model.addAttribute("habilitada", props.isHabilitada());
        model.addAttribute("nombre", nombreUsuario(auth));
        return "admin/facturacion_admin";
    }

    // ==========================================================
    // DETALLE
    // ==========================================================
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, Authentication auth) {

        FacturaElectronica f = facturacionService.buscar(id);
        if (f == null) {
            return "redirect:/admin/facturacion";
        }

        String qrContenido = f.getQrUrl() != null ? f.getQrUrl() : cufeService.construirUrlQr(f);

        model.addAttribute("factura", f);
        model.addAttribute("qrDataUri", qrService.generarDataUri(qrContenido, 220));
        model.addAttribute("datosQr", cufeService.construirDatosQr(f));
        model.addAttribute("modo", props.getModo());
        model.addAttribute("cadenaCufe", f.getCadenaCufe());
        model.addAttribute("nombre", nombreUsuario(auth));
        return "admin/factura_detalle_admin";
    }

    // ==========================================================
    // ACCIONES
    // ==========================================================
    @PostMapping("/{id}/reenviar")
    public String reenviar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            FacturaElectronica f = facturacionService.reenviar(id);
            flash.addFlashAttribute("exito", "Factura " + f.getNumeroFactura() + " reenviada. Estado: " + f.getEstado());
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo reenviar: " + e.getMessage());
        }
        return "redirect:/admin/facturacion/" + id;
    }

    @PostMapping("/{id}/consultar")
    public String consultar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            FacturaElectronica f = facturacionService.consultarEstado(id);
            flash.addFlashAttribute("exito", "Estado consultado: " + f.getEstado());
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo consultar: " + e.getMessage());
        }
        return "redirect:/admin/facturacion/" + id;
    }

    /** Emite (o re-emite) la factura de una venta que no la tiene. */
    @PostMapping("/emitir-venta")
    public String emitirVenta(@RequestParam Long idVenta, RedirectAttributes flash) {
        try {
            FacturaElectronica f = facturacionService.emitirDesdeVenta(idVenta);
            if (f == null) {
                flash.addFlashAttribute("error", "No se pudo emitir la factura de la venta #" + idVenta + ".");
                return "redirect:/admin/facturacion";
            }
            flash.addFlashAttribute("exito", "Factura " + f.getNumeroFactura() + " generada. Estado: " + f.getEstado());
            return "redirect:/admin/facturacion/" + f.getId();
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error emitiendo la factura: " + e.getMessage());
            return "redirect:/admin/facturacion";
        }
    }

    // ==========================================================
    // ARCHIVOS
    // ==========================================================
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] pdf = facturacionService.generarPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("Factura-" + id + ".pdf", StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> xml(@PathVariable Long id) {
        FacturaElectronica f = facturacionService.buscar(id);
        String contenido = facturacionService.obtenerXml(id);
        String nombre = f != null ? f.getNitEmisor() + f.getTipoDocumento() + f.getNumeroFactura() + ".xml" : "factura.xml";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_XML)
                .body(contenido.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/{id}/qr.png")
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        FacturaElectronica f = facturacionService.buscar(id);
        String contenido = f != null && f.getQrUrl() != null ? f.getQrUrl() : "https://www.dian.gov.co";
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrService.generarPng(contenido, 320));
    }

    // ==========================================================
    // CONFIGURACIÓN / ESTADO DEL MÓDULO
    // ==========================================================
    @GetMapping("/configuracion")
    public String configuracion(Model model, Authentication auth) {

        ResolucionFacturacion resolucion = null;
        try {
            resolucion = numeracionService.resolucionVigente();
        } catch (Exception ignored) {
            // Sin resolución registrada
        }

        model.addAttribute("props", props);
        model.addAttribute("resolucion", resolucion);
        model.addAttribute("certificado", facturacionService.estadoCertificado());
        model.addAttribute("modoActual", props.getModo());
        model.addAttribute("nombre", nombreUsuario(auth));
        return "admin/facturacion_config_admin";
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    private String nombreUsuario(Authentication auth) {
        if (auth == null) {
            return "Administrador";
        }
        try {
            Usuario u = usuarioRepository.findByCorreo(auth.getName());
            return u != null ? u.getNombre() : auth.getName();
        } catch (Exception e) {
            return auth.getName();
        }
    }
}
