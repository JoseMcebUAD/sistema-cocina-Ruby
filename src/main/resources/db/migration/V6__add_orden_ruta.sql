-- Agrega el campo orden a la tabla ruta para controlar el orden de visualización (1,2,3...)
ALTER TABLE ruta ADD COLUMN orden INT;

UPDATE ruta SET orden = 1 WHERE nombre = 'Zona Centro';
UPDATE ruta SET orden = 2 WHERE nombre = 'Zona Norte';
UPDATE ruta SET orden = 3 WHERE nombre = 'Zona Sur';
UPDATE ruta SET orden = 4 WHERE nombre = 'Zona Oriente';
UPDATE ruta SET orden = 5 WHERE nombre = 'Zona Poniente';

ALTER TABLE ruta MODIFY COLUMN orden INT NOT NULL;
ALTER TABLE ruta ADD UNIQUE INDEX idx_ruta_orden (orden);