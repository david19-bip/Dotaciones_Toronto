package com.dotacionestoronto.dotacionestoronto.facturacion.dian;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dotacionestoronto.dotacionestoronto.facturacion.config.FacturacionProperties;
import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Canal alterno: envía la factura a un proveedor tecnológico autorizado por la
 * DIAN mediante una API REST. El proveedor se encarga de la firma, la
 * transmisión y la validación.
 *
 * El contrato es genérico (JSON con el XML en base64); si el proveedor
 * elegido usa otro formato, solo hay que ajustar {@link #construirCuerpo}.
 */
@Component
public class ProveedorTecnologicoGateway implements DianGateway {

    private final FacturacionProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProveedorTecnologicoGateway(FacturacionProperties props) {
        this.props = props;
    }

    @Override
    public String modo() {
        return "PROVEEDOR";
    }

    @Override
    public ResultadoEnvio enviar(FacturaElectronica factura, String xmlFirmado) {
        try {
            String url = props.getProveedor().getUrlBase() + props.getProveedor().getRutaEmision();

            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("numero", factura.getNumeroFactura());
            cuerpo.put("tipoDocumento", factura.getTipoDocumento());
            cuerpo.put("nitEmisor", factura.getNitEmisor());
            cuerpo.put("documentoAdquirente", factura.getNitAdquirente());
            cuerpo.put("cufe", factura.getCufe());
            cuerpo.put("ambiente", factura.getAmbiente());
            cuerpo.put("xml", Base64.getEncoder().encodeToString(xmlFirmado.getBytes(StandardCharsets.UTF_8)));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(props.getDian().getTimeoutSegundos()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + props.getProveedor().getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            String respuesta = response.body();
            if (response.statusCode() >= 400) {
                return ResultadoEnvio.error("El proveedor respondió HTTP " + response.statusCode(), respuesta);
            }

            JsonNode json = mapper.readTree(respuesta);
            String estado = texto(json, "estado", "status");
            String trackId = texto(json, "trackId", "id", "uuid");
            String mensaje = texto(json, "mensaje", "message", "descripcion");

            if (estado != null && estado.toUpperCase().contains("RECHAZ")) {
                return ResultadoEnvio.rechazada(mensaje != null ? mensaje : "Rechazado por el proveedor", respuesta);
            }
            if (estado != null && (estado.toUpperCase().contains("ACEPT") || estado.toUpperCase().contains("VALID"))) {
                return ResultadoEnvio.aceptada(trackId, mensaje != null ? mensaje : "Documento aceptado.", respuesta);
            }
            return ResultadoEnvio.enProceso(trackId, mensaje != null ? mensaje : "Documento enviado al proveedor.", respuesta);

        } catch (Exception e) {
            return ResultadoEnvio.error("Error enviando al proveedor tecnológico: " + e.getMessage(), null);
        }
    }

    @Override
    public ResultadoConsulta consultar(String trackId) {
        try {
            String ruta = props.getProveedor().getRutaConsulta().replace("{trackId}", trackId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(props.getProveedor().getUrlBase() + ruta))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + props.getProveedor().getToken())
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            String respuesta = response.body();
            JsonNode json = mapper.readTree(respuesta);
            String estado = texto(json, "estado", "status");
            String mensaje = texto(json, "mensaje", "message");

            if (estado != null && estado.toUpperCase().contains("ACEPT")) {
                return ResultadoConsulta.aceptada(mensaje, respuesta);
            }
            if (estado != null && estado.toUpperCase().contains("RECHAZ")) {
                return ResultadoConsulta.rechazada(mensaje, respuesta);
            }
            return ResultadoConsulta.enProceso(mensaje, respuesta);

        } catch (Exception e) {
            return ResultadoConsulta.error("Error consultando al proveedor: " + e.getMessage(), null);
        }
    }

    private String texto(JsonNode json, String... claves) {
        for (String clave : claves) {
            JsonNode nodo = json.get(clave);
            if (nodo != null && !nodo.isNull()) {
                return nodo.asText();
            }
        }
        return null;
    }
}
