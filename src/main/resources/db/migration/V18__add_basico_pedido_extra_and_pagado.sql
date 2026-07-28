CREATE TABLE basico_pedido_extra (
    id_basico_pedido_extra INT AUTO_INCREMENT PRIMARY KEY,
    id_basico_pedido       INT          NOT NULL,
    id_complemento         INT          NOT NULL,
    cantidad               TINYINT      NOT NULL,
    precio                 DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_bpe_basico_pedido FOREIGN KEY (id_basico_pedido)
        REFERENCES basico_pedido(id_basico_pedido),
    CONSTRAINT fk_bpe_complemento FOREIGN KEY (id_complemento)
        REFERENCES complemento(id_complemento)
);

ALTER TABLE pedido
    ADD COLUMN pagado TINYINT(1) NOT NULL DEFAULT 0;
