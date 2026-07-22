SET NAMES utf8mb4;

-- =============================================================================
-- Agrega campo estatus a la tabla basico
-- =============================================================================

ALTER TABLE basico
    ADD COLUMN estatus ENUM('DISPONIBLE','NO_DISPONIBLE','AGOTADO') NOT NULL DEFAULT 'DISPONIBLE'
        AFTER precio_basico;

-- =============================================================================
-- Nueva línea de pedido: basico_pedido
-- Relaciona un paquete básico con un pedido
-- =============================================================================

CREATE TABLE IF NOT EXISTS basico_pedido (
    id_basico_pedido INT          NOT NULL AUTO_INCREMENT,
    id_basico        INT          NOT NULL,
    id_pedido        INT          NOT NULL,
    precio_unitario  DECIMAL(5,2) NOT NULL COMMENT 'Precio del básico al momento del pedido',
    PRIMARY KEY (id_basico_pedido),
    CONSTRAINT fk_basico_pedido_basico
        FOREIGN KEY (id_basico) REFERENCES basico (id_basico)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_basico_pedido_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------

CREATE INDEX idx_basico_pedido_pedido ON basico_pedido (id_pedido);
