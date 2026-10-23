package com.dotacionestoronto.dotacionestoronto.facturacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración central del módulo de facturación electrónica.
 *
 * Todas las propiedades se leen del archivo application.properties con el
 * prefijo "facturacion.".
 *
 * Modos disponibles (facturacion.modo):
 *   SIMULADO  -> Genera XML UBL 2.1, CUFE, firma (si hay certificado) y PDF,
 *                pero NO transmite a la DIAN. La factura queda ACEPTADA marcada
 *                como simulación. Ideal para pruebas y exposición del proyecto.
 *   DIAN      -> Transmisión directa a los servicios web (SOAP) de la DIAN.
 *                Requiere certificado .p12, Software ID / PIN y habilitación.
 *   PROVEEDOR -> Transmisión a través de un proveedor tecnológico autorizado
 *                (API REST configurable).
 */
@Component
@ConfigurationProperties(prefix = "facturacion")
public class FacturacionProperties {

    /** Activa o desactiva la emisión automática de facturas. */
    private boolean habilitada = true;

    /** SIMULADO | DIAN | PROVEEDOR */
    private String modo = "SIMULADO";

    /** 1 = Producción, 2 = Pruebas (habilitación). */
    private String ambiente = "2";

    /** Genera automáticamente la factura al entregar un pedido. */
    private boolean emisionAutomatica = true;

    /** Crea las tablas del módulo al arrancar si no existen. */
    private boolean autoCrearTablas = true;

    private Emisor emisor = new Emisor();
    private Numeracion numeracion = new Numeracion();
    private Impuestos impuestos = new Impuestos();
    private Dian dian = new Dian();
    private Proveedor proveedor = new Proveedor();

    // ==========================================================
    // EMISOR
    // ==========================================================
    public static class Emisor {
        private String nit = "900123456";
        private String digitoVerificacion = "";       // si se deja vacío se calcula
        private String razonSocial = "DOTACIONES TORONTO S.A.S.";
        private String nombreComercial = "Dotaciones Toronto";
        private String direccion = "CL 1 # 1 - 1";
        private String ciudad = "Bogotá D.C.";
        private String ciudadCodigo = "11001";
        private String departamento = "Bogotá D.C.";
        private String departamentoCodigo = "11";
        private String pais = "Colombia";
        private String paisCodigo = "CO";
        private String telefono = "3000000000";
        private String correo = "facturacion@dotacionestoronto.com";
        /** 48 = Responsable de IVA, 49 = No responsable de IVA. */
        private String regimen = "48";
        /** Responsabilidades fiscales separadas por coma (RUT). */
        private String responsabilidades = "O-13,O-15,O-23";
        /** Código CIIU (actividad económica principal). */
        private String actividadEconomica = "4641";
        /** Fabricante del software (obligatorio en la representación gráfica). */
        private String fabricanteSoftware = "Dotaciones Toronto";

        public String getNit() { return nit; }
        public void setNit(String v) { this.nit = v; }
        public String getDigitoVerificacion() { return digitoVerificacion; }
        public void setDigitoVerificacion(String v) { this.digitoVerificacion = v; }
        public String getRazonSocial() { return razonSocial; }
        public void setRazonSocial(String v) { this.razonSocial = v; }
        public String getNombreComercial() { return nombreComercial; }
        public void setNombreComercial(String v) { this.nombreComercial = v; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String v) { this.direccion = v; }
        public String getCiudad() { return ciudad; }
        public void setCiudad(String v) { this.ciudad = v; }
        public String getCiudadCodigo() { return ciudadCodigo; }
        public void setCiudadCodigo(String v) { this.ciudadCodigo = v; }
        public String getDepartamento() { return departamento; }
        public void setDepartamento(String v) { this.departamento = v; }
        public String getDepartamentoCodigo() { return departamentoCodigo; }
        public void setDepartamentoCodigo(String v) { this.departamentoCodigo = v; }
        public String getPais() { return pais; }
        public void setPais(String v) { this.pais = v; }
        public String getPaisCodigo() { return paisCodigo; }
        public void setPaisCodigo(String v) { this.paisCodigo = v; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String v) { this.telefono = v; }
        public String getCorreo() { return correo; }
        public void setCorreo(String v) { this.correo = v; }
        public String getRegimen() { return regimen; }
        public void setRegimen(String v) { this.regimen = v; }
        public String getResponsabilidades() { return responsabilidades; }
        public void setResponsabilidades(String v) { this.responsabilidades = v; }
        public String getActividadEconomica() { return actividadEconomica; }
        public void setActividadEconomica(String v) { this.actividadEconomica = v; }
        public String getFabricanteSoftware() { return fabricanteSoftware; }
        public void setFabricanteSoftware(String v) { this.fabricanteSoftware = v; }
    }

