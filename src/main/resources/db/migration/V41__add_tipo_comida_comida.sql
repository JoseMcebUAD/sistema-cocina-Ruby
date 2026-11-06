ALTER TABLE comida
    ADD COLUMN tipo_comida ENUM('FIJA', 'ESPECIAL') NOT NULL DEFAULT 'FIJA' AFTER limite_complemento;
