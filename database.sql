-- =====================================================
-- Script de Base de Datos para el proyecto Librería Luz
-- Evidencias SENA: GA7-220501096-AA3-EV01 y EV02
-- Motor compatible: MySQL 8.0+
-- =====================================================

-- 1. Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS libreria
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. Usar la base de datos
USE libreria;

-- 3. Crear tabla libro si no existe
CREATE TABLE IF NOT EXISTS libro (
  id INT NOT NULL AUTO_INCREMENT,
  	itulo VARCHAR(150) NOT NULL,
  isbn VARCHAR(20) NULL DEFAULT NULL,
  precio DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL,
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- 4. Datos iniciales de prueba (opcionales para demostración)
INSERT INTO libro (	itulo, isbn, precio, stock) VALUES
('Cien años de soledad', '978-0307474728', 59000.00, 10),
('El amor en los tiempos del cólera', '978-0307389732', 52000.00, 8),
('La sombra del viento', '978-8408163381', 68000.00, 5);
