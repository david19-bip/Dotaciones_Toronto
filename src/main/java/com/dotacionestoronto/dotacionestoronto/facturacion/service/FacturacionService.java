package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.dian.DianGateway;
import com.dotacionestoronto.dotacionestoronto.facturacion.dian.ResultadoConsulta;
import com.dotacionestoronto.dotacionestoronto.facturacion.dian.ResultadoEnvio;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.FacturaElectronicaRepository;

/**
 * Orquestador del módulo de facturación automática.
 *
 * Flujo de emisión:
 *   1. Se crea la factura a partir de la venta (numeración + CUFE + ítems).
 *   2. Se construye el XML UBL 2.1 y se firma (XAdES-BES) si hay certificado.
 *   3. Se transmite por el canal configurado (SIMULADO / DIAN / PROVEEDOR).
 *   4. Se guarda el resultado, el trackId y la bitácora de eventos.
 *
 * Cualquier fallo deja la factura en estado ERROR con el detalle guardado, sin
 * afectar la venta ni el pedido; el administrador puede reintentar el envío.
 */
@Service
public class FacturacionService {

    private static final Logger log = LoggerFactory.getLogger(FacturacionService.class);

    private final FacturacionProperties props;
    private final FacturaEmisionService emision;
    private final XmlFacturaService xmlService;
    private final FirmaDigitalService firma;
    private final PdfFacturaService pdfService;
    private final FacturaElectronicaRepository facturaRepository;
    private final Map<String, DianGateway> gateways = new HashMap<>();

    public FacturacionService(FacturacionProperties props,
                              FacturaEmisionService emision,
                              XmlFacturaService xmlService,
                              FirmaDigitalService firma,
                              PdfFacturaService pdfService,
                              FacturaElectronicaRepository facturaRepository,
                              List<DianGateway> gatewaysDisponibles) {
        this.props = props;
        this.emision = emision;
        this.xmlService = xmlService;
        this.firma = firma;
        this.pdfService = pdfService;
        this.facturaRepository = facturaRepository;
        gatewaysDisponibles.forEach(g -> gateways.put(g.modo().toUpperCase(), g));
    }

    public DianGateway gatewayActual() {
        DianGateway gateway = gateways.get(props.getModo());
        if (gateway == null) {
            gateway = gateways.get("SIMULADO");
        }
        return gateway;
    }

    // ==========================================================
    // EMISIÓN AUTOMÁTICA
    // ==========================================================

    /**
     * Se invoca automáticamente cuando el vendedor marca un pedido como
     * "Entregado". Nunca lanza excepciones hacia el controlador.
     */
    public FacturaElectronica emitirDesdeVenta(Long idVenta) {
        if (!props.isHabilitada()) {
            log.info("Facturación deshabilitada (facturacion.habilitada=false). Venta #{} sin factura.", idVenta);
            return null;
        }
        try {
            FacturaElectronica f = emision.crearDesdeVenta(idVenta);
            if (f == null) {
                return null;
            }
            if (FacturaElectronica.ESTADO_ACEPTADA.equals(f.getEstado())) {
                return f;   // ya estaba facturada y aceptada
            }
            return generarYTransmitir(f);

        } catch (Exception e) {
            log.error("Error emitiendo la factura de la venta #{}: {}", idVenta, e.getMessage(), e);
            try {
                FacturaElectronica existente = emision.buscarPorVenta(idVenta);
                if (existente != null) {
                    emision.registrarError(existente.getId(), e.getMessage(), stack(e));
                }
            } catch (Exception ignored) {
                // si tampoco se puede registrar, se deja en el log
            }
            return null;
        }
    }

    /** Genera XML, firma y transmite una factura ya creada. */
    public FacturaElectronica generarYTransmitir(FacturaElectronica factura) {

        Long id = factura.getId();

        // ---------- 1. XML ----------
        String xml = xmlService.construir(factura);
        String xmlFirmado = firma.firmar(xml);
        boolean firmado = firma.estaFirmado(xmlFirmado);
        emision.guardarXml(id, xmlFirmado, firmado);

        // ---------- 2. Validación previa ----------
        if (props.isTransmisionReal() && !firmado) {
            String mensaje = "No hay certificado digital configurado: la DIAN rechaza documentos sin firma. "
                    + "Configure facturacion.dian.certificado-ruta.";
            emision.registrarEnvio(id, FacturaElectronica.ESTADO_ERROR, null, mensaje, null,
                    nvl(factura.getIntentos()) + 1, mensaje);
            return emision.cargar(id);
        }

        // ---------- 3. Transmisión ----------
        DianGateway gateway = gatewayActual();
        ResultadoEnvio resultado;
        try {
            resultado = gateway.enviar(factura, xmlFirmado);
        } catch (Exception e) {
            resultado = ResultadoEnvio.error(e.getMessage(), stack(e));
        }

        String estado = mapearEstado(resultado.estado());
        emision.registrarEnvio(id, estado, resultado.trackId(), resultado.mensaje(),
                resultado.respuestaCruda(), nvl(factura.getIntentos()) + 1, null);

        log.info("Factura {} enviada por canal {} -> {}", factura.getNumeroFactura(), gateway.modo(), estado);

        return emision.cargar(id);
    }

