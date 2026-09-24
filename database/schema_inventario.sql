-- =============================================================================
-- SISTEMA INTEGRAL DE GESTIÓN DE INVENTARIO, VENTAS Y COMPRAS (MySQL 8.0+)
-- Arquitectura Relacional Empresarial 3NF (18 Tablas Normalizadas)
-- Seguridad RBAC Granular con Roles y Permisos Muchos a Muchos
-- =============================================================================

DROP DATABASE IF EXISTS db_tienda_inventario;
CREATE DATABASE db_tienda_inventario 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE db_tienda_inventario;

-- =============================================================================
-- 1. MÓDULO DE SEGURIDAD Y CONTROL DE ACCESO (RBAC GRANULAR)
-- =============================================================================

-- 1.1 Catálogo de Permisos / Privilegios Granulares
CREATE TABLE permisos (
    id_permiso INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE COMMENT 'Ej: PRODUCTO_CREAR, INVENTARIO_AJUSTAR',
    descripcion VARCHAR(150) NOT NULL,
    modulo VARCHAR(50) NOT NULL COMMENT 'SEGURIDAD, CATALOGO, INVENTARIO, VENTAS, COMPRAS, REPORTES'
) ENGINE=InnoDB COMMENT='Catálogo de privilegios atómicos del sistema';

-- 1.2 Catálogo de Roles
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE COMMENT 'Ej: ROLE_ADMINISTRADOR, ROLE_SUPERVISOR_ALMACEN',
    descripcion VARCHAR(150) NULL
) ENGINE=InnoDB COMMENT='Roles de usuarios del sistema';

-- 1.3 Asignación de Permisos a Roles (Muchos a Muchos)
CREATE TABLE roles_permisos (
    id_rol INT NOT NULL,
    id_permiso INT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT fk_rp_roles FOREIGN KEY (id_rol) REFERENCES roles(id_rol) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rp_permisos FOREIGN KEY (id_permiso) REFERENCES permisos(id_permiso) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Matriz de autorización rol-permiso';

-- 1.4 Usuarios del Sistema
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'Hash BCrypt',
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20) NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1: Activo, 0: Inactivo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP NULL,
    INDEX idx_usuario_username (username),
    INDEX idx_usuario_email (email)
) ENGINE=InnoDB COMMENT='Cuentas de usuario empresariales';

