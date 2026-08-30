-- Agrega identificador público UUID a la tabla ruta.
-- Se genera con UUID() de MySQL para backfill de filas existentes;
-- las filas nuevas recibirán su UUID desde la capa de servicio.

ALTER TABLE ruta ADD COLUMN uuid_ruta VARCHAR(45) NULL;

UPDATE ruta SET uuid_ruta = UUID();

ALTER TABLE ruta MODIFY COLUMN uuid_ruta VARCHAR(45) NOT NULL;

ALTER TABLE ruta ADD UNIQUE INDEX uq_ruta_uuid (uuid_ruta);