-- Unifica el modelo de descuento en todas las tablas de líneas de pedido.
-- Elimina los campos de semántica inconsistente (descuento_aplicado / precio_especial)
-- y agrega el modelo uniforme: descuento (boolean) + precio_descuento (precio efectivo).
--
-- precio_unitario  = precio original del catálogo (nunca cambia)
-- descuento        = si se aplicó algún descuento (catálogo WEB, volumen o manual COCINA)
-- precio_descuento = precio efectivo que paga el cliente (solo cuando descuento = 1)

ALTER TABLE comida_pedido
    DROP COLUMN descuento_aplicado,
    DROP COLUMN precio_especial,
    ADD COLUMN descuento      TINYINT(1)    NOT NULL DEFAULT 0,
    ADD COLUMN precio_descuento DECIMAL(7,2) NULL;

ALTER TABLE desayuno_pedido
    ADD COLUMN descuento        TINYINT(1)    NOT NULL DEFAULT 0,
    ADD COLUMN precio_descuento DECIMAL(7,2)  NULL;

ALTER TABLE basico_pedido
    ADD COLUMN descuento        TINYINT(1)    NOT NULL DEFAULT 0,
    ADD COLUMN precio_descuento DECIMAL(7,2)  NULL;

ALTER TABLE producto_cocina_pedido
    ADD COLUMN descuento        TINYINT(1)    NOT NULL DEFAULT 0,
    ADD COLUMN precio_descuento DECIMAL(7,2)  NULL;

ALTER TABLE combo_pedido
    ADD COLUMN descuento        TINYINT(1)    NOT NULL DEFAULT 0,
    ADD COLUMN precio_descuento DECIMAL(7,2)  NULL;
