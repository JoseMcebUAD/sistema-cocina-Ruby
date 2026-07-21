-- Agrega el nombre del repartidor con valor por defecto 'Don cesar'.
ALTER TABLE pago_repartidor
    ADD COLUMN nombre_repartidor VARCHAR(100) NOT NULL DEFAULT 'Don cesar';

-- Amplía el rango del monto de pago de DECIMAL(5,2) a DECIMAL(8,2).
ALTER TABLE pago_repartidor
    MODIFY COLUMN pago DECIMAL(8,2) NOT NULL;
