-- =============================================================================
-- V14 — Cloudinary file management
--   1) Normaliza el ENUM `archivo.entity_type` (V1 tenía coma sobrante y valores
--      en minúsculas que no casan con TipoCatalogoProducto del backend).
--   2) Añade COMPLEMENTO al ENUM `archivo_modulo.tipo_catalogo_producto`.
--   3) Inserta los 6 módulos del catálogo (COMIDA, COMPLEMENTO, DESAYUNO,
--      SNACK, CHAROLA, BEBIDA) con su ruta destino en Cloudinary y los MIME
--      aceptados como JSON.
-- =============================================================================

ALTER TABLE archivo
    MODIFY COLUMN entity_type
        ENUM('BASICO','COMIDA','COMPLEMENTO','DESAYUNO','SNACK','CHAROLA','BEBIDA', 'POSTRE') NOT NULL;

ALTER TABLE archivo_modulo
    MODIFY COLUMN tipo_catalogo_producto
        ENUM('BASICO','COMIDA','COMPLEMENTO','DESAYUNO','SNACK','CHAROLA','BEBIDA','POSTRE') NULL;

INSERT INTO archivo_modulo (nombre_modulo, tipo_catalogo_producto, ruta, archivos_aceptados) VALUES
    ('COMIDA',      'COMIDA',      'cocina_rubi/comida',      JSON_ARRAY('image/jpeg','image/png','image/webp')),
    ('COMPLEMENTO', 'COMPLEMENTO', 'cocina_rubi/complemento', JSON_ARRAY('image/jpeg','image/png','image/webp')),
    ('DESAYUNO',    'DESAYUNO',    'cocina_rubi/desayuno',    JSON_ARRAY('image/jpeg','image/png','image/webp')),
    ('SNACK',       'SNACK',       'cocina_rubi/snack',       JSON_ARRAY('image/jpeg','image/png','image/webp')),
    ('CHAROLA',     'CHAROLA',     'cocina_rubi/charola',     JSON_ARRAY('image/jpeg','image/png','image/webp')),
    ('BEBIDA',      'BEBIDA',      'cocina_rubi/bebida',      JSON_ARRAY('image/jpeg','image/png','image/webp')),
    ('POSTRE',      'POSTRE',       'cocina_rubi/postre',      JSON_ARRAY('image/jpeg','image/png','image/webp'));
