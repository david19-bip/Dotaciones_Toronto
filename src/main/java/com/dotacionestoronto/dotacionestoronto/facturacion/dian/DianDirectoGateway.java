package com.dotacionestoronto.dotacionestoronto.facturacion.dian;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;

/**
 * Transmisión directa a los servicios web (SOAP 1.2) de la DIAN.
 *
 * Operaciones utilizadas del WCF "IWcfDianCustomerServices":
 *   - SendTestSetAsync  (ambiente de habilitación, set de pruebas)
 *   - SendBillAsync     (envío del documento en producción)
 *   - GetStatusZip      (consulta del resultado del envío)
 *
 * La autenticación es mutua (mTLS) con el certificado digital del facturador
 * configurado en facturacion.dian.certificado-ruta.
 */
@Component
public class DianDirectoGateway implements DianGateway {

    private static final String NS_WCF = "http://wcf.dian.colombia";

    private final FacturacionProperties props;

    public DianDirectoGateway(FacturacionProperties props) {
        this.props = props;
    }

    @Override
    public String modo() {
        return "DIAN";
    }

    // ==========================================================
    // ENVÍO
    // ==========================================================
    @Override
    public ResultadoEnvio enviar(FacturaElectronica factura, String xmlFirmado) {

        try {
            String nombreArchivo = factura.getNitEmisor() + factura.getTipoDocumento()
                    + factura.getNumeroFactura() + ".xml";
            String contenido = Base64.getEncoder().encodeToString(xmlFirmado.getBytes(StandardCharsets.UTF_8));

            boolean habilitacion = "2".equals(props.getAmbiente());
            String testSetId = props.getNumeracion().getTestSetId();

            ResultadoEnvio resultado;
            if (habilitacion && testSetId != null && !testSetId.isBlank()) {
                String cuerpo = "<wcf:SendTestSetAsync>"
                        + "<wcf:fileName>" + esc(nombreArchivo) + "</wcf:fileName>"
                        + "<wcf:contentFile>" + contenido + "</wcf:contentFile>"
                        + "<wcf:testSetId>" + esc(testSetId) + "</wcf:testSetId>"
                        + "</wcf:SendTestSetAsync>";
                String respuesta = invocar("SendTestSetAsync", cuerpo);
                resultado = interpretarEnvio(respuesta);
            } else {
                String cuerpo = "<wcf:SendBillAsync>"
                        + "<wcf:fileName>" + esc(nombreArchivo) + "</wcf:fileName>"
                        + "<wcf:contentFile>" + contenido + "</wcf:contentFile>"
                        + "</wcf:SendBillAsync>";
                String respuesta = invocar("SendBillAsync", cuerpo);
                resultado = interpretarEnvio(respuesta);
            }
            return resultado;

        } catch (Exception e) {
            return ResultadoEnvio.error("Error de comunicación con la DIAN: " + e.getMessage(), null);
        }
    }

    private ResultadoEnvio interpretarEnvio(String respuestaXml) {
        if (respuestaXml == null) {
            return ResultadoEnvio.error("La DIAN no devolvió respuesta.", null);
        }

        String zipKey = valor(respuestaXml, "ZipKey");
        if (zipKey == null || zipKey.isBlank()) {
            zipKey = valor(respuestaXml, "TrackId");
        }
        String error = valor(respuestaXml, "ErrorMessage");
        String statusCode = valor(respuestaXml, "StatusCode");
        String statusDescription = valor(respuestaXml, "StatusDescription");

        if (error != null && !error.isBlank()) {
            return ResultadoEnvio.rechazada(error, respuestaXml);
        }
        if (zipKey != null && !zipKey.isBlank()) {
            return ResultadoEnvio.enProceso(zipKey,
                    "Documento recibido por la DIAN. ZipKey: " + zipKey
                            + (statusDescription != null ? " · " + statusDescription : ""),
                    respuestaXml);
        }
        if (statusCode != null) {
            return ResultadoEnvio.enProceso(null,
                    "La DIAN respondió con código " + statusCode
                            + (statusDescription != null ? ": " + statusDescription : ""),
                    respuestaXml);
        }
        return ResultadoEnvio.error("No se pudo interpretar la respuesta de la DIAN.", respuestaXml);
    }

    // ==========================================================
    // CONSULTA DE ESTADO
    // ==========================================================
    @Override
    public ResultadoConsulta consultar(String trackId) {
        try {
            String cuerpo = "<wcf:GetStatusZip><wcf:trackId>" + esc(trackId) + "</wcf:trackId></wcf:GetStatusZip>";
            String respuesta;
            try {
                respuesta = invocar("GetStatusZip", cuerpo);
            } catch (Exception e) {
                // Algunos canales solo exponen GetStatus
                respuesta = invocar("GetStatus", "<wcf:trackId>" + esc(trackId) + "</wcf:trackId>");
            }
            return interpretarConsulta(respuesta);
        } catch (Exception e) {
            return ResultadoConsulta.error("Error consultando la DIAN: " + e.getMessage(), null);
        }
    }