    // ==========================================================
    // NUMERACIÓN AUTORIZADA POR LA DIAN
    // ==========================================================
    public static class Numeracion {
        private String prefijo = "DT";
        private long desde = 1;
        private long hasta = 5000;
        private long consecutivoActual = 0;    // 0 = iniciar en "desde"
        private String claveTecnica = "fc8eac422eba16e22ffd8c6f94b3f40a6e38162c";
        private String resolucionNumero = "18760000001";
        private String vigenciaDesde = "2025-01-01";
        private String vigenciaHasta = "2027-12-31";
        /** Semilla del rango para habilitación (DIAN). */
        private String testSetId = "";

        public String getPrefijo() { return prefijo; }
        public void setPrefijo(String v) { this.prefijo = v; }
        public long getDesde() { return desde; }
        public void setDesde(long v) { this.desde = v; }
        public long getHasta() { return hasta; }
        public void setHasta(long v) { this.hasta = v; }
        public long getConsecutivoActual() { return consecutivoActual; }
        public void setConsecutivoActual(long v) { this.consecutivoActual = v; }
        public String getClaveTecnica() { return claveTecnica; }
        public void setClaveTecnica(String v) { this.claveTecnica = v; }
        public String getResolucionNumero() { return resolucionNumero; }
        public void setResolucionNumero(String v) { this.resolucionNumero = v; }
        public String getVigenciaDesde() { return vigenciaDesde; }
        public void setVigenciaDesde(String v) { this.vigenciaDesde = v; }
        public String getVigenciaHasta() { return vigenciaHasta; }
        public void setVigenciaHasta(String v) { this.vigenciaHasta = v; }
        public String getTestSetId() { return testSetId; }
        public void setTestSetId(String v) { this.testSetId = v; }
    }

    // ==========================================================
    // IMPUESTOS
    // ==========================================================
    public static class Impuestos {
        /** Porcentaje de IVA aplicado a la venta. */
        private double ivaPorcentaje = 19.0;
        /** true = el precio de los productos ya incluye IVA. */
        private boolean preciosIncluyenIva = true;
        /** Código de forma de pago DIAN: 1 contra entrega, 10 efectivo, 42 consignación. */
        private String formaPago = "1";
        /** Código de medio de pago DIAN: 10 efectivo, 42 consignación bancaria, ZZZ otro. */
        private String medioPago = "10";
        /** Unidad de medida DIAN (94 = unidad). */
        private String unidadMedida = "94";

        public double getIvaPorcentaje() { return ivaPorcentaje; }
        public void setIvaPorcentaje(double v) { this.ivaPorcentaje = v; }
        public boolean isPreciosIncluyenIva() { return preciosIncluyenIva; }
        public void setPreciosIncluyenIva(boolean v) { this.preciosIncluyenIva = v; }
        public String getFormaPago() { return formaPago; }
        public void setFormaPago(String v) { this.formaPago = v; }
        public String getMedioPago() { return medioPago; }
        public void setMedioPago(String v) { this.medioPago = v; }
        public String getUnidadMedida() { return unidadMedida; }
        public void setUnidadMedida(String v) { this.unidadMedida = v; }
    }

    // ==========================================================
    // DIAN (transmisión directa)
    // ==========================================================
    public static class Dian {
        private String urlHabilitacion = "https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc";
        private String urlProduccion = "https://vpfe.dian.gov.co/WcfDianCustomerServices.svc";
        private String softwareId = "00000000-0000-0000-0000-000000000000";
        private String softwarePin = "00000";
        private String softwareNombre = "Dotaciones Toronto";
        /** Ruta del certificado .p12 (file:/... o classpath:/certificado/firma.p12). */
        private String certificadoRuta = "";
        private String certificadoPassword = "";
        /** Alias del certificado dentro del .p12 (opcional). */
        private String certificadoAlias = "";
        /** Timeout en segundos para las llamadas a la DIAN. */
        private int timeoutSegundos = 60;

