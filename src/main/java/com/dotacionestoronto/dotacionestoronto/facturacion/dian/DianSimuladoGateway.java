package com.dotacionestoronto.dotacionestoronto.facturacion.dian;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.FacturaElectronica;

/**
 * Canal de simulación: no transmite nada a la DIAN, pero ejecuta todo el
 * proceso (XML UBL, CUFE, firma si hay certificado, PDF con QR) y marca la
 * factura como ACEPTADA indicando que es una simulación.
 *
 * Sirve para pruebas, demostraciones y para el proyecto académico.
 */
@Component
public class DianSimuladoGateway implements DianGateway {

    @Override
    public String modo() {
        return "SIMULADO";
    }

    @Override
    public ResultadoEnvio enviar(FacturaElectronica factura, String xmlFirmado) {
        String trackId = UUID.randomUUID().toString();

        if (xmlFirmado == null || !xmlFirmado.contains("<Invoice")) {
            return ResultadoEnvio.error("El XML generado no es un documento UBL válido.", null);
        }
        if (factura.getCufe() == null || factura.getCufe().length() != 96) {
            return ResultadoEnvio.error("El CUFE no fue calculado correctamente.", null);
        }

        return ResultadoEnvio.aceptada(
                trackId,
                "Documento aceptado en MODO SIMULACIÓN (no fue transmitido a la DIAN).",
                "{\"simulado\":true,\"zipKey\":\"" + trackId + "\",\"statusCode\":\"00\",\"statusDescription\":\"Procesado correctamente (simulación)\"}");
    }

    @Override
    public ResultadoConsulta consultar(String trackId) {
        return ResultadoConsulta.aceptada("Consulta simulada: documento aceptado.", null);
    }
}
