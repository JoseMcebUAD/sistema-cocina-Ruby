-- Descuento promocional opcional para desayunos.
-- Alineado con los mismos 3 campos que ya tienen comida, basico, combo y producto_cocina (V45).

ALTER TABLE desayuno
    ADD COLUMN descuento_activo      TINYINT(1)     NOT NULL DEFAULT 0,
    ADD COLUMN descripcion_descuento VARCHAR(400)   NULL,
    ADD COLUMN precio_descuento      DECIMAL(10, 2) NULL;