-- 1.5 Asignación de Roles a Usuarios (Muchos a Muchos)
CREATE TABLE usuarios_roles (
    id_usuario INT NOT NULL,
    id_rol INT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_ur_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ur_roles FOREIGN KEY (id_rol) REFERENCES roles(id_rol) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Asignación de roles por usuario';

-- =============================================================================
-- 2. MÓDULO DE CATÁLOGO Y CLASIFICACIÓN
-- =============================================================================

-- 2.1 Categorías
CREATE TABLE categorias (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB COMMENT='Categorías de productos';

-- 2.2 Marcas
CREATE TABLE marcas (
    id_marca INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    pais_origen VARCHAR(80) NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB COMMENT='Marcas comerciales';

-- 2.3 Unidades de Medida
CREATE TABLE unidades_medida (
    id_unidad INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE COMMENT 'NIU, KGM, BX, LTR',
    nombre VARCHAR(50) NOT NULL,
    abreviatura VARCHAR(10) NOT NULL
) ENGINE=InnoDB COMMENT='Unidades de medida normalizadas (SUNAT)';

-- 2.4 Productos
CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    codigo_sku VARCHAR(50) NOT NULL UNIQUE COMMENT 'Código interno SKU',
    codigo_barras VARCHAR(50) NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT NULL,
    id_categoria INT NOT NULL,
    id_marca INT NOT NULL,
    id_unidad INT NOT NULL,
    precio_costo DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    precio_venta DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_minimo INT NOT NULL DEFAULT 5,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prod_categorias FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prod_marcas FOREIGN KEY (id_marca) REFERENCES marcas(id_marca) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prod_unidades FOREIGN KEY (id_unidad) REFERENCES unidades_medida(id_unidad) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_productos_sku (codigo_sku),
    INDEX idx_productos_nombre (nombre)
) ENGINE=InnoDB COMMENT='Maestro unificado de artículos comerciales';

-- =============================================================================
-- 3. MÓDULO MULTIALMACÉN Y CONTROL DE STOCK
-- =============================================================================

-- 3.1 Almacenes / Sedes
CREATE TABLE almacenes (
    id_almacen INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    ciudad VARCHAR(80) NOT NULL DEFAULT 'Lima',
    estado TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB COMMENT='Almacenes físicos y sucursales';

-- 3.2 Saldo de Stock por Almacén
CREATE TABLE stock_almacen (
    id_stock INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    id_almacen INT NOT NULL,
    stock_actual INT NOT NULL DEFAULT 0,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_stock_almacen FOREIGN KEY (id_almacen) REFERENCES almacenes(id_almacen) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_producto_almacen UNIQUE (id_producto, id_almacen),
    CONSTRAINT chk_stock_positivo CHECK (stock_actual >= 0)
) ENGINE=InnoDB COMMENT='Existencias físicas en tiempo real por almacén';

-- 3.3 Tipos de Movimiento
CREATE TABLE tipos_movimiento (
    id_tipo INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE COMMENT 'ENTRADA_COMPRA, SALIDA_VENTA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, TRASLADO_ORIGEN, TRASLADO_DESTINO',
    descripcion VARCHAR(120) NOT NULL,
    naturaleza VARCHAR(20) NOT NULL COMMENT 'INGRESO, EGRESO, NEUTRO'
) ENGINE=InnoDB COMMENT='Catálogo de operaciones de inventario';

-- 3.4 Movimientos de Inventario (Cabecera)
CREATE TABLE movimientos_inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    numero_movimiento VARCHAR(30) NOT NULL UNIQUE,
    id_tipo INT NOT NULL,
    id_almacen_origen INT NOT NULL,
    id_almacen_destino INT NULL,
    id_usuario INT NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_tipos FOREIGN KEY (id_tipo) REFERENCES tipos_movimiento(id_tipo) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mov_almacen_origen FOREIGN KEY (id_almacen_origen) REFERENCES almacenes(id_almacen) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mov_almacen_destino FOREIGN KEY (id_almacen_destino) REFERENCES almacenes(id_almacen) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mov_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Kardex / Cabecera de operaciones de almacén';

-- 3.5 Detalle de Movimientos
CREATE TABLE detalle_movimientos (
    id_detalle_mov INT AUTO_INCREMENT PRIMARY KEY,
    id_movimiento INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    costo_unitario DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_dm_movimiento FOREIGN KEY (id_movimiento) REFERENCES movimientos_inventario(id_movimiento) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_dm_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_dm_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB COMMENT='Partidas de productos de cada movimiento';

-- =============================================================================
-- 4. MÓDULO DE COMPRAS Y PROVEEDORES
-- =============================================================================

-- 4.1 Proveedores
CREATE TABLE proveedores (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    contacto VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    direccion VARCHAR(255) NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB COMMENT='Proveedores comerciales';

-- 4.2 Órdenes de Compra
CREATE TABLE ordenes_compra (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    numero_orden VARCHAR(30) NOT NULL UNIQUE,
    id_proveedor INT NOT NULL,
    id_almacen_destino INT NOT NULL,
    id_usuario INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE, RECIBIDA, ANULADA',
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    impuesto DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_recepcion TIMESTAMP NULL,
    CONSTRAINT fk_oc_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_oc_almacen FOREIGN KEY (id_almacen_destino) REFERENCES almacenes(id_almacen) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_oc_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Cabecera de compras y abastecimiento';

-- 4.3 Detalle de Órdenes de Compra
CREATE TABLE detalle_ordenes_compra (
    id_detalle_orden INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_compra DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_doc_orden FOREIGN KEY (id_orden) REFERENCES ordenes_compra(id_orden) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_doc_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_doc_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB COMMENT='Líneas de compra por orden';

-- =============================================================================
-- 5. MÓDULO DE VENTAS Y CLIENTES
-- =============================================================================

-- 5.1 Clientes
CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL DEFAULT 'DNI' COMMENT 'DNI, RUC, CE, PASAPORTE',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    nombres_o_razon_social VARCHAR(150) NOT NULL,
    email VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    direccion VARCHAR(200) NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB COMMENT='Clientes y compradores';

-- 5.2 Ventas (Cabecera)
CREATE TABLE ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    tipo_comprobante VARCHAR(20) NOT NULL COMMENT 'BOLETA, FACTURA, NOTA_VENTA',
    serie VARCHAR(5) NOT NULL,
    numero_correlativo VARCHAR(10) NOT NULL,
    id_cliente INT NOT NULL,
    id_usuario INT NOT NULL COMMENT 'Cajero o vendedor',
    id_almacen INT NOT NULL COMMENT 'Almacén de donde se despacha el producto',
    metodo_pago VARCHAR(30) NOT NULL COMMENT 'EFECTIVO, TARJETA, TRANSFERENCIA, YAPE_PLIN',
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    igv DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) NOT NULL DEFAULT 'EMITIDA' COMMENT 'EMITIDA, ANULADA',
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ventas_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ventas_almacen FOREIGN KEY (id_almacen) REFERENCES almacenes(id_almacen) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_comprobante UNIQUE (serie, numero_correlativo)
) ENGINE=InnoDB COMMENT='Facturación y ventas emitidas';

-- 5.3 Detalle de Ventas
CREATE TABLE detalle_ventas (
    id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_dv_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_dv_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_dv_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB COMMENT='Ítems vendidos por comprobante';

-- =============================================================================
-- 6. MÓDULO DE AUDITORÍA
-- =============================================================================

CREATE TABLE auditoria_sistema (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NULL,
    username VARCHAR(50) NOT NULL,
    accion VARCHAR(80) NOT NULL COMMENT 'CREACION, MODIFICACION, ELIMINACION, LOGIN, AJUSTE_STOCK',
    tabla_afectada VARCHAR(60) NOT NULL,
    id_registro_afectado INT NULL,
    detalle TEXT NULL,
    ip_origen VARCHAR(45) NULL,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_auditoria_usuario (username),
    INDEX idx_auditoria_fecha (fecha_hora)
) ENGINE=InnoDB COMMENT='Log inmutable de auditoría para trazabilidad';

-- =============================================================================
-- INSERTS SEMILLA: SEGURIDAD RBAC, USUARIOS, CATÁLOGO Y STOCK
-- =============================================================================

-- Permisos granulares
INSERT INTO permisos (nombre, descripcion, modulo) VALUES
-- Módulo Seguridad
('USUARIO_ADMIN', 'Gestión integral de usuarios, roles y asignaciones', 'SEGURIDAD'),
-- Módulo Catálogo
('PRODUCTO_VER', 'Consulta de catálogo y precios de venta', 'CATALOGO'),
('PRODUCTO_CREAR', 'Registro de nuevos artículos en el catálogo', 'CATALOGO'),
('PRODUCTO_EDITAR', 'Actualización de especificaciones y precios de venta', 'CATALOGO'),
('PRODUCTO_ELIMINAR', 'Baja lógica de artículos del catálogo', 'CATALOGO'),
('COSTO_VER', 'Visualización de precios de costo de compra', 'CATALOGO'),
-- Módulo Inventario
('STOCK_VER', 'Consulta de existencias físicas en almacenes', 'INVENTARIO'),
('INVENTARIO_ENTRADA', 'Registro de ingresos físicos de mercancía', 'INVENTARIO'),
('INVENTARIO_SALIDA', 'Registro de despachos físicos de mercancía', 'INVENTARIO'),
('INVENTARIO_AJUSTAR', 'Ajuste extraordinario por conteo y auditoría física', 'INVENTARIO'),
('INVENTARIO_TRASLADAR', 'Transferencia entre diferentes almacenes', 'INVENTARIO'),
-- Módulo Compras
('COMPRA_VER', 'Consulta de órdenes de compra a proveedores', 'COMPRAS'),
('COMPRA_CREAR', 'Emisión de requerimientos de compra', 'COMPRAS'),
('COMPRA_RECEPCIONAR', 'Recepción y liquidación de órdenes de compra', 'COMPRAS'),
-- Módulo Ventas
('VENTA_VER', 'Consulta del historial de ventas y comprobantes', 'VENTAS'),
('VENTA_REGISTRAR', 'Emisión y cobro de boletas, facturas y notas de venta', 'VENTAS'),
('VENTA_ANULAR', 'Anulación de comprobantes de pago emitidos', 'VENTAS'),
-- Módulo Reportes
('REPORTE_DESCARGAR', 'Generación y exportación de reportes PDF oficiales', 'REPORTES');

-- Roles
INSERT INTO roles (nombre, descripcion) VALUES
('ROLE_ADMINISTRADOR', 'Acceso irrestricto, configuración y administración general'),
('ROLE_SUPERVISOR_ALMACEN', 'Control de catálogo, abastecimiento, traslados y ajustes'),
('ROLE_OPERADOR_ALMACEN', 'Operación de despacho, recepción y toma física'),
('ROLE_CAJERO_VENDEDOR', 'Atención en punto de venta, facturación y consulta de stock');

-- Matriz Roles - Permisos
-- 1. ADMINISTRADOR: Todos los permisos (1 al 17)
INSERT INTO roles_permisos (id_rol, id_permiso)
SELECT 1, id_permiso FROM permisos;

-- 2. SUPERVISOR_ALMACEN: Gestión de productos, stock, ajustes, compras y reportes
INSERT INTO roles_permisos (id_rol, id_permiso) VALUES
(2, 2), -- PRODUCTO_VER
(2, 3), -- PRODUCTO_CREAR
(2, 4), -- PRODUCTO_EDITAR
(2, 6), -- COSTO_VER
(2, 7), -- STOCK_VER
(2, 8), -- INVENTARIO_ENTRADA
(2, 9), -- INVENTARIO_SALIDA
(2, 10),-- INVENTARIO_AJUSTAR
(2, 11),-- INVENTARIO_TRASLADAR
(2, 12),-- COMPRA_VER
(2, 13),-- COMPRA_CREAR
(2, 14),-- COMPRA_RECEPCIONAR
(2, 18);-- REPORTE_DESCARGAR

-- 3. OPERADOR_ALMACEN: Entradas, salidas y consulta básica de stock (sin ajustar ni ver costos)
INSERT INTO roles_permisos (id_rol, id_permiso) VALUES
(3, 2), -- PRODUCTO_VER
(3, 7), -- STOCK_VER
(3, 8), -- INVENTARIO_ENTRADA
(3, 9); -- INVENTARIO_SALIDA

-- 4. CAJERO_VENDEDOR: Ventas, consulta de stock y emisión de tickets
INSERT INTO roles_permisos (id_rol, id_permiso) VALUES
(4, 2), -- PRODUCTO_VER
(4, 7), -- STOCK_VER
(4, 15),-- VENTA_VER
(4, 16);-- VENTA_REGISTRAR

-- Usuarios iniciales (Contraseña: Admin123* para todos con hash BCrypt verificado)
-- Hash: $2a$10$mkuvcNQyOfCJSLnAqa8FiOmzgZaPk35cvZActeM4SVL/xyv8e5UxC
INSERT INTO usuarios (username, password, nombres, apellidos, email, telefono, estado) VALUES
('admin', '$2a$10$mkuvcNQyOfCJSLnAqa8FiOmzgZaPk35cvZActeM4SVL/xyv8e5UxC', 'Administrador', 'General', 'admin@tienda.com', '999111222', 1),
('supervisor', '$2a$10$mkuvcNQyOfCJSLnAqa8FiOmzgZaPk35cvZActeM4SVL/xyv8e5UxC', 'Ricardo', 'Palma Alarcón', 'supervisor@tienda.com', '988222333', 1),
('almacenero', '$2a$10$mkuvcNQyOfCJSLnAqa8FiOmzgZaPk35cvZActeM4SVL/xyv8e5UxC', 'Juan', 'Pérez Quispe', 'almacenero@tienda.com', '977333444', 1),
('cajero', '$2a$10$mkuvcNQyOfCJSLnAqa8FiOmzgZaPk35cvZActeM4SVL/xyv8e5UxC', 'María', 'López Vega', 'cajero@tienda.com', '966444555', 1);

-- Asignación de Roles
INSERT INTO usuarios_roles (id_usuario, id_rol) VALUES
(1, 1), -- admin -> ADMINISTRADOR
(2, 2), -- supervisor -> SUPERVISOR_ALMACEN
(3, 3), -- almacenero -> OPERADOR_ALMACEN
(4, 4); -- cajero -> CAJERO_VENDEDOR

-- Categorías
INSERT INTO categorias (nombre, descripcion, estado) VALUES
('Laptops y Equipos', 'Computadoras portátiles y estaciones de trabajo', 1),
('Periféricos y Accesorios', 'Teclados, ratones, auriculares y monitores', 1),
('Almacenamiento y Redes', 'Discos duros, memorias SSD y routers', 1);

-- Marcas
INSERT INTO marcas (nombre, pais_origen, estado) VALUES
('Lenovo', 'China', 1),
('Logitech', 'Suiza', 1),
('Kingston', 'Estados Unidos', 1),
('Dell', 'Estados Unidos', 1);

-- Unidades de Medida
INSERT INTO unidades_medida (codigo, nombre, abreviatura) VALUES
('NIU', 'Unidad (Bienes)', 'UND'),
('BX', 'Caja', 'CJ'),
('KGM', 'Kilogramo', 'KG');

-- Almacenes
INSERT INTO almacenes (codigo, nombre, direccion, ciudad, estado) VALUES
('ALM-CENTRAL', 'Almacén Central Principal', 'Av. Argentina 2850, Callao', 'Callao', 1),
('ALM-SUR', 'Sucursal Sur - San Isidro', 'Av. República de Panamá 4500', 'Lima', 1);

-- Proveedores
INSERT INTO proveedores (ruc, razon_social, contacto, telefono, email, direccion, estado) VALUES
('20556789012', 'Tech Import S.A.C.', 'Carlos Ramos', '01-4458923', 'ventas@techimport.pe', 'Av. Central 1234, Lima', 1),
('20601234567', 'Global Electronics Perú S.R.L.', 'Elena Soto', '01-7894561', 'contacto@globalelectronics.pe', 'Calle Los Sauces 456, San Isidro', 1);

-- Clientes
INSERT INTO clientes (tipo_documento, numero_documento, nombres_o_razon_social, email, telefono, direccion, estado) VALUES
('DNI', '45896321', 'Roberto Gómez Bolaños', 'rgomez@cliente.com', '988554433', 'Av. Larco 450, Miraflores', 1),
('RUC', '20456789123', 'Soluciones Digitales E.I.R.L.', 'compras@soldigitales.pe', '01-2244668', 'Calle Las Camelias 200, San Isidro', 1);

-- Tipos de Movimiento
INSERT INTO tipos_movimiento (codigo, descripcion, naturaleza) VALUES
('ENTRADA_COMPRA', 'Ingreso por recepción de orden de compra', 'INGRESO'),
('SALIDA_VENTA', 'Despacho por venta de mostrador', 'EGRESO'),
('AJUSTE_POSITIVO', 'Regularización por sobrante en inventario físico', 'INGRESO'),
('AJUSTE_NEGATIVO', 'Regularización por merma, daño o faltante', 'EGRESO'),
('TRASLADO_ORIGEN', 'Salida de almacén por transferencia entre sedes', 'EGRESO'),
('TRASLADO_DESTINO', 'Entrada a almacén por recepción de transferencia', 'INGRESO');

-- Productos iniciales
INSERT INTO productos (codigo_sku, codigo_barras, nombre, descripcion, id_categoria, id_marca, id_unidad, precio_costo, precio_venta, stock_minimo, estado) VALUES
('SKU-LAP-001', '775123456001', 'Laptop Lenovo ThinkPad T14 Gen 4', 'Intel Core i7, 16GB RAM, 512GB SSD', 1, 1, 1, 3500.00, 4200.00, 5, 1),
('SKU-MOU-002', '775123456002', 'Mouse Inalámbrico Logitech MX Master 3S', 'Sensor Darkfield 8000 DPI, Quiet Clicks', 2, 2, 1, 280.00, 380.00, 10, 1),
('SKU-SSD-003', '775123456003', 'SSD Kingston NV2 1TB M.2 NVMe', 'Lectura hasta 3500 MB/s, Gen 4x4', 3, 3, 1, 190.00, 290.00, 8, 1),
('SKU-MON-004', '775123456004', 'Monitor Dell UltraSharp 27 4K U2723QE', 'IPS Black, USB-C Hub, HDR400', 2, 4, 1, 1850.00, 2450.00, 3, 1);

-- Stock Inicial en Almacén Central (ID: 1)
INSERT INTO stock_almacen (id_producto, id_almacen, stock_actual) VALUES
(1, 1, 15), -- ThinkPad (Óptimo)
(2, 1, 4),  -- Logitech MX Master (Crítico: 4 <= 10)
(3, 1, 2),  -- SSD Kingston (Crítico: 2 <= 8)
(4, 1, 8);  -- Monitor Dell (Óptimo)

-- Stock Inicial en Sucursal Sur (ID: 2)
INSERT INTO stock_almacen (id_producto, id_almacen, stock_actual) VALUES
(1, 2, 5),
(2, 2, 3),
(3, 2, 1),
(4, 2, 2);
