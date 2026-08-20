SET NAMES utf8mb4;

-- =============================================================================
-- V28: Cambio de ON DELETE RESTRICT a SET NULL / CASCADE en tablas hijas de
--      las entidades catálogo (Comida, Desayuno, Complemento, ProductoCocina,
--      Basico, Paquete).
--
-- Lógica:
--   · Tablas de pedido (histórico) → SET NULL: el precio ya está registrado en
--     la línea, el registro del pedido se conserva intacto.
--   · Tablas estructurales/operativas → CASCADE: una fila puente o de inventario
--     sin su entidad padre carece de significado.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- 1. comida_pedido.id_comida  →  SET NULL
-- -----------------------------------------------------------------------------
ALTER TABLE comida_pedido DROP FOREIGN KEY fk_comida_pedido_comida;
ALTER TABLE comida_pedido MODIFY COLUMN id_comida INT NULL;
ALTER TABLE comida_pedido ADD CONSTRAINT fk_comida_pedido_comida
    FOREIGN KEY (id_comida) REFERENCES comida (id_comida)
    ON UPDATE CASCADE ON DELETE SET NULL;

-- -----------------------------------------------------------------------------
-- 2. complemento_comida_pedido.id_complemento  →  SET NULL
-- -----------------------------------------------------------------------------
ALTER TABLE complemento_comida_pedido DROP FOREIGN KEY fk_comp_com_ped_complemento;
ALTER TABLE complemento_comida_pedido MODIFY COLUMN id_complemento INT NULL;
ALTER TABLE complemento_comida_pedido ADD CONSTRAINT fk_comp_com_ped_complemento
    FOREIGN KEY (id_complemento) REFERENCES complemento (id_complemento)
    ON UPDATE CASCADE ON DELETE SET NULL;

-- -----------------------------------------------------------------------------
-- 3. desayuno_pedido.id_desayuno  →  SET NULL
-- -----------------------------------------------------------------------------
ALTER TABLE desayuno_pedido DROP FOREIGN KEY fk_desayuno_pedido_desayuno;
ALTER TABLE desayuno_pedido MODIFY COLUMN id_desayuno INT NULL;
ALTER TABLE desayuno_pedido ADD CONSTRAINT fk_desayuno_pedido_desayuno
    FOREIGN KEY (id_desayuno) REFERENCES desayuno (id_desayuno)
    ON UPDATE CASCADE ON DELETE SET NULL;

-- -----------------------------------------------------------------------------
-- 4. producto_cocina_pedido.id_producto_cocina  →  SET NULL
-- -----------------------------------------------------------------------------
ALTER TABLE producto_cocina_pedido DROP FOREIGN KEY fk_prod_coc_ped_producto;
ALTER TABLE producto_cocina_pedido MODIFY COLUMN id_producto_cocina INT NULL;
ALTER TABLE producto_cocina_pedido ADD CONSTRAINT fk_prod_coc_ped_producto
    FOREIGN KEY (id_producto_cocina) REFERENCES producto_cocina (id_producto_cocina)
    ON UPDATE CASCADE ON DELETE SET NULL;

-- -----------------------------------------------------------------------------
-- 5. basico_pedido.id_basico  →  SET NULL
-- -----------------------------------------------------------------------------
ALTER TABLE basico_pedido DROP FOREIGN KEY fk_basico_pedido_basico;
ALTER TABLE basico_pedido MODIFY COLUMN id_basico INT NULL;
ALTER TABLE basico_pedido ADD CONSTRAINT fk_basico_pedido_basico
    FOREIGN KEY (id_basico) REFERENCES basico (id_basico)
    ON UPDATE CASCADE ON DELETE SET NULL;

-- -----------------------------------------------------------------------------
-- 6. basico_pedido_extra.id_complemento  →  SET NULL
--    (la migración V18 no especificó ON DELETE, quedó RESTRICT por defecto)
-- -----------------------------------------------------------------------------
ALTER TABLE basico_pedido_extra DROP FOREIGN KEY fk_bpe_complemento;
ALTER TABLE basico_pedido_extra MODIFY COLUMN id_complemento INT NULL;
ALTER TABLE basico_pedido_extra ADD CONSTRAINT fk_bpe_complemento
    FOREIGN KEY (id_complemento) REFERENCES complemento (id_complemento)
    ON UPDATE CASCADE ON DELETE SET NULL;

-- -----------------------------------------------------------------------------
-- 7. paquete_pedido.id_paquete  →  SET NULL
-- -----------------------------------------------------------------------------
ALTER TABLE paquete_pedido DROP FOREIGN KEY fk_pp_paquete_ref;
ALTER TABLE paquete_pedido MODIFY COLUMN id_paquete INT NULL;
ALTER TABLE paquete_pedido ADD CONSTRAINT fk_pp_paquete_ref
    FOREIGN KEY (id_paquete) REFERENCES paquete (id_paquete)
    ON UPDATE CASCADE ON DELETE SET NULL;

-- -----------------------------------------------------------------------------
-- 8. basico.id_comida  →  CASCADE
--    Al eliminar una Comida, los Básicos que la referencian se eliminan también.
-- -----------------------------------------------------------------------------
ALTER TABLE basico DROP FOREIGN KEY fk_basico_comida;
ALTER TABLE basico ADD CONSTRAINT fk_basico_comida
    FOREIGN KEY (id_comida) REFERENCES comida (id_comida)
    ON UPDATE CASCADE ON DELETE CASCADE;

-- -----------------------------------------------------------------------------
-- 9. basico_complemento.id_complemento  →  CASCADE
--    La fila puente no tiene sentido sin su complemento.
-- -----------------------------------------------------------------------------
ALTER TABLE basico_complemento DROP FOREIGN KEY fk_basico_comp_complemento;
ALTER TABLE basico_complemento ADD CONSTRAINT fk_basico_comp_complemento
    FOREIGN KEY (id_complemento) REFERENCES complemento (id_complemento)
    ON UPDATE CASCADE ON DELETE CASCADE;

-- -----------------------------------------------------------------------------
-- 10. inventario_comida.id_comida  →  CASCADE
--     El registro de inventario no tiene sentido sin la comida asociada.
-- -----------------------------------------------------------------------------
ALTER TABLE inventario_comida DROP FOREIGN KEY fk_inventario_comida;
ALTER TABLE inventario_comida ADD CONSTRAINT fk_inventario_comida
    FOREIGN KEY (id_comida) REFERENCES comida (id_comida)
    ON UPDATE CASCADE ON DELETE CASCADE;
