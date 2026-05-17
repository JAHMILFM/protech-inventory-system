-- ═══════════════════════════════════════════════════════════════════════════
-- FERRETERÍA PRO-TECH :: Script de Base de Datos
-- MySQL 8.0+
-- ═══════════════════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS ferreteria_protech
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE ferreteria_protech;

-- ═══════════════════════════════════════════════════════════════════════════
-- TABLA: roles
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══════════════════════════════════════════════════════════════════════════
-- TABLA: usuarios
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150),
    email VARCHAR(100) UNIQUE,
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_usuario_username (username),
    INDEX idx_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══════════════════════════════════════════════════════════════════════════
-- TABLA: usuario_roles (muchos a muchos)
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, role_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══════════════════════════════════════════════════════════════════════════
-- TABLA: categorias (jerárquica: Departamento > Categoría > Subcategoría)
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    icono VARCHAR(50),
    nivel INT NOT NULL DEFAULT 1 COMMENT '1=Departamento, 2=Categoría, 3=Subcategoría',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    padre_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_categoria_nombre (nombre),
    INDEX idx_categoria_padre (padre_id),
    INDEX idx_categoria_nivel (nivel),
    FOREIGN KEY (padre_id) REFERENCES categorias(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══════════════════════════════════════════════════════════════════════════
-- TABLA: productos
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(20) NOT NULL UNIQUE,
    ean13 VARCHAR(13),
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio_costo DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    precio_oferta DECIMAL(10,2),
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    stock_reserva INT DEFAULT 0,
    largo_cm DECIMAL(8,2),
    ancho_cm DECIMAL(8,2),
    alto_cm DECIMAL(8,2),
    peso_kg DECIMAL(8,3),
    ubicacion_pasillo VARCHAR(10),
    ubicacion_lado VARCHAR(5),
    ubicacion_nivel VARCHAR(5),
    marca VARCHAR(100),
    modelo VARCHAR(100),
    unidad_medida VARCHAR(20) DEFAULT 'UND',
    imagen_url VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_producto_sku (sku),
    INDEX idx_producto_ean (ean13),
    INDEX idx_producto_nombre (nombre),
    INDEX idx_producto_categoria (categoria_id),
    INDEX idx_producto_activo (activo),
    INDEX idx_producto_marca (marca),
    INDEX idx_producto_stock (stock_actual, stock_minimo),
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══════════════════════════════════════════════════════════════════════════
-- TABLA: kardex (auditoría de inventario)
-- ═══════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS kardex (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    tipo_movimiento ENUM('ENTRADA','SALIDA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','DEVOLUCION') NOT NULL,
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_nuevo INT NOT NULL,
    precio_unitario DECIMAL(10,2),
    costo_total DECIMAL(12,2),
    documento_referencia VARCHAR(50),
    motivo VARCHAR(500),
    proveedor VARCHAR(150),
    usuario_id BIGINT,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_kardex_producto (producto_id),
    INDEX idx_kardex_tipo (tipo_movimiento),
    INDEX idx_kardex_fecha (fecha_movimiento),
    INDEX idx_kardex_proveedor (proveedor),
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══════════════════════════════════════════════════════════════════════════
-- DATOS INICIALES
-- ═══════════════════════════════════════════════════════════════════════════

-- Roles del sistema
INSERT IGNORE INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Administrador del sistema'),
('OPERARIO', 'Operario de almacén'),
('PROVEEDOR', 'Proveedor de mercancía'),
('CLIENTE', 'Cliente de la ferretería');

-- Nota: El usuario admin y los datos de ejemplo se crean automáticamente
-- al iniciar la aplicación Spring Boot (ver DataInitializer.java)
-- Credenciales por defecto: admin / admin123
