package com.dotacionestoronto.dotacionestoronto.facturacion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dotacionestoronto.dotacionestoronto.entity.DetalleVenta;
import com.dotacionestoronto.dotacionestoronto.entity.Producto;
import com.dotacionestoronto.dotacionestoronto.entity.Usuario;
import com.dotacionestoronto.dotacionestoronto.entity.Venta;
import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.DetalleFacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.EventoFactura;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.ResolucionFacturacion;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.DetalleFacturaElectronicaRepository;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.EventoFacturaRepository;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.FacturaElectronicaRepository;
import com.dotacionestoronto.dotacionestoronto.repository.DetalleVentaRepository;
import com.dotacionestoronto.dotacionestoronto.repository.ProductoRepository;
import com.dotacionestoronto.dotacionestoronto.repository.UsuarioRepository;
import com.dotacionestoronto.dotacionestoronto.repository.VentaRepository;

/**
 * Crea y persiste la factura electrónica a partir de una venta.
 *
 * Todas las operaciones de base de datos se ejecutan en transacciones
 * independientes (REQUIRES_NEW) para que un fallo en la DIAN nunca
 * deshaga la venta ni el pedido.
 */
@Service
public class FacturaEmisionService {

    private final FacturacionProperties props;
    private final FacturaElectronicaRepository facturaRepository;
    private final DetalleFacturaElectronicaRepository detalleRepository;
    private final EventoFacturaRepository eventoRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final NumeracionService numeracionService;
    private final CufeService cufeService;

    public FacturaEmisionService(FacturacionProperties props,
                                 FacturaElectronicaRepository facturaRepository,
                                 DetalleFacturaElectronicaRepository detalleRepository,
                                 EventoFacturaRepository eventoRepository,
                                 VentaRepository ventaRepository,
                                 DetalleVentaRepository detalleVentaRepository,
                                 UsuarioRepository usuarioRepository,
                                 ProductoRepository productoRepository,
                                 NumeracionService numeracionService,
                                 CufeService cufeService) {
        this.props = props;
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.eventoRepository = eventoRepository;
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.numeracionService = numeracionService;
        this.cufeService = cufeService;
    }

    // ==========================================================
    // CREACIÓN
    // ==========================================================