        public String getUrlHabilitacion() { return urlHabilitacion; }
        public void setUrlHabilitacion(String v) { this.urlHabilitacion = v; }
        public String getUrlProduccion() { return urlProduccion; }
        public void setUrlProduccion(String v) { this.urlProduccion = v; }
        public String getSoftwareId() { return softwareId; }
        public void setSoftwareId(String v) { this.softwareId = v; }
        public String getSoftwarePin() { return softwarePin; }
        public void setSoftwarePin(String v) { this.softwarePin = v; }
        public String getSoftwareNombre() { return softwareNombre; }
        public void setSoftwareNombre(String v) { this.softwareNombre = v; }
        public String getCertificadoRuta() { return certificadoRuta; }
        public void setCertificadoRuta(String v) { this.certificadoRuta = v; }
        public String getCertificadoPassword() { return certificadoPassword; }
        public void setCertificadoPassword(String v) { this.certificadoPassword = v; }
        public String getCertificadoAlias() { return certificadoAlias; }
        public void setCertificadoAlias(String v) { this.certificadoAlias = v; }
        public int getTimeoutSegundos() { return timeoutSegundos; }
        public void setTimeoutSegundos(int v) { this.timeoutSegundos = v; }

        public String getUrl() {
            return "1".equals(getAmbiente()) ? urlProduccion : urlHabilitacion;
        }

        /** El ambiente vive en la clase externa; se resuelve por el setter de abajo. */
        private String ambiente = "2";
        public String getAmbiente() { return ambiente; }
        public void setAmbiente(String v) { this.ambiente = v; }
    }

    // ==========================================================
    // PROVEEDOR TECNOLÓGICO (API REST genérica)
    // ==========================================================
    public static class Proveedor {
        private String urlBase = "";
        private String token = "";
        /** Ruta del endpoint de emisión, relativa a urlBase. */
        private String rutaEmision = "/api/v1/invoices";
        private String rutaConsulta = "/api/v1/invoices/{trackId}";

        public String getUrlBase() { return urlBase; }
        public void setUrlBase(String v) { this.urlBase = v; }
        public String getToken() { return token; }
        public void setToken(String v) { this.token = v; }
        public String getRutaEmision() { return rutaEmision; }
        public void setRutaEmision(String v) { this.rutaEmision = v; }
        public String getRutaConsulta() { return rutaConsulta; }
        public void setRutaConsulta(String v) { this.rutaConsulta = v; }
    }

    // ==========================================================
    // GETTERS / SETTERS RAÍZ
    // ==========================================================
    public boolean isHabilitada() { return habilitada; }
    public void setHabilitada(boolean v) { this.habilitada = v; }

    public String getModo() { return modo; }
    public void setModo(String v) { this.modo = v != null ? v.toUpperCase() : "SIMULADO"; }

    public String getAmbiente() { return ambiente; }
    public void setAmbiente(String v) {
        this.ambiente = v;
        this.dian.setAmbiente(v);
    }

    public boolean isEmisionAutomatica() { return emisionAutomatica; }
    public void setEmisionAutomatica(boolean v) { this.emisionAutomatica = v; }

    public boolean isAutoCrearTablas() { return autoCrearTablas; }
    public void setAutoCrearTablas(boolean v) { this.autoCrearTablas = v; }

    public Emisor getEmisor() { return emisor; }
    public void setEmisor(Emisor v) { this.emisor = v; }
    public Numeracion getNumeracion() { return numeracion; }
    public void setNumeracion(Numeracion v) { this.numeracion = v; }
    public Impuestos getImpuestos() { return impuestos; }
    public void setImpuestos(Impuestos v) { this.impuestos = v; }
    public Dian getDian() { return dian; }
    public void setDian(Dian v) { this.dian = v; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor v) { this.proveedor = v; }

    /** true si el modo actual transmite realmente a un tercero. */
    public boolean isTransmisionReal() {
        return "DIAN".equals(modo) || "PROVEEDOR".equals(modo);
    }
}
