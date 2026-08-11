-- V19: Agrega identificador público UUID a la tabla basico.
-- Basico era la única entidad del catálogo sin UUID público.
-- Se hace backfill con UUID() de MySQL para filas existentes;
-- las nuevas filas recibirán su UUID desde BasicoService.save().

ALTER TABLE basico ADD COLUMN uuid_basico VARCHAR(45) NULL;

UPDATE basico SET uuid_basico = UUID();

ALTER TABLE basico MODIFY COLUMN uuid_basico VARCHAR(45) NOT NULL;

ALTER TABLE basico ADD UNIQUE INDEX uq_basico_uuid (uuid_basico);