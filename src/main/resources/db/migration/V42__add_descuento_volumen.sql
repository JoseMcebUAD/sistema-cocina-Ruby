-- Descuento de volumen: registra el descuento por línea aplicado al crear el pedido
ALTER TABLE comida_pedido
    ADD COLUMN descuento_aplicado DECIMAL(7,2) NOT NULL DEFAULT 0.00
    AFTER precio_unitario;

-- Tipo de descuento aplicado al pedido (NULL = sin descuento)
ALTER TABLE pedido
    ADD COLUMN tipo_descuento ENUM('COMIDAS_DIEZ') NULL DEFAULT NULL
    AFTER precio_final_orden;