    /**
     * Genera la factura (sin transmitirla todavía).
     *
     * @return la factura creada con ítems y totales calculados
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FacturaElectronica crearDesdeVenta(Long idVenta) {

        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalStateException("La venta #" + idVenta + " no existe."));

        FacturaElectronica existente = facturaRepository.findByIdVenta(idVenta).orElse(null);
        if (existente != null) {
            return cargar(existente.getId());
        }

        // ---------- 1. Numeración autorizada ----------
        NumeracionService.NumeroAsignado numero = numeracionService.siguienteNumero();
        ResolucionFacturacion resolucion = numero.resolucion();

        // ---------- 2. Encabezado ----------
        FacturaElectronica f = new FacturaElectronica();
        f.setIdVenta(venta.getId());
        f.setIdPedido(venta.getIdPedido());
        f.setIdCliente(venta.getIdUsuario());
        f.setTipoDocumento("01");
        f.setNumeroFactura(numero.numeroFactura());
        f.setPrefijo(numero.prefijo());
        f.setConsecutivo(numero.consecutivo());
        f.setFechaEmision(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        f.setEstado(FacturaElectronica.ESTADO_BORRADOR);
        f.setMoneda("COP");
        f.setFormaPago(props.getImpuestos().getFormaPago());
        f.setMedioPago(props.getImpuestos().getMedioPago());
        f.setAmbiente(props.getAmbiente());
        f.setSoftwareId(props.getDian().getSoftwareId());
        f.setSimulado(Boolean.FALSE);
        f.setIntentos(0);

        // ---- Emisor ----
        FacturacionProperties.Emisor emisor = props.getEmisor();
        String dvEmisor = emisor.getDigitoVerificacion() != null && !emisor.getDigitoVerificacion().isBlank()
                ? emisor.getDigitoVerificacion()
                : cufeService.calcularDigitoVerificacion(emisor.getNit());

        f.setNitEmisor(cufeService.soloDigitos(emisor.getNit()));
        f.setDvEmisor(dvEmisor);
        f.setRazonSocialEmisor(emisor.getRazonSocial());
        f.setNombreComercialEmisor(emisor.getNombreComercial());
        f.setDireccionEmisor(emisor.getDireccion());
        f.setCiudadEmisor(emisor.getCiudad());
        f.setDepartamentoEmisor(emisor.getDepartamento());
        f.setTelefonoEmisor(emisor.getTelefono());
        f.setCorreoEmisor(emisor.getCorreo());
        f.setRegimenEmisor(emisor.getRegimen());
        f.setResponsabilidadesEmisor(emisor.getResponsabilidades());
        f.setActividadEconomicaEmisor(emisor.getActividadEconomica());

        // ---- Adquirente ----
        Usuario cliente = venta.getIdUsuario() != null
                ? usuarioRepository.findById(venta.getIdUsuario()).orElse(null)
                : null;

        if (cliente != null) {
            String documento = cufeService.soloDigitos(cliente.getDocumento());
            f.setNitAdquirente(documento);
            f.setTipoDocAdquirente(tipoDocumentoAdquirente(documento));
            f.setDvAdquirente("31".equals(f.getTipoDocAdquirente())
                    ? cufeService.calcularDigitoVerificacion(documento) : null);
            f.setRazonSocialAdquirente(cliente.getNombre());
            f.setCorreoAdquirente(cliente.getCorreo());
            f.setDireccionAdquirente(cliente.getDireccion());
            f.setTelefonoAdquirente(cliente.getTelefono());
            f.setCiudadAdquirente(props.getEmisor().getCiudad());
        } else {
            f.setNitAdquirente("222222222222");
            f.setTipoDocAdquirente("13");
            f.setRazonSocialAdquirente("consumidor final");
        }

        // ---- Resolución ----
        f.setClaveTecnica(resolucion.getClaveTecnica());
        f.setResolucionNumero(resolucion.getNumeroResolucion());
        f.setResolucionDesde(resolucion.getRangoDesde());
        f.setResolucionHasta(resolucion.getRangoHasta());
        f.setResolucionVigenciaDesde(resolucion.getVigenciaDesde());
        f.setResolucionVigenciaHasta(resolucion.getVigenciaHasta());

        // ---------- 3. Ítems e impuestos ----------
        List<DetalleVenta> detallesVenta = detalleVentaRepository.findByIdVenta(venta.getId());
        List<DetalleFacturaElectronica> detalles = new ArrayList<>();

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        int linea = 1;

        BigDecimal ivaPct = BigDecimal.valueOf(props.getImpuestos().getIvaPorcentaje());
        BigDecimal factor = BigDecimal.ONE.add(ivaPct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));

        for (DetalleVenta dv : detallesVenta) {
            BigDecimal cantidad = dv.getCantidad() != null ? BigDecimal.valueOf(dv.getCantidad()) : BigDecimal.ONE;
            BigDecimal precio = dv.getPrecioUnitario() != null ? dv.getPrecioUnitario() : BigDecimal.ZERO;

            BigDecimal baseLinea;
            BigDecimal ivaLinea;

            if (props.getImpuestos().isPreciosIncluyenIva()) {
                BigDecimal totalLineaConIva = precio.multiply(cantidad);
                baseLinea = totalLineaConIva.divide(factor, 2, RoundingMode.HALF_UP);
                ivaLinea = totalLineaConIva.subtract(baseLinea).setScale(2, RoundingMode.HALF_UP);
                precio = precio.divide(factor, 2, RoundingMode.HALF_UP);   // precio unitario sin IVA
            } else {
                baseLinea = precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
                ivaLinea = baseLinea.multiply(ivaPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            Producto producto = dv.getIdProducto() != null
                    ? productoRepository.findById(dv.getIdProducto()).orElse(null) : null;

            DetalleFacturaElectronica d = new DetalleFacturaElectronica();
            d.setNumeroLinea(linea++);
            d.setCodigoProducto(dv.getIdProducto() != null ? String.valueOf(dv.getIdProducto()) : "");
            d.setDescripcion(descripcionProducto(producto));
            d.setCantidad(cantidad);
            d.setUnidadMedida(props.getImpuestos().getUnidadMedida());
            d.setPrecioUnitario(precio);
            d.setDescuento(BigDecimal.ZERO);
            d.setIvaPorcentaje(ivaPct);
            d.setIvaValor(ivaLinea);
            d.setTotalLinea(baseLinea);
            detalles.add(d);

            totalBase = totalBase.add(baseLinea);
            totalIva = totalIva.add(ivaLinea);
        }

        // Si la venta no tiene detalle, se factura el total como un solo ítem
        if (detalles.isEmpty() && venta.getTotalPagado() != null) {
            BigDecimal total = BigDecimal.valueOf(venta.getTotalPagado());
            BigDecimal base = props.getImpuestos().isPreciosIncluyenIva()
                    ? total.divide(factor, 2, RoundingMode.HALF_UP) : total;
            BigDecimal iva = total.subtract(base).setScale(2, RoundingMode.HALF_UP);

            DetalleFacturaElectronica d = new DetalleFacturaElectronica();
            d.setNumeroLinea(1);
            d.setCodigoProducto("SIN-CODIGO");
            d.setDescripcion("Venta según pedido #" + venta.getIdPedido());
            d.setCantidad(BigDecimal.ONE);
            d.setUnidadMedida(props.getImpuestos().getUnidadMedida());
            d.setPrecioUnitario(base);
            d.setDescuento(BigDecimal.ZERO);
            d.setIvaPorcentaje(ivaPct);
            d.setIvaValor(iva);
            d.setTotalLinea(base);
            detalles.add(d);

            totalBase = base;
            totalIva = iva;
        }

        f.setValorBruto(totalBase.setScale(2, RoundingMode.HALF_UP));
        f.setTotalIva(totalIva.setScale(2, RoundingMode.HALF_UP));
        f.setTotalImpuestos(f.getTotalIva());
        f.setTotalFactura(f.getValorBruto().add(f.getTotalIva()));
        f.setDescuentoTotal(BigDecimal.ZERO);

        // ---------- 4. CUFE y código de seguridad del software ----------
        cufeService.calcularYAsignar(f);
        f.setSoftwareSecurityCode(cufeService.calcularSoftwareSecurityCode(
                props.getDian().getSoftwareId(), props.getDian().getSoftwarePin(), f.getNumeroFactura()));
        f.setQrUrl(cufeService.construirUrlQr(f));

        // ---------- 5. Persistencia ----------
        f.setEstado(FacturaElectronica.ESTADO_GENERADA);
        FacturaElectronica guardada = facturaRepository.save(f);

        for (DetalleFacturaElectronica d : detalles) {
            d.setIdFactura(guardada.getId());
        }
        detalleRepository.saveAll(detalles);

        eventoRepository.save(new EventoFactura(guardada.getId(), EventoFactura.TIPO_GENERACION,
                guardada.getEstado(),
                "Factura " + guardada.getNumeroFactura() + " generada. CUFE: " + guardada.getCufe(),
                null));

        guardada.setDetalles(detalles);
        return guardada;
    }

    /** Descripción legible del producto facturado. */
    private String descripcionProducto(Producto producto) {
        if (producto == null) {
            return "Producto";
        }
        StringBuilder s = new StringBuilder(producto.getNombre() != null ? producto.getNombre() : "Producto");
        if (producto.getTalla() != null && !producto.getTalla().isBlank()) {
            s.append(" - Talla ").append(producto.getTalla());
        }
        if (producto.getColor() != null && !producto.getColor().isBlank()) {
            s.append(" - ").append(producto.getColor());
        }
        return s.toString();
    }

