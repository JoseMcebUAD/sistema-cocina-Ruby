ALTER TABLE pedido_domicilio
    ADD COLUMN tarifas_especiales DECIMAL(5,2) NULL;

ALTER TABLE pedido_domicilio_cocina
    ADD COLUMN tarifas_especiales DECIMAL(5,2) NULL;
