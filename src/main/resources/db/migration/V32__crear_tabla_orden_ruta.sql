CREATE TABLE orden_ruta (
    id_orden_ruta INT NOT NULL AUTO_INCREMENT,
    tiempo_estimado_min INT NULL,
    PRIMARY KEY (id_orden_ruta)
);

INSERT INTO orden_ruta (tiempo_estimado_min) VALUES (NULL),(NULL),(NULL),(NULL),(NULL),(NULL);

ALTER TABLE ruta ADD COLUMN id_orden_ruta INT NULL;
ALTER TABLE ruta ADD CONSTRAINT fk_ruta_orden_ruta
    FOREIGN KEY (id_orden_ruta) REFERENCES orden_ruta(id_orden_ruta);

ALTER TABLE ruta DROP COLUMN tiempo_estimado_min;
ALTER TABLE ruta DROP COLUMN orden;
