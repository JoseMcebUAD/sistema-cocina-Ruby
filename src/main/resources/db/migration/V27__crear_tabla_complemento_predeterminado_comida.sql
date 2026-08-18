CREATE TABLE IF NOT EXISTS complemento_predeterminado_comida(
    id_complemento_predeterminado_comida INT NOT NULL AUTO_INCREMENT,
    id_complemento INT NOT NULL,
    id_comida INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 0,
    PRIMARY KEY(id_complemento_predeterminado_comida),
    CONSTRAINT uq_complemento_predeterminado_comida UNIQUE(id_complemento, id_comida),
    CONSTRAINT fk_complemento_predeterminado_comida_complemento FOREIGN KEY(id_complemento)
            REFERENCES complemento(id_complemento) ON DELETE CASCADE,
    CONSTRAINT fk_complemento_predeterminado_comida_comida FOREIGN KEY(id_comida)
            REFERENCES comida(id_comida) ON DELETE CASCADE
);