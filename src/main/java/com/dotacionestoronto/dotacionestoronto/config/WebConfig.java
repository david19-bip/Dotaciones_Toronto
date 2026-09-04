package com.dotacionestoronto.dotacionestoronto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve todo lo que esté en la carpeta física "uploads/productos"
        // bajo la URL "/images/productos/**"
        registry.addResourceHandler("/images/productos/**")
                .addResourceLocations("file:uploads/productos/");
    }
}