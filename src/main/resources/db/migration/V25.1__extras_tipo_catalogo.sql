-- V25.1: Añade EXTRAS como nuevo tipo estático de módulo de archivos.
--
-- Extiende el enum tipo_catalogo_producto / entity_type con el valor EXTRAS
-- (módulo de propósito general para archivos que no pertenecen a un catálogo
-- específico, p. ej. banners, promociones, imágenes misceláneas) y siembra
-- la fila de archivo_modulo correspondiente.

-- 1) Extender el enum en archivo_modulo y archivo
ALTER TABLE archivo_modulo
    MODIFY COLUMN tipo_catalogo_producto ENUM('BASICO', 'COMIDA', 'DESAYUNO') NULL;

ALTER TABLE archivo
    MODIFY COLUMN entity_type ENUM('BASICO', 'COMIDA', 'DESAYUNO') NULL;

-- 2) Sembrar el módulo EXTRAS
INSERT INTO archivo_modulo (nombre_modulo, tipo_catalogo_producto, id_categoria, ruta, archivos_aceptados)
VALUES ('EXTRAS', NULL, 5,
        'cocina_rubi/extras',
        JSON_ARRAY('image/jpeg', 'image/png', 'image/webp'));
