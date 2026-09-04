package com.dotacionestoronto.dotacionestoronto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Dotaciones Toronto.
 * <p>
 * Es una aplicación Spring Boot monolítica (MVC + Thymeleaf) que gestiona el
 * catálogo de productos, el carrito de compra, los pedidos, las ventas, el
 * inventario y las devoluciones de una empresa de dotaciones/EPP, con
 * autenticación y autorización por roles (Administrador, Cliente, Vendedor,
 * Bodeguero) mediante Spring Security.
 */
@SpringBootApplication
public class DotacionestorontoApplication {

	/**
	 * Arranca el contenedor Spring Boot (Tomcat embebido) y toda la
	 * configuración de la aplicación.
	 *
	 * @param args argumentos de línea de comandos (no se usan actualmente)
	 */
	public static void main(String[] args) {
		SpringApplication.run(DotacionestorontoApplication.class, args);
	}

}

