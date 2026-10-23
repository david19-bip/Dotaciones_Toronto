package com.dotacionestoronto.dotacionestoronto.facturacion.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Factura electrónica de venta (documento tributario).
 * Guarda una "foto" completa de los datos al momento de la emisión, de modo
 * que el XML, el CUFE y el PDF puedan reproducirse aunque luego cambie el
 * cliente o el producto.
 */
@Entity
@Table(name = "factura_electronica")
public class FacturaElectronica {

    // Estados del ciclo de vida
    public static final String ESTADO_BORRADOR  = "BORRADOR";
    public static final String ESTADO_GENERADA  = "GENERADA";
    public static final String ESTADO_ENVIADA   = "ENVIADA";
    public static final String ESTADO_ACEPTADA  = "ACEPTADA";
    public static final String ESTADO_RECHAZADA = "RECHAZADA";
    public static final String ESTADO_ERROR     = "ERROR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long id;

    @Column(name = "id_venta_fk")
    private Long idVenta;

    @Column(name = "id_pedido_fk")
    private Long idPedido;

    @Column(name = "id_cliente_fk")
    private Long idCliente;

    @Column(name = "tipo_documento", length = 4)
    private String tipoDocumento = "01";           // 01 = Factura electrónica de venta

    @Column(name = "numero_factura", length = 60, unique = true)
    private String numeroFactura;                  // Ej: DT00001

    @Column(name = "prefijo", length = 10)
    private String prefijo;