    private ResultadoConsulta interpretarConsulta(String respuestaXml) {
        if (respuestaXml == null) {
            return ResultadoConsulta.error("La DIAN no devolvió respuesta.", null);
        }
        String isValid = valor(respuestaXml, "IsValid");
        String statusCode = valor(respuestaXml, "StatusCode");
        String statusDescription = valor(respuestaXml, "StatusDescription");
        String statusMessage = valor(respuestaXml, "StatusMessage");
        String error = valor(respuestaXml, "ErrorMessage");

        String detalle = "Código: " + (statusCode != null ? statusCode : "-")
                + " · " + (statusDescription != null ? statusDescription : "")
                + (statusMessage != null ? " · " + statusMessage : "");

        if ("true".equalsIgnoreCase(isValid)) {
            return ResultadoConsulta.aceptada("Documento aceptado por la DIAN. " + detalle, respuestaXml);
        }
        if ("false".equalsIgnoreCase(isValid)) {
            return ResultadoConsulta.rechazada("Documento rechazado por la DIAN. " + detalle, respuestaXml);
        }
        if (error != null && !error.isBlank()) {
            return ResultadoConsulta.error(error, respuestaXml);
        }
        return ResultadoConsulta.enProceso("El documento aún está en proceso en la DIAN. " + detalle, respuestaXml);
    }

    // ==========================================================
    // INFRAESTRUCTURA HTTP/SOAP
    // ==========================================================
    private String invocar(String operacion, String cuerpo) throws Exception {

        String envelope = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
                + " xmlns:wcf=\"" + NS_WCF + "\">"
                + "<soap:Body>" + cuerpo + "</soap:Body>"
                + "</soap:Envelope>";

        HttpClient cliente = construirCliente();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getDian().getUrl()))
                .timeout(Duration.ofSeconds(props.getDian().getTimeoutSegundos()))
                .header("Content-Type", "application/soap+xml; charset=utf-8")
                .header("SOAPAction", "\"" + NS_WCF + "/IWcfDianCustomerServices/" + operacion + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = cliente.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() >= 400 && response.body() == null) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    /**
     * Cliente HTTP con certificado de cliente (autenticación mutua).
     * Si no hay certificado configurado se conecta sin él (solo sirve para
     * probar la conectividad; la DIAN rechazará el envío).
     */
    private HttpClient construirCliente() throws Exception {

        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(props.getDian().getTimeoutSegundos()));

        if (props.getDian().getCertificadoRuta() != null && !props.getDian().getCertificadoRuta().isBlank()) {
            KeyStore ks = cargarKeyStore();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, props.getDian().getCertificadoPassword() != null
                    ? props.getDian().getCertificadoPassword().toCharArray() : new char[0]);

            TrustManager[] trustAll = new TrustManager[]{ new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }};

            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), trustAll, new SecureRandom());
            builder.sslContext(ssl);
        }

        return builder.build();
    }

    private KeyStore cargarKeyStore() throws Exception {
        String ruta = props.getDian().getCertificadoRuta();
        byte[] contenido;
        if (ruta.startsWith("classpath:")) {
            try (var is = getClass().getClassLoader().getResourceAsStream(ruta.substring("classpath:".length()))) {
                if (is == null) {
                    throw new IllegalStateException("Certificado no encontrado en classpath");
                }
                contenido = is.readAllBytes();
            }
        } else {
            String limpia = ruta.startsWith("file:") ? ruta.substring("file:".length()) : ruta;
            contenido = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(limpia));
        }
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(contenido), props.getDian().getCertificadoPassword() != null
                ? props.getDian().getCertificadoPassword().toCharArray() : new char[0]);
        return ks;
    }

    /** Extrae el texto del primer elemento con ese nombre local. */
    private String valor(String xml, String nombreLocal) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList nodos = doc.getElementsByTagNameNS("*", nombreLocal);
            if (nodos.getLength() > 0) {
                return nodos.item(0).getTextContent();
            }
            nodos = doc.getElementsByTagName(nombreLocal);
            if (nodos.getLength() > 0) {
                return nodos.item(0).getTextContent();
            }
        } catch (Exception e) {
            // Respuesta no XML: se devuelve null
        }
        return null;
    }

    private String esc(String valor) {
        return valor == null ? "" : valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
