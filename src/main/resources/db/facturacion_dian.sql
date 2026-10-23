-- =====================================================================
--  MÓDULO DE FACTURACIÓN ELECTRÓNICA - DIAN
--  Tablas del módulo. Se pueden ejecutar en phpMyAdmin sobre la base
--  de datos "dotacionestoronto" (son seguras: usan IF NOT EXISTS).
-- =====================================================================

-- ---------------------------------------------------------------------
-- Resoluciones de numeración autorizadas por la DIAN
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `resolucion_facturacion` (
  `id_resolucion` int(11) NOT NULL AUTO_INCREMENT,
  `tipo_documento` varchar(4) DEFAULT '01',
  `numero_resolucion` varchar(40) DEFAULT NULL,
  `prefijo` varchar(10) DEFAULT NULL,
  `rango_desde` bigint(20) DEFAULT NULL,
  `rango_hasta` bigint(20) DEFAULT NULL,
  `consecutivo_actual` bigint(20) DEFAULT NULL,
  `clave_tecnica` varchar(128) DEFAULT NULL,
  `vigencia_desde` date DEFAULT NULL,
  `vigencia_hasta` date DEFAULT NULL,
  `vigente` tinyint(1) DEFAULT 1,
  `notificacion` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_resolucion`),
  KEY `idx_resolucion_tipo` (`tipo_documento`, `vigente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- Facturas electrónicas
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `factura_electronica` (
  `id_factura` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_venta_fk` bigint(20) DEFAULT NULL,
  `id_pedido_fk` bigint(20) DEFAULT NULL,
  `id_cliente_fk` bigint(20) DEFAULT NULL,
  `tipo_documento` varchar(4) DEFAULT '01',
  `numero_factura` varchar(60) NOT NULL,
  `prefijo` varchar(10) DEFAULT NULL,
  `consecutivo` bigint(20) DEFAULT NULL,
  `fecha_emision` datetime DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'BORRADOR',
  `cufe` varchar(128) DEFAULT NULL,
  `cadena_cufe` text DEFAULT NULL,
  `valor_bruto` decimal(19,2) DEFAULT 0.00,
  `descuento_total` decimal(19,2) DEFAULT 0.00,
  `total_iva` decimal(19,2) DEFAULT 0.00,
  `total_impuestos` decimal(19,2) DEFAULT 0.00,
  `total_factura` decimal(19,2) DEFAULT 0.00,
  `moneda` varchar(3) DEFAULT 'COP',
  `forma_pago` varchar(4) DEFAULT '1',
  `medio_pago` varchar(4) DEFAULT '10',
  `nit_emisor` varchar(20) DEFAULT NULL,
  `dv_emisor` varchar(2) DEFAULT NULL,
  `razon_social_emisor` varchar(200) DEFAULT NULL,
  `nombre_comercial_emisor` varchar(200) DEFAULT NULL,
  `direccion_emisor` varchar(200) DEFAULT NULL,
  `ciudad_emisor` varchar(100) DEFAULT NULL,
  `departamento_emisor` varchar(100) DEFAULT NULL,
  `telefono_emisor` varchar(40) DEFAULT NULL,
  `correo_emisor` varchar(150) DEFAULT NULL,
  `regimen_emisor` varchar(6) DEFAULT NULL,
  `responsabilidades_emisor` varchar(100) DEFAULT NULL,
  `actividad_economica_emisor` varchar(10) DEFAULT NULL,
  `tipo_doc_adquirente` varchar(4) DEFAULT '13',
  `nit_adquirente` varchar(30) DEFAULT NULL,
  `dv_adquirente` varchar(2) DEFAULT NULL,
  `razon_social_adquirente` varchar(200) DEFAULT NULL,
  `correo_adquirente` varchar(150) DEFAULT NULL,
  `direccion_adquirente` varchar(200) DEFAULT NULL,
  `telefono_adquirente` varchar(40) DEFAULT NULL,
  `ciudad_adquirente` varchar(100) DEFAULT NULL,
  `clave_tecnica` varchar(128) DEFAULT NULL,
  `resolucion_numero` varchar(40) DEFAULT NULL,
  `resolucion_desde` bigint(20) DEFAULT NULL,
  `resolucion_hasta` bigint(20) DEFAULT NULL,
  `resolucion_vigencia_desde` date DEFAULT NULL,
  `resolucion_vigencia_hasta` date DEFAULT NULL,
  `software_id` varchar(100) DEFAULT NULL,
  `software_security_code` varchar(128) DEFAULT NULL,
  `ambiente` varchar(2) DEFAULT '2',
  `qr_url` text DEFAULT NULL,
  `xml` longtext DEFAULT NULL,
  `firmado` tinyint(1) DEFAULT 0,
  `estado_dian` varchar(40) DEFAULT NULL,
  `mensaje_dian` text DEFAULT NULL,
  `track_id` varchar(100) DEFAULT NULL,
  `fecha_envio` datetime DEFAULT NULL,
  `fecha_respuesta` datetime DEFAULT NULL,
  `intentos` int(11) DEFAULT 0,
  `ultimo_error` text DEFAULT NULL,
  `simulado` tinyint(1) DEFAULT 0,
  `creado_en` datetime DEFAULT NULL,
  `actualizado_en` datetime DEFAULT NULL,
  PRIMARY KEY (`id_factura`),
  UNIQUE KEY `uk_factura_numero` (`numero_factura`),
  KEY `idx_factura_venta` (`id_venta_fk`),
  KEY `idx_factura_cliente` (`id_cliente_fk`),
  KEY `idx_factura_estado` (`estado`),
  KEY `idx_factura_cufe` (`cufe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- Detalle (ítems) de la factura: es una foto de la venta al facturar
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `factura_electronica_detalle` (
  `id_detalle_factura` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_factura_fk` bigint(20) NOT NULL,
  `numero_linea` int(11) DEFAULT NULL,
  `codigo_producto` varchar(60) DEFAULT NULL,
  `descripcion` varchar(300) DEFAULT NULL,
  `cantidad` decimal(19,2) DEFAULT NULL,
  `unidad_medida` varchar(6) DEFAULT '94',
  `precio_unitario` decimal(19,2) DEFAULT NULL,
  `descuento` decimal(19,2) DEFAULT 0.00,
  `iva_porcentaje` decimal(6,2) DEFAULT 0.00,
  `iva_valor` decimal(19,2) DEFAULT 0.00,
  `total_linea` decimal(19,2) DEFAULT 0.00,
  PRIMARY KEY (`id_detalle_factura`),
  KEY `idx_detalle_factura` (`id_factura_fk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- Bitácora de eventos (auditoría del envío)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `factura_electronica_evento` (
  `id_evento` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_factura_fk` bigint(20) NOT NULL,
  `fecha` datetime DEFAULT NULL,
  `tipo` varchar(30) DEFAULT NULL,
  `estado` varchar(30) DEFAULT NULL,
  `mensaje` varchar(500) DEFAULT NULL,
  `payload` text DEFAULT NULL,
  PRIMARY KEY (`id_evento`),
  KEY `idx_evento_factura` (`id_factura_fk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- Resolución de ejemplo (AJUSTAR con los datos reales de la DIAN).
-- El prefijo, el rango y la clave técnica llegan en la resolución de
-- habilitación / autorización de numeración.
-- ---------------------------------------------------------------------
INSERT INTO `resolucion_facturacion`
  (`tipo_documento`, `numero_resolucion`, `prefijo`, `rango_desde`, `rango_hasta`,
   `consecutivo_actual`, `clave_tecnica`, `vigencia_desde`, `vigencia_hasta`, `vigente`)
SELECT '01', '18760000001', 'DT', 1, 5000, 0,
       'fc8eac422eba16e22ffd8c6f94b3f40a6e38162c',
       '2025-01-01', '2027-12-31', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `resolucion_facturacion` WHERE `tipo_documento` = '01');
