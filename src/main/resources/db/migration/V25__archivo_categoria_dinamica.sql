-- V25: Migra el subsistema de archivos a módulos dinámicos por categoría.
--
-- - TipoCatalogoProducto se reduce a {BASICO, COMIDA, DESAYUNO}. Los tipos
--   estáticos SNACK, CHAROLA, BEBIDA, POSTRE ahora se identifican vía FK a
--   la tabla categoria.
-- - COMPLEMENTO deja de gestionar imágenes: sus filas se eliminan.
-- - Cada fila de archivo_modulo / archivo usa exactamente uno de los dos
--   discriminadores (tipo_catalogo_producto XOR id_categoria); la validación
--   XOR se aplica a nivel de aplicación en FileServiceImpl.

-- 1) Agregar la columna id_categoria (nullable) a ambas tablas
ALTER TABLE archivo_modulo
    ADD COLUMN id_categoria INT NULL AFTER tipo_catalogo_producto,
    ADD CONSTRAINT fk_archivo_modulo_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE ON DELETE CASCADE;

CREATE INDEX idx_archivo_modulo_id_categoria ON archivo_modulo (id_categoria);

ALTER TABLE archivo
    ADD COLUMN id_categoria INT NULL AFTER entity_type,
    ADD CONSTRAINT fk_archivo_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX idx_archivo_id_categoria ON archivo (id_categoria);

-- 2) Permitir NULL en las columnas discriminador antes del backfill
--    (en el esquema original son NOT NULL; se reducirán al enum de 3 valores en el paso 4)
ALTER TABLE archivo_modulo
    MODIFY COLUMN tipo_catalogo_producto ENUM('BASICO','COMIDA','DESAYUNO','SNACK','CHAROLA','BEBIDA','POSTRE','COMPLEMENTO') NULL;

ALTER TABLE archivo
    MODIFY COLUMN entity_type ENUM('BASICO','COMIDA','DESAYUNO','SNACK','CHAROLA','BEBIDA','POSTRE','COMPLEMENTO') NULL;

-- 3) Backfill: mover las filas de tipos ahora dinámicos a la FK
UPDATE archivo_modulo am
JOIN categoria c ON UPPER(am.tipo_catalogo_producto) = UPPER(c.nombre)
SET am.id_categoria = c.id_categoria,
    am.tipo_catalogo_producto = NULL
WHERE am.tipo_catalogo_producto IN ('SNACK', 'CHAROLA', 'BEBIDA', 'POSTRE');

UPDATE archivo a
JOIN categoria c ON UPPER(a.entity_type) = UPPER(c.nombre)
SET a.id_categoria = c.id_categoria,
    a.entity_type = NULL
WHERE a.entity_type IN ('SNACK', 'CHAROLA', 'BEBIDA', 'POSTRE');

-- 4) Eliminar filas de COMPLEMENTO (ya no gestiona imágenes)
DELETE FROM archivo WHERE entity_type = 'COMPLEMENTO';
DELETE FROM archivo_modulo WHERE tipo_catalogo_producto = 'COMPLEMENTO';

-- 5) Reducir los ENUMs a los tres tipos estáticos restantes
ALTER TABLE archivo_modulo
    MODIFY COLUMN tipo_catalogo_producto ENUM('BASICO', 'COMIDA', 'DESAYUNO') NULL;

ALTER TABLE archivo
    MODIFY COLUMN entity_type ENUM('BASICO', 'COMIDA', 'DESAYUNO') NULL;

-- 6) Sembrar archivo_modulo para las 4 categorías del seeder de V23.
--    NOT EXISTS evita duplicados si el backfill ya generó filas equivalentes.
INSERT INTO archivo_modulo (nombre_modulo, tipo_catalogo_producto, id_categoria, ruta, archivos_aceptados)
SELECT c.nombre,
       NULL,
       c.id_categoria,
       CONCAT('cocina_rubi/', LOWER(c.nombre)),
       JSON_ARRAY('image/jpeg', 'image/png', 'image/webp')
FROM categoria c
WHERE c.nombre IN ('BEBIDA', 'CHAROLA', 'SNACK', 'POSTRE')
  AND NOT EXISTS (
      SELECT 1 FROM archivo_modulo am WHERE am.id_categoria = c.id_categoria
  );
