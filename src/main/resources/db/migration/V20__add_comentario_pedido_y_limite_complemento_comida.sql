ALTER TABLE pedido
    ADD COLUMN comentario VARCHAR(100) NULL AFTER pagado;

ALTER TABLE comida
    ADD COLUMN limite_complemento TINYINT UNSIGNED NOT NULL DEFAULT 3 AFTER destacado;