    @Column(name = "consecutivo")
    private Long consecutivo;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "estado", length = 20)
    private String estado = ESTADO_BORRADOR;

    @Column(name = "cufe", length = 128)
    private String cufe;

    @Lob
    @Column(name = "cadena_cufe", columnDefinition = "TEXT")
    private String cadenaCufe;

    // ------------------ Totales ------------------
    @Column(name = "valor_bruto", precision = 19, scale = 2)
    private BigDecimal valorBruto = BigDecimal.ZERO;      // sin impuestos

    @Column(name = "descuento_total", precision = 19, scale = 2)
    private BigDecimal descuentoTotal = BigDecimal.ZERO;

    @Column(name = "total_iva", precision = 19, scale = 2)
    private BigDecimal totalIva = BigDecimal.ZERO;

    @Column(name = "total_impuestos", precision = 19, scale = 2)
    private BigDecimal totalImpuestos = BigDecimal.ZERO;

    @Column(name = "total_factura", precision = 19, scale = 2)
    private BigDecimal totalFactura = BigDecimal.ZERO;

    @Column(name = "moneda", length = 3)
    private String moneda = "COP";

    @Column(name = "forma_pago", length = 4)
    private String formaPago = "1";

    @Column(name = "medio_pago", length = 4)
    private String medioPago = "10";

    // ------------------ Emisor (foto) ------------------
    @Column(name = "nit_emisor", length = 20)
    private String nitEmisor;

    @Column(name = "dv_emisor", length = 2)
    private String dvEmisor;

    @Column(name = "razon_social_emisor", length = 200)
    private String razonSocialEmisor;

    @Column(name = "nombre_comercial_emisor", length = 200)
    private String nombreComercialEmisor;

    @Column(name = "direccion_emisor", length = 200)
    private String direccionEmisor;

    @Column(name = "ciudad_emisor", length = 100)
    private String ciudadEmisor;

    @Column(name = "departamento_emisor", length = 100)
    private String departamentoEmisor;

    @Column(name = "telefono_emisor", length = 40)
    private String telefonoEmisor;

    @Column(name = "correo_emisor", length = 150)
    private String correoEmisor;

    @Column(name = "regimen_emisor", length = 6)
    private String regimenEmisor;

    @Column(name = "responsabilidades_emisor", length = 100)
    private String responsabilidadesEmisor;

    @Column(name = "actividad_economica_emisor", length = 10)
    private String actividadEconomicaEmisor;

    // ------------------ Adquirente (foto) ------------------
    @Column(name = "tipo_doc_adquirente", length = 4)
    private String tipoDocAdquirente = "13";

    @Column(name = "nit_adquirente", length = 30)
    private String nitAdquirente;

    @Column(name = "dv_adquirente", length = 2)
    private String dvAdquirente;

    @Column(name = "razon_social_adquirente", length = 200)
    private String razonSocialAdquirente;

    @Column(name = "correo_adquirente", length = 150)
    private String correoAdquirente;

    @Column(name = "direccion_adquirente", length = 200)
    private String direccionAdquirente;

    @Column(name = "telefono_adquirente", length = 40)
    private String telefonoAdquirente;

    @Column(name = "ciudad_adquirente", length = 100)
    private String ciudadAdquirente;

    // ------------------ Numeración / DIAN ------------------
    @Column(name = "clave_tecnica", length = 128)
    private String claveTecnica;

    @Column(name = "resolucion_numero", length = 40)
    private String resolucionNumero;

    @Column(name = "resolucion_desde")
    private Long resolucionDesde;

    @Column(name = "resolucion_hasta")
    private Long resolucionHasta;

    @Column(name = "resolucion_vigencia_desde")
    private LocalDate resolucionVigenciaDesde;

    @Column(name = "resolucion_vigencia_hasta")
    private LocalDate resolucionVigenciaHasta;

    @Column(name = "software_id", length = 100)
    private String softwareId;

    @Column(name = "software_security_code", length = 128)
    private String softwareSecurityCode;

    @Column(name = "ambiente", length = 2)
    private String ambiente;

    @Lob
    @Column(name = "qr_url", columnDefinition = "TEXT")
    private String qrUrl;

    @Lob
    @Column(name = "xml", columnDefinition = "LONGTEXT")
    private String xml;

    @Column(name = "firmado")
    private Boolean firmado = Boolean.FALSE;

    // ------------------ Resultado DIAN ------------------
    @Column(name = "estado_dian", length = 40)
    private String estadoDian;

    @Lob
    @Column(name = "mensaje_dian", columnDefinition = "TEXT")
    private String mensajeDian;

    @Column(name = "track_id", length = 100)
    private String trackId;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "intentos")
    private Integer intentos = 0;

    @Lob
    @Column(name = "ultimo_error", columnDefinition = "TEXT")
    private String ultimoError;

    @Column(name = "simulado")
    private Boolean simulado = Boolean.FALSE;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    // ------------------ Datos de apoyo (no persistidos) ------------------
    // Los ítems y los eventos se cargan con sus repositorios y se adjuntan aquí
    // para que las vistas y el PDF los puedan usar directamente.
    @Transient
    private List<DetalleFacturaElectronica> detalles = new ArrayList<>();

    @Transient
    private List<EventoFactura> eventos = new ArrayList<>();

    // ------------------ Helpers ------------------
    public boolean estaRespondida() {
        return ESTADO_ACEPTADA.equals(estado) || ESTADO_RECHAZADA.equals(estado);
    }

    public boolean sePuedeReenviar() {
        return !ESTADO_ACEPTADA.equals(estado);
    }

    // ------------------ Getters / Setters ------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdVenta() { return idVenta; }
    public void setIdVenta(Long v) { this.idVenta = v; }

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long v) { this.idPedido = v; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long v) { this.idCliente = v; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String v) { this.tipoDocumento = v; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String v) { this.numeroFactura = v; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String v) { this.prefijo = v; }

    public Long getConsecutivo() { return consecutivo; }
    public void setConsecutivo(Long v) { this.consecutivo = v; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime v) { this.fechaEmision = v; }

    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }

    public String getCufe() { return cufe; }
    public void setCufe(String v) { this.cufe = v; }

    public String getCadenaCufe() { return cadenaCufe; }
    public void setCadenaCufe(String v) { this.cadenaCufe = v; }

    public BigDecimal getValorBruto() { return valorBruto; }
    public void setValorBruto(BigDecimal v) { this.valorBruto = v; }

    public BigDecimal getDescuentoTotal() { return descuentoTotal; }
    public void setDescuentoTotal(BigDecimal v) { this.descuentoTotal = v; }

    public BigDecimal getTotalIva() { return totalIva; }
    public void setTotalIva(BigDecimal v) { this.totalIva = v; }

    public BigDecimal getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(BigDecimal v) { this.totalImpuestos = v; }

    public BigDecimal getTotalFactura() { return totalFactura; }
    public void setTotalFactura(BigDecimal v) { this.totalFactura = v; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String v) { this.moneda = v; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String v) { this.formaPago = v; }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String v) { this.medioPago = v; }

    public String getNitEmisor() { return nitEmisor; }
    public void setNitEmisor(String v) { this.nitEmisor = v; }

    public String getDvEmisor() { return dvEmisor; }
    public void setDvEmisor(String v) { this.dvEmisor = v; }

    public String getRazonSocialEmisor() { return razonSocialEmisor; }
    public void setRazonSocialEmisor(String v) { this.razonSocialEmisor = v; }

    public String getNombreComercialEmisor() { return nombreComercialEmisor; }
    public void setNombreComercialEmisor(String v) { this.nombreComercialEmisor = v; }

    public String getDireccionEmisor() { return direccionEmisor; }
    public void setDireccionEmisor(String v) { this.direccionEmisor = v; }

    public String getCiudadEmisor() { return ciudadEmisor; }
    public void setCiudadEmisor(String v) { this.ciudadEmisor = v; }

    public String getDepartamentoEmisor() { return departamentoEmisor; }
    public void setDepartamentoEmisor(String v) { this.departamentoEmisor = v; }

    public String getTelefonoEmisor() { return telefonoEmisor; }
    public void setTelefonoEmisor(String v) { this.telefonoEmisor = v; }

    public String getCorreoEmisor() { return correoEmisor; }
    public void setCorreoEmisor(String v) { this.correoEmisor = v; }

    public String getRegimenEmisor() { return regimenEmisor; }
    public void setRegimenEmisor(String v) { this.regimenEmisor = v; }

    public String getResponsabilidadesEmisor() { return responsabilidadesEmisor; }
    public void setResponsabilidadesEmisor(String v) { this.responsabilidadesEmisor = v; }

    public String getActividadEconomicaEmisor() { return actividadEconomicaEmisor; }
    public void setActividadEconomicaEmisor(String v) { this.actividadEconomicaEmisor = v; }

    public String getTipoDocAdquirente() { return tipoDocAdquirente; }
    public void setTipoDocAdquirente(String v) { this.tipoDocAdquirente = v; }

    public String getNitAdquirente() { return nitAdquirente; }
    public void setNitAdquirente(String v) { this.nitAdquirente = v; }

    public String getDvAdquirente() { return dvAdquirente; }
    public void setDvAdquirente(String v) { this.dvAdquirente = v; }

    public String getRazonSocialAdquirente() { return razonSocialAdquirente; }
    public void setRazonSocialAdquirente(String v) { this.razonSocialAdquirente = v; }

    public String getCorreoAdquirente() { return correoAdquirente; }
    public void setCorreoAdquirente(String v) { this.correoAdquirente = v; }

    public String getDireccionAdquirente() { return direccionAdquirente; }
    public void setDireccionAdquirente(String v) { this.direccionAdquirente = v; }

    public String getTelefonoAdquirente() { return telefonoAdquirente; }
    public void setTelefonoAdquirente(String v) { this.telefonoAdquirente = v; }

    public String getCiudadAdquirente() { return ciudadAdquirente; }
    public void setCiudadAdquirente(String v) { this.ciudadAdquirente = v; }

    public String getClaveTecnica() { return claveTecnica; }
    public void setClaveTecnica(String v) { this.claveTecnica = v; }

    public String getResolucionNumero() { return resolucionNumero; }
    public void setResolucionNumero(String v) { this.resolucionNumero = v; }

    public Long getResolucionDesde() { return resolucionDesde; }
    public void setResolucionDesde(Long v) { this.resolucionDesde = v; }

    public Long getResolucionHasta() { return resolucionHasta; }
    public void setResolucionHasta(Long v) { this.resolucionHasta = v; }

    public LocalDate getResolucionVigenciaDesde() { return resolucionVigenciaDesde; }
    public void setResolucionVigenciaDesde(LocalDate v) { this.resolucionVigenciaDesde = v; }

    public LocalDate getResolucionVigenciaHasta() { return resolucionVigenciaHasta; }
    public void setResolucionVigenciaHasta(LocalDate v) { this.resolucionVigenciaHasta = v; }

    public String getSoftwareId() { return softwareId; }
    public void setSoftwareId(String v) { this.softwareId = v; }

    public String getSoftwareSecurityCode() { return softwareSecurityCode; }
    public void setSoftwareSecurityCode(String v) { this.softwareSecurityCode = v; }

    public String getAmbiente() { return ambiente; }
    public void setAmbiente(String v) { this.ambiente = v; }

    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String v) { this.qrUrl = v; }

    public String getXml() { return xml; }
    public void setXml(String v) { this.xml = v; }

    public Boolean getFirmado() { return firmado; }
    public void setFirmado(Boolean v) { this.firmado = v; }

    public String getEstadoDian() { return estadoDian; }
    public void setEstadoDian(String v) { this.estadoDian = v; }

    public String getMensajeDian() { return mensajeDian; }
    public void setMensajeDian(String v) { this.mensajeDian = v; }

    public String getTrackId() { return trackId; }
    public void setTrackId(String v) { this.trackId = v; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime v) { this.fechaEnvio = v; }

    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime v) { this.fechaRespuesta = v; }

    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer v) { this.intentos = v; }

    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String v) { this.ultimoError = v; }

    public Boolean getSimulado() { return simulado; }
    public void setSimulado(Boolean v) { this.simulado = v; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime v) { this.creadoEn = v; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime v) { this.actualizadoEn = v; }

    public List<DetalleFacturaElectronica> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFacturaElectronica> v) { this.detalles = v; }

    public List<EventoFactura> getEventos() { return eventos; }
    public void setEventos(List<EventoFactura> v) { this.eventos = v; }
}
