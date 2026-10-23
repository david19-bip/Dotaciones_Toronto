package com.dotacionestoronto.dotacionestoronto.facturacion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita las tareas programadas del módulo (consulta de estados en la DIAN).
 */
@Configuration
@EnableScheduling
public class FacturacionConfig {
}
