-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 10-09-2026 a las 21:45:00
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `dotacionestoronto`
--
DROP DATABASE IF EXISTS `dotacionestoronto`;
CREATE DATABASE `dotacionestoronto` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `dotacionestoronto`;

-- --------------------------------------------------------

--
-- 1. Estructura de tabla para la tabla `roles`
--
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `roles`
--
INSERT INTO `roles` (`id`, `nombre`) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_ODONTOLOGO'),
(3, 'ROLE_PACIENTE'),
(4, 'ROLE_VENDEDOR');

-- --------------------------------------------------------

--
-- 2. Estructura de tabla para la tabla `usuario` (Tabla unificada y limpia)
--
CREATE TABLE `usuario` (
  `id_usuario` bigint(20) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `correo` varchar(255) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `documento` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `id_rol_fk` bigint(20) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `UK_2mlfr087gb1ce55f2j87o74t` (`correo`),
  KEY `fk_usuario_rol` (`id_rol_fk`),
  CONSTRAINT `fk_usuario_rol` FOREIGN KEY (`id_rol_fk`) REFERENCES `roles` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--
INSERT INTO `usuario` (`id_usuario`, `password`, `correo`, `direccion`, `documento`, `estado`, `id_rol_fk`, `nombre`, `telefono`) VALUES
(1, '$2a$10$NHzEtUDh951E.ZEfGY/K6.haBXOZDTPIV/ofJ0ZXhCPKkN8XWIbn6', 'cristian@gmail.com', 'cra djwiqpd', '1561626326', 'Habilitado', 4, 'cristian', '156212623236'),
(2, '$2a$10$TQ4yvw9aSapLY5.82DAJ.OCNzsd9uw6z5XikWx9hzlto4qcC14LVi', 'daniel@gmail.com', 'av 34', '54256454', 'Habilitado', 3, 'montaperras', '32132141'),
(3, '$2a$10$rUhp650TiQmTvP9hFnL3luaH650iLLlbNpZuotJL4Ec3RLuhZId22', 'ronaldo@gmail.com', 'av 45', '2546325', 'Habilitado', 1, 'ronaldo', '321026515'),
(4, '$2a$10$hKRGJDy/5/bWucgnovvOPu628JeCrd1.8e2eFyBoc420d9r1POiqi', 'daniell@gmail.com', 'av 509', '10235155', 'Habilitado', 4, 'daniel', '3210451515'),
(5, '$2a$10$9ctA4osRjEpUi7CgUItApexoS07H/9b7SzPPf4Zi6YRzilvJUk6Y.', 'neymar@gmail.com', 'avenida 34', '102156165', 'Habilitado', 1, 'neymar', '321023554'),
(6, '$2a$10$fBZJJiT9NA8UfOgV4B5i7.f6ScuDbZY1lcpW2UU7WYBmr6b81oU0C', 'maca@gmail.com', 'avenida 765', '52156256', 'Habilitado', 1, 'macalister', '321564832'),
(7, '$2a$10$y2Tbs.JwA9FGVJDDXRuC3.Mo4573UmyWYNryELkwC6k1xGtfrbTp6', 'leo@gmail.com', 'calle 78', '542448', 'Habilitado', 4, 'leo', '321656525'),
(8, '$2a$10$YTLJf6TFjTk77wbGkrqQMeNfYkx1UBLk9BF5wUAcz/mSpGeF3lZse', 'suarez@gmail.com', 'ave 12', '6261256', 'Habilitado', 4, 'suarez', '362626'),
(9, '$2a$10$0ambw30gcR8fooUwmC0NF.gOnilrXyLFsAc6yXdhx1hRITSG2dwhq', 'benzema@gmail.com', 'avenida 67', '121313', 'Habilitado', 1, 'benzema', '3131313'),
(10, '$2a$10$tIHBMtH24qwWLeombtbeCuffvVXJCeXmMl6.E5QF.gS8kGLA5sD02', 'jhoan@gmail.com', 'calle 12', '3329414394103', 'Habilitado', 2, 'Jhoan', '8888888888'),
(11, '$2a$10$DVSq7xEsZTYRjZtvTusjh.GTkbxU9GFCajY9N5y7lDqlEeIjoZdu2', 'jefferson@gmail.com', 'jefferson@gmail.com', '8980989809', 'Habilitado', 1, 'jefferson', '72698148734'),
(12, '$2a$10$1Rs.SGFWnaDKCieYaF36pu9uolme7SmwGsRw7agLyvU/jV0bTzVGC', 'david@gmail.com', 'calle12', '32432423434343', 'Habilitado', 3, 'David', '23111313131'),
(13, '$2a$10$g5JhRKETI3omOWCTrb7F/ult9gCHMSalQ0hRWKRt7ghCEZa8Dh0Du', 'cristian.alt@gmail.com', 'calle12', '343243242', 'Habilitado', 4, 'Cristian', '4432432432');

-- --------------------------------------------------------

--
-- 3. Estructura de tabla para la tabla `usuario_roles`
--
CREATE TABLE `usuario_roles` (
  `usuario_id` bigint(20) NOT NULL,
  `rol_id` bigint(20) NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `usuario_roles_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `usuario_roles_ibfk_2` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuario_roles`
--
INSERT INTO `usuario_roles` (`usuario_id`, `rol_id`) VALUES
(1, 1),
(2, 3),
(3, 2);

-- --------------------------------------------------------

--
-- 4. Estructura de tabla para la tabla `producto`
--
CREATE TABLE `producto` (
  `id_producto` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `imagen` varchar(255) DEFAULT NULL,
  `inhabilitado` bit(1) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` decimal(38,2) NOT NULL,
  `talla` varchar(255) DEFAULT NULL,
  `categoria` varchar(100) DEFAULT 'General',
  PRIMARY KEY (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `producto`
--
INSERT INTO `producto` (`id_producto`, `color`, `estado`, `imagen`, `inhabilitado`, `nombre`, `precio`, `talla`, `categoria`) VALUES
(6, 'Rojo', 'Disponible', NULL, b'0', 'Camisa', 30000.00, 'M', 'Camisas/Polos'),
(7, 'Azul', 'Disponible', NULL, b'0', 'Overol', 30000.00, 'L', 'Protección Personal'),
(8, 'Cafe', 'Disponible', NULL, b'0', 'Casco', 20000.00, 'M', 'Protección Personal'),
(9, 'Azul', 'Disponible', NULL, b'0', 'Medias', 5000.00, 'S', 'Calzado'),
(10, 'Azul', 'Disponible', NULL, b'0', 'Zapatos', 30000.00, 'L', 'Calzado'),
(11, 'Azul', 'Disponible', NULL, b'0', 'Chaqueta ', 45000.00, 'S', 'Chaquetas'),
(12, 'Azul Oscuro', 'Disponible', NULL, b'0', 'Pantalón Jean', 30000.00, 'M', 'Pantalones');

-- --------------------------------------------------------

--
-- 5. Estructura de tabla para la tabla `inventario`
--
CREATE TABLE `inventario` (
  `id_inventario` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad_actual` int(11) DEFAULT NULL,
  `id_producto_fk` bigint(20) DEFAULT NULL,
  `inhabilitado` bit(1) DEFAULT NULL,
  `stock_minimo` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_inventario`),
  KEY `fk_inventario_producto` (`id_producto_fk`),
  CONSTRAINT `fk_inventario_producto` FOREIGN KEY (`id_producto_fk`) REFERENCES `producto` (`id_producto`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `inventario`
--
INSERT INTO `inventario` (`id_inventario`, `cantidad_actual`, `id_producto_fk`, `inhabilitado`, `stock_minimo`) VALUES
(10, 0, 7, b'1', 20),
(11, 10, 9, b'1', 5),
(12, 0, 8, b'0', 40),
(13, 50, 10, b'0', 20),
(14, 30, 6, b'0', 10),
(16, 40, 11, b'0', 20),
(17, 30, 12, b'0', 31);

-- --------------------------------------------------------

--
-- 6. Estructura de tabla para la tabla `movimientos_inventario`
--
CREATE TABLE `movimientos_inventario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_inventario` bigint(20) DEFAULT NULL,
  `nombre_producto` varchar(255) DEFAULT NULL,
  `tipo_movimiento` varchar(50) DEFAULT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `usuario_responsable` varchar(255) DEFAULT NULL,
  `fecha_hora` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `movimientos_inventario`
--
INSERT INTO `movimientos_inventario` (`id`, `id_inventario`, `nombre_producto`, `tipo_movimiento`, `cantidad`, `motivo`, `usuario_responsable`, `fecha_hora`) VALUES
(1, 3, 'Camiseta - S - Rojo', 'SALIDA', 45, 'Porque si', 'suarez', '2026-09-02 20:36:13'),
(2, 6, 'Botas - M - Cafe', 'ENTRADA', 50, 'Registro inicial de inventario', 'suarez', '2026-09-02 21:12:13'),
(3, 7, 'Overol - XL - Verde', 'ENTRADA', 30, 'Registro inicial de inventario', 'suarez', '2026-09-02 21:17:05'),
(4, 7, 'Overol - XL - Verde', 'SALIDA', 30, 'Si', 'suarez', '2026-09-02 21:31:53'),
(5, 1, 'Camiseta - S - Rojo', 'AJUSTE', 0, 'Porque asi es la vida', 'suarez', '2026-09-02 22:16:59'),
(6, 8, 'Botas - M - Cafe', 'ENTRADA', 50, 'Registro inicial de inventario', 'suarez', '2026-09-02 22:32:27'),
(7, 6, 'Botas - M - Cafe', 'SALIDA', 30, 'Porque asi es la vida', 'suarez', '2026-09-08 18:10:19'),
(8, 9, 'Camisa - M - Rojo', 'ENTRADA', 20, 'Registro inicial de inventario', 'suarez', '2026-09-08 21:26:44'),
(9, 10, 'Overol - L - Azul', 'ENTRADA', 20, 'Registro inicial de inventario', 'suarez', '2026-09-08 21:27:08'),
(10, 11, 'Medias - S - Azul', 'ENTRADA', 10, 'Registro inicial de inventario', 'suarez', '2026-09-08 21:33:37'),
(11, 12, 'Casco - M - Cafe', 'ENTRADA', 50, 'Registro inicial de inventario', 'suarez', '2026-09-08 21:49:42'),
(12, 12, 'Casco - M - Cafe', 'SALIDA', 50, 'SIZAS', 'suarez', '2026-09-08 21:50:10'),
(13, 13, 'Zapatos - L - Azul', 'ENTRADA', 50, 'Registro inicial de inventario', 'suarez', '2026-09-10 17:33:07'),
(14, 10, 'Overol - L - Azul', 'ENTRADA', 10, 'SIZAS', 'benzema', '2026-09-10 18:32:51'),
(15, 10, 'Overol - L - Azul', 'SALIDA', 30, 'Situación actual del producto.', 'benzema', '2026-09-10 18:36:33'),
(16, 16, 'Chaqueta  - S - Azul', 'ENTRADA', 40, 'Registro inicial de inventario (Admin)', 'benzema', '2026-09-10 18:42:44'),
(17, 17, 'Pantalón Jean - M - Azul Oscuro', 'ENTRADA', 30, 'Registro inicial de inventario (Admin)', 'benzema', '2026-09-10 18:48:18');

-- --------------------------------------------------------

--
-- 7. Estructura de tabla para la tabla `pedido`
--
CREATE TABLE `pedido` (
  `id_pedido` bigint(20) NOT NULL AUTO_INCREMENT,
  `estado` varchar(255) DEFAULT NULL,
  `fecha_pedido` datetime(6) DEFAULT NULL,
  `id_usuario_fk` bigint(20) DEFAULT NULL,
  `oculto` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id_pedido`),
  KEY `fk_pedido_usuario` (`id_usuario_fk`),
  CONSTRAINT `fk_pedido_usuario` FOREIGN KEY (`id_usuario_fk`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 8. Estructura de tabla para la tabla `venta`
--
CREATE TABLE `venta` (
  `id_venta` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_venta` datetime(6) DEFAULT NULL,
  `id_pedido_fk` bigint(20) DEFAULT NULL,
  `id_usuario_fk` bigint(20) DEFAULT NULL,
  `total_pagado` double DEFAULT NULL,
  PRIMARY KEY (`id_venta`),
  KEY `fk_venta_pedido` (`id_pedido_fk`),
  KEY `fk_venta_usuario` (`id_usuario_fk`),
  CONSTRAINT `fk_venta_pedido` FOREIGN KEY (`id_pedido_fk`) REFERENCES `pedido` (`id_pedido`) ON DELETE SET NULL,
  CONSTRAINT `fk_venta_usuario` FOREIGN KEY (`id_usuario_fk`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 9. Estructura de tabla para la tabla `devolucion`
--
CREATE TABLE `devolucion` (
  `id_devolucion` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` int(11) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `fecha_devolucion` date DEFAULT NULL,
  `id_detalle_venta_fk` bigint(20) DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_devolucion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;