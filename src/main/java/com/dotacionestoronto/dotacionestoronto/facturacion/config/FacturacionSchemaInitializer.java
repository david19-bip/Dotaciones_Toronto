package com.dotacionestoronto.dotacionestoronto.facturacion.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import com.dotacionestoronto.dotacionestoronto.facturacion.entity.ResolucionFacturacion;
import com.dotacionestoronto.dotacionestoronto.facturacion.repository.ResolucionFacturacionRepository;

/**
 * Deja el módulo listo para funcionar en el primer arranque:
 *   1. Crea las tablas de facturación si no existen.
 *   2. Registra la resolución de numeración configurada en
 *      facturacion.numeracion.* si todavía no hay ninguna.
 *
 * Si el usuario prefiere manejar el esquema a mano, basta con poner
 * facturacion.auto-crear-tablas=false.
 */
@Component
public class FacturacionSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FacturacionSchemaInitializer.class);

    private final FacturacionProperties props;
    private final JdbcTemplate jdbcTemplate;
    private final ResolucionFacturacionRepository resolucionRepository;

    public FacturacionSchemaInitializer(FacturacionProperties props,
                                        JdbcTemplate jdbcTemplate,
                                        ResolucionFacturacionRepository resolucionRepository) {
        this.props = props;
        this.jdbcTemplate = jdbcTemplate;
        this.resolucionRepository = resolucionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (props.isAutoCrearTablas()) {
            try {
                ResourceDatabasePopulator populator =
                        new ResourceDatabasePopulator(new ClassPathResource("db/facturacion_dian.sql"));
                populator.setContinueOnError(true);
                populator.execute(jdbcTemplate.getDataSource());
                log.info("Tablas del módulo de facturación verificadas/creadas.");
            } catch (Exception e) {
                log.warn("No se pudieron crear automáticamente las tablas de facturación ({}). " +
                         "Ejecute manualmente src/main/resources/db/facturacion_dian.sql", e.getMessage());
            }
        }

        try {
            sembrarResolucion();
        } catch (Exception e) {
            log.warn("No se pudo verificar la resolución de facturación: {}", e.getMessage());
        }
    }

    /**
     * Si no existe una resolución vigente, se crea a partir de las
     * propiedades (útil para pruebas y para el modo simulación).
     */
    private void sembrarResolucion() {

        if (!props.isHabilitada()) {
            return;
        }
        if (resolucionRepository.findVigenteByTipo("01").isPresent()) {
            return;
        }

        FacturacionProperties.Numeracion n = props.getNumeracion();

        ResolucionFacturacion r = new ResolucionFacturacion();
        r.setTipoDocumento("01");
        r.setNumeroResolucion(n.getResolucionNumero());
        r.setPrefijo(n.getPrefijo());
        r.setRangoDesde(n.getDesde());
        r.setRangoHasta(n.getHasta());
        // Si se indica un consecutivo ya usado, se respeta; si no, arranca en el rango inicial
        r.setConsecutivoActual(n.getConsecutivoActual() > 0 ? n.getConsecutivoActual() : (n.getDesde() - 1));
        r.setClaveTecnica(n.getClaveTecnica());
        r.setVigenciaDesde(parsearFecha(n.getVigenciaDesde()));
        r.setVigenciaHasta(parsearFecha(n.getVigenciaHasta()));
        r.setVigente(Boolean.TRUE);
        r.setNotificacion("Creada automáticamente desde application.properties");

        resolucionRepository.save(r);
        log.info("Resolución de facturación registrada: prefijo {} del {} al {}",
                r.getPrefijo(), r.getRangoDesde(), r.getRangoHasta());
    }

    private LocalDate parsearFecha(String valor) {
        try {
            return valor != null && !valor.isBlank() ? LocalDate.parse(valor) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
