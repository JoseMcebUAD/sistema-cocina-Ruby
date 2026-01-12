-- Script para crear la base de datos de pruebas
-- Esta base de datos es completamente separada de la BD de producción
-- Ejecutar con: mysql -u root -p < create-test-database.sql

-- Crear la base de datos de prueba si no existe
CREATE DATABASE IF NOT EXISTS `sistema-cocina-ruby-test`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

-- Mensaje de confirmación
SELECT 'Base de datos de prueba creada exitosamente' as mensaje;

-- Mostrar las bases de datos disponibles
SHOW DATABASES LIKE '%cocina-ruby%';
