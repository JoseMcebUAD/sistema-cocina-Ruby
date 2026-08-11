-- V24: Reemplaza el enum tipo_producto en producto_cocina por FK a categoria
--      y añade tabla puente producto_cocina_subcategoria.
--
-- Fase 2 del sprint de categorías. El backfill mapea el nombre del enum
-- viejo (SNACK, CHAROLA, BEBIDA, POSTRE) al nombre de la categoría creada
-- en V23. Si algún producto_cocina.tipo_producto no tiene contraparte en la
-- tabla categoria, el paso MODIFY ... NOT NULL abortará la migración.


-- 1) Agregar la FK como nullable + índice
ALTER TABLE producto_cocina
    ADD COLUMN id_categoria INT NULL AFTER destacado,
    ADD CONSTRAINT fk_producto_cocina_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX idx_producto_cocina_id_categoria ON producto_cocina (id_categoria);

-- 2) Backfill desde el enum viejo al nuevo id_categoria
UPDATE producto_cocina pc
JOIN categoria c ON UPPER(pc.tipo_producto) = UPPER(c.nombre)
SET pc.id_categoria = c.id_categoria;

-- 3) Endurecer NOT NULL y descartar el enum
ALTER TABLE producto_cocina
    MODIFY COLUMN id_categoria INT NOT NULL,
    DROP COLUMN tipo_producto;

-- 4) Tabla puente producto_cocina ↔ subcategoria (0..N por producto)
CREATE TABLE IF NOT EXISTS producto_cocina_subcategoria (
    id_producto_cocina INT NOT NULL,
    id_subcategoria    INT NOT NULL,
    PRIMARY KEY (id_producto_cocina, id_subcategoria),
    CONSTRAINT fk_pcs_producto_cocina
        FOREIGN KEY (id_producto_cocina) REFERENCES producto_cocina (id_producto_cocina)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_pcs_subcategoria
        FOREIGN KEY (id_subcategoria) REFERENCES subcategoria (id_subcategoria)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pcs_subcategoria ON producto_cocina_subcategoria (id_subcategoria);