    /** Tipo de documento del adquirente según la longitud del número. */
    private String tipoDocumentoAdquirente(String documento) {
        if (documento == null) {
            return "13";
        }
        if (documento.length() == 9) {
            return "31";     // NIT
        }
        if (documento.length() == 7 || documento.length() == 8) {
            return "13";     // Cédula
        }
        return "13";
    }

    // ==========================================================
    // ACTUALIZACIONES
    // ==========================================================

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarXml(Long idFactura, String xml, boolean firmado) {
        FacturaElectronica f = facturaRepository.findById(idFactura).orElseThrow();
        f.setXml(xml);
        f.setFirmado(firmado);
        f.setActualizadoEn(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        facturaRepository.save(f);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEnvio(Long idFactura, String estado, String trackId, String mensaje,
                               String respuesta, int intentos, String error) {
        FacturaElectronica f = facturaRepository.findById(idFactura).orElseThrow();
        f.setEstado(estado);
        f.setEstadoDian(estado);
        f.setTrackId(trackId != null ? trackId : f.getTrackId());
        f.setMensajeDian(mensaje);
        f.setFechaEnvio(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        f.setIntentos(intentos);
        f.setUltimoError(error);
        f.setSimulado("SIMULADO".equals(props.getModo()));
        if (FacturaElectronica.ESTADO_ACEPTADA.equals(estado) || FacturaElectronica.ESTADO_RECHAZADA.equals(estado)) {
            f.setFechaRespuesta(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        }
        f.setActualizadoEn(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        facturaRepository.save(f);

        eventoRepository.save(new EventoFactura(idFactura,
                FacturaElectronica.ESTADO_ERROR.equals(estado) ? EventoFactura.TIPO_ERROR : EventoFactura.TIPO_RESPUESTA,
                estado, mensaje, respuesta));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarConsulta(Long idFactura, String estado, String mensaje, String respuesta) {
        FacturaElectronica f = facturaRepository.findById(idFactura).orElseThrow();
        f.setEstado(estado);
        f.setEstadoDian(estado);
        f.setMensajeDian(mensaje);
        f.setFechaRespuesta(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        f.setActualizadoEn(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        facturaRepository.save(f);
        eventoRepository.save(new EventoFactura(idFactura, EventoFactura.TIPO_RESPUESTA, estado, mensaje, respuesta));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarError(Long idFactura, String mensaje, String detalle) {
        FacturaElectronica f = facturaRepository.findById(idFactura).orElseThrow();
        f.setEstado(FacturaElectronica.ESTADO_ERROR);
        f.setUltimoError(mensaje);
        f.setActualizadoEn(LocalDateTime.now(CufeService.ZONA_COLOMBIA));
        facturaRepository.save(f);
        eventoRepository.save(new EventoFactura(idFactura, EventoFactura.TIPO_ERROR,
                FacturaElectronica.ESTADO_ERROR, mensaje, detalle));
    }

    // ==========================================================
    // CONSULTAS
    // ==========================================================

    /** Carga la factura con sus ítems y eventos. */
    public FacturaElectronica cargar(Long idFactura) {
        FacturaElectronica f = facturaRepository.findById(idFactura).orElse(null);
        if (f == null) {
            return null;
        }
        f.setDetalles(detalleRepository.findByIdFacturaOrderByNumeroLineaAsc(idFactura));
        f.setEventos(eventoRepository.findByIdFacturaOrderByFechaAsc(idFactura));
        return f;
    }

    public FacturaElectronica buscarPorVenta(Long idVenta) {
        return facturaRepository.findByIdVenta(idVenta).map(f -> cargar(f.getId())).orElse(null);
    }

    public FacturaElectronica buscarPorNumero(String numero) {
        return facturaRepository.findByNumeroFactura(numero).map(f -> cargar(f.getId())).orElse(null);
    }

    public List<FacturaElectronica> listarTodas() {
        List<FacturaElectronica> facturas = facturaRepository.findAllByOrderByFechaEmisionDesc();
        facturas.forEach(f -> f.setDetalles(detalleRepository.findByIdFacturaOrderByNumeroLineaAsc(f.getId())));
        return facturas;
    }

    public List<FacturaElectronica> listarPorEstado(String estado) {
        List<FacturaElectronica> facturas = facturaRepository.findByEstadoOrderByFechaEmisionDesc(estado);
        facturas.forEach(f -> f.setDetalles(detalleRepository.findByIdFacturaOrderByNumeroLineaAsc(f.getId())));
        return facturas;
    }

    public LocalDate hoy() {
        return LocalDate.now(CufeService.ZONA_COLOMBIA);
    }
}
