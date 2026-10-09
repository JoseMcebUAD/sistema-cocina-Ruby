CREATE TABLE pedido_tarifa_especial (
    id            INT           NOT NULL AUTO_INCREMENT,
    id_pedido     INT           NOT NULL,
    id_tarifa     INT           NOT NULL,
    precio_tarifa DECIMAL(5,2)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pte_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_pte_tarifa FOREIGN KEY (id_tarifa)
        REFERENCES tarifa_especial(id_tarifa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE pedido_domicilio        DROP COLUMN tarifas_especiales;
ALTER TABLE pedido_domicilio_cocina DROP COLUMN tarifas_especiales;