    // ==========================================================
    // REENVÍO Y CONSULTA
    // ==========================================================

    /** Reintenta el envío de una factura que quedó en ERROR o ENVIADA. */
    public FacturaElectronica reenviar(Long idFactura) {
        FacturaElectronica f = emision.cargar(idFactura);
        if (f == null) {
            throw new IllegalArgumentException("La factura #" + idFactura + " no existe.");
        }
        if (FacturaElectronica.ESTADO_ACEPTADA.equals(f.getEstado())) {
            return f;
        }
        // Se regenera el XML para reflejar el estado actual de configuración
        return generarYTransmitir(f);
    }

    /** Consulta el estado en la DIAN y actualiza la factura. */
    public FacturaElectronica consultarEstado(Long idFactura) {
        FacturaElectronica f = emision.cargar(idFactura);
        if (f == null) {
            throw new IllegalArgumentException("La factura #" + idFactura + " no existe.");
        }
        if (f.getTrackId() == null || f.getTrackId().isBlank()) {
            return f;
        }
        ResultadoConsulta r = gatewayActual().consultar(f.getTrackId());
        if (r.encontrado()) {
            emision.registrarConsulta(idFactura, mapearEstado(r.estado()), r.mensaje(), r.respuestaCruda());
        }
        return emision.cargar(idFactura);
    }

    // ==========================================================
    // DOCUMENTOS
    // ==========================================================

    public byte[] generarPdf(Long idFactura) {
        FacturaElectronica f = emision.cargar(idFactura);
        if (f == null) {
            throw new IllegalArgumentException("La factura #" + idFactura + " no existe.");
        }
        return pdfService.generar(f);
    }

    public String nombrePdf(Long idFactura) {
        FacturaElectronica f = emision.cargar(idFactura);
        return f != null ? pdfService.nombreArchivo(f) : "factura.pdf";
    }

    public String obtenerXml(Long idFactura) {
        FacturaElectronica f = emision.cargar(idFactura);
        if (f == null) {
            throw new IllegalArgumentException("La factura #" + idFactura + " no existe.");
        }
        if (f.getXml() != null && !f.getXml().isBlank()) {
            return f.getXml();
        }
        return xmlService.construir(f);
    }

    // ==========================================================
    // CONSULTAS DE APOYO
    // ==========================================================

    public FacturaElectronica buscar(Long idFactura) { return emision.cargar(idFactura); }

    public FacturaElectronica buscarPorVenta(Long idVenta) { return emision.buscarPorVenta(idVenta); }

    public FacturaElectronica buscarPorNumero(String numero) { return emision.buscarPorNumero(numero); }

    public List<FacturaElectronica> listarTodas() { return emision.listarTodas(); }

    public List<FacturaElectronica> listarPorEstado(String estado) { return emision.listarPorEstado(estado); }

    public long contar(String estado) { return facturaRepository.countByEstado(estado); }

    public long contarTodas() { return facturaRepository.count(); }

    public FacturacionProperties getProps() { return props; }

    /** Nombre del modo activo para mostrar en la interfaz. */
    public String modoActual() {
        return props.getModo();
    }

    public String estadoCertificado() {
        if (!firma.hayCertificado()) {
            return "SIN CERTIFICADO";
        }
        return firma.titularCertificado();
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    private String mapearEstado(String estadoCanal) {
        if (estadoCanal == null) {
            return FacturaElectronica.ESTADO_ERROR;
        }
        return switch (estadoCanal.toUpperCase()) {
            case "ACEPTADA" -> FacturaElectronica.ESTADO_ACEPTADA;
            case "RECHAZADA" -> FacturaElectronica.ESTADO_RECHAZADA;
            case "EN_PROCESO" -> FacturaElectronica.ESTADO_ENVIADA;
            default -> FacturaElectronica.ESTADO_ERROR;
        };
    }

    private int nvl(Integer valor) {
        return valor != null ? valor : 0;
    }

    private String stack(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        String texto = sw.toString();
        return texto.length() > 4000 ? texto.substring(0, 4000) : texto;
    }
}
