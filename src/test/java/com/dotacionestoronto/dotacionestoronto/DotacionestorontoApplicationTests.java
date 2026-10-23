package com.dotacionestoronto.dotacionestoronto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica que el contexto de Spring levante correctamente.
 *
 * Se ejecuta sobre una base de datos H2 en memoria para no depender de que el
 * MySQL local esté encendido al compilar/ejecutar las pruebas.
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:contexto;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.show-sql=false",
		"facturacion.auto-crear-tablas=false"
})
class DotacionestorontoApplicationTests {

	@Test
	void contextLoads() {
	}

}
