-- Descuento promocional opcional para cada producto del catálogo.
-- Los 3 campos son idénticos en las 4 tablas: activo (flag), descripción y precio con descuento.

ALTER TABLE comida
    ADD COLUMN descuento_activo      TINYINT(1)     NOT NULL DEFAULT 0,
    ADD COLUMN descripcion_descuento VARCHAR(400)   NULL,
    ADD COLUMN precio_descuento      DECIMAL(10, 2) NULL;

ALTER TABLE basico
    ADD COLUMN descuento_activo      TINYINT(1)     NOT NULL DEFAULT 0,
    ADD COLUMN descripcion_descuento VARCHAR(400)   NULL,
    ADD COLUMN precio_descuento      DECIMAL(10, 2) NULL;

ALTER TABLE combo
    ADD COLUMN descuento_activo      TINYINT(1)     NOT NULL DEFAULT 0,
    ADD COLUMN descripcion_descuento VARCHAR(400)   NULL,
    ADD COLUMN precio_descuento      DECIMAL(10, 2) NULL;

ALTER TABLE producto_cocina
    ADD COLUMN descuento_activo      TINYINT(1)     NOT NULL DEFAULT 0,
    ADD COLUMN descripcion_descuento VARCHAR(400)   NULL,
    ADD COLUMN precio_descuento      DECIMAL(10, 2) NULL;
