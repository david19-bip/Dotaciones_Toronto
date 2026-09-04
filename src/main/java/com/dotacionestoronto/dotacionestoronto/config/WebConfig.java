package com.dotacionestoronto.dotacionestoronto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web adicional a la que provee Spring Boot por defecto.
 * Se usa para exponer como recurso HTTP la carpeta física donde se guardan
 * las imágenes de productos subidas por los usuarios, ya que esa carpeta
 * vive fuera del classpath de la aplicación (no se empaqueta dentro del
 * .jar) y por lo tanto no se sirve automáticamente.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Mapea la URL pública {@code /images/productos/**} a la carpeta física
     * {@code uploads/productos/} (relativa al directorio de ejecución de la
     * aplicación), de modo que las imágenes de productos cargadas por el
     * administrador puedan visualizarse sin necesidad de recompilar ni
     * reiniciar la aplicación.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/productos/**")
                .addResourceLocations("file:uploads/productos/");
    }
}