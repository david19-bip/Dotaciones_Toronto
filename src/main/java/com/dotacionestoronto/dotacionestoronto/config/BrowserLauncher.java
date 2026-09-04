package com.dotacionestoronto.dotacionestoronto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Componente utilitario que abre automáticamente el navegador del sistema
 * apuntando a la aplicación una vez que Spring Boot termina de arrancar.
 * Pensado para facilitar la ejecución local (por ejemplo, al iniciar con un
 * doble clic o desde un acceso directo), no tiene efecto en un despliegue
 * en servidor.
 */
@Component
public class BrowserLauncher {

    /** Puerto en el que quedó escuchando la aplicación (por defecto 8080). */
    @Value("${server.port:8080}")
    private String port;

    /**
     * Escucha el evento {@link ApplicationReadyEvent} (aplicación lista para
     * recibir peticiones) y abre el navegador por defecto del sistema
     * operativo en la URL local de la aplicación. Detecta si el sistema es
     * Windows, macOS o Linux para usar el comando adecuado. Si el navegador
     * no puede abrirse por cualquier motivo, el error se ignora
     * silenciosamente: esto nunca debe impedir que la aplicación arranque.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        try {
            String url = "http://localhost:" + port;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[] { "cmd", "/c", "start", "", url });
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[] { "open", url });
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[] { "xdg-open", url });
            }
        } catch (Exception e) {
            // La app no rompe si el navegador no se puede abrir.
        }
    }
}
