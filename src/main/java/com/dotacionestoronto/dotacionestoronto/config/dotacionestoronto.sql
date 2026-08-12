CREATE DATABASE dotacionestoronto;
USE dotacionestoronto;

CREATE TABLE rol (
id_rol INT AUTO_INCREMENT PRIMARY KEY,
nombre_rol VARCHAR(50),
descripcion_rol VARCHAR(150)
);

CREATE TABLE usuario (
id_usuario INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL,
documento VARCHAR(20) UNIQUE,
correo VARCHAR(100) UNIQUE,
telefono VARCHAR(20),
password VARCHAR(255),
direccion VARCHAR(150),
estado ENUM('Habilitado','Inhabilitado') DEFAULT 'Habilitado',
id_rol_fk INT DEFAULT 2,
FOREIGN KEY(id_rol_fk) REFERENCES rol(id_rol)
);

CREATE TABLE producto (
id_producto INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL,
precio DECIMAL(10,2) NOT NULL,
talla VARCHAR(10),
color VARCHAR(30),
imagen VARCHAR(255),
inhabilitado TINYINT(1) DEFAULT 0,
estado ENUM('Disponible','Agotado') DEFAULT 'Disponible'
);

CREATE TABLE inventario (
id_inventario INT AUTO_INCREMENT PRIMARY KEY,
id_producto_fk INT,
cantidad_actual INT,
stock_minimo INT,
inhabilitado TINYINT(1) NOT NULL DEFAULT 0,
FOREIGN KEY (id_producto_fk) REFERENCES producto(id_producto)
);

CREATE TABLE alerta_stock(
id_alerta INT AUTO_INCREMENT PRIMARY KEY,
id_producto_fk INT,
nivel_stock INT,
fecha_alerta DATE,
FOREIGN KEY (id_producto_fk) REFERENCES producto(id_producto)
);

CREATE TABLE descuento (
id_descuento INT AUTO_INCREMENT PRIMARY KEY,
id_producto_fk INT,
tipo_descuento VARCHAR(50),
valor_descuento DECIMAL(10,2),
FOREIGN KEY (id_producto_fk) REFERENCES producto(id_producto)
);

CREATE TABLE carrito (

id_carrito INT AUTO_INCREMENT PRIMARY KEY,
id_usuario_fk INT,
fecha_creacion DATE,
FOREIGN KEY (id_usuario_fk) REFERENCES usuario(id_usuario)
);

CREATE TABLE detalle_carrito (
id_detalle_carrito INT AUTO_INCREMENT PRIMARY KEY,
id_carrito_fk INT,
id_producto_fk INT,
cantidad INT,
FOREIGN KEY (id_carrito_fk) REFERENCES carrito(id_carrito),
FOREIGN KEY (id_producto_fk) REFERENCES producto(id_producto)
);

CREATE TABLE pedido (
id_pedido INT AUTO_INCREMENT PRIMARY KEY,
id_usuario_fk INT,
fecha_pedido DATETIME,
estado VARCHAR(50),
oculto TINYINT(1) DEFAULT 0,
FOREIGN KEY (id_usuario_fk) REFERENCES usuario(id_usuario)
);

CREATE TABLE detalle_pedido (
id_detalle_pedido INT AUTO_INCREMENT PRIMARY KEY,
id_pedido_fk INT,
id_producto_fk INT,
precio_unitario DECIMAL(10,2),

cantidad INT,
FOREIGN KEY (id_pedido_fk) REFERENCES pedido(id_pedido),
FOREIGN KEY (id_producto_fk) REFERENCES producto(id_producto)
);

CREATE TABLE venta (
id_venta INT AUTO_INCREMENT PRIMARY KEY,
id_usuario_fk INT,
fecha_venta DATETIME,
total_pagado DECIMAL(10,2),
id_pedido_fk INT NULL,
FOREIGN KEY (id_pedido_fk) REFERENCES pedido(id_pedido),
FOREIGN KEY (id_usuario_fk) REFERENCES usuario(id_usuario)
);

CREATE TABLE detalle_venta (
id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
id_venta_fk INT,
id_producto_fk INT,
cantidad INT,
precio_unitario DECIMAL(10,2) NOT NULL DEFAULT 0,
FOREIGN KEY (id_venta_fk) REFERENCES venta(id_venta),
FOREIGN KEY (id_producto_fk) REFERENCES producto(id_producto)
);

CREATE TABLE factura (
id_factura INT AUTO_INCREMENT PRIMARY KEY,
id_venta_fk INT,
total_factura DECIMAL(10,2),
fecha_factura DATETIME,
FOREIGN KEY (id_venta_fk) REFERENCES venta(id_venta)

);

CREATE TABLE devolucion (
id_devolucion INT AUTO_INCREMENT PRIMARY KEY,
id_detalle_venta_fk INT,
cantidad INT,
motivo VARCHAR(200),
fecha_devolucion DATE,
estado VARCHAR(50) DEFAULT 'Pendiente',
FOREIGN KEY (id_detalle_venta_fk) REFERENCES detalle_venta(id_detalle_venta)
);

INSERT INTO rol (nombre_rol, descripcion_rol) 

VALUES  

('Administrador','Control total del sistema'), 

('Cliente','Usuario que compra productos'), 

('Vendedor','Gestiona ventas'), 

('Bodeguero','Administra inventario'); 