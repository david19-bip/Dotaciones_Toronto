package com.dotacionestoronto.dotacionestoronto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BrowserLauncher {

    @Value("${server.port:8080}")
    private String port;

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
