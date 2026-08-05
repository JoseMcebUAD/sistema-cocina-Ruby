-- V23: Catálogo de categorías y subcategorías para clasificar los ProductoCocina.
--
-- categoria    : Reemplaza al enum DBConstants.TipoProducto en la Fase 2. Nombre único.
-- subcategoria : Desglose fino asociado a una categoría. Unicidad de nombre por categoría.
--
-- ON DELETE RESTRICT en subcategoria: no se puede borrar una categoría con subcategorías;
-- CategoriaService bloquea con 409 antes de que la BD lance el error.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INT         NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(60) NOT NULL,
    PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subcategoria (
    id_subcategoria INT         NOT NULL AUTO_INCREMENT,
    id_categoria    INT         NOT NULL,
    nombre          VARCHAR(60) NOT NULL,
    PRIMARY KEY (id_subcategoria),
    CONSTRAINT fk_subcategoria_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_subcategoria_nombre_por_categoria UNIQUE (id_categoria, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_subcategoria_id_categoria ON subcategoria (id_categoria);

-- Seeder: categorías iniciales que corresponden a los valores actuales de
-- DBConstants.TipoProducto (BEBIDA, CHAROLA, SNACK) + POSTRE que se incorpora
-- en este sprint para soportar la nueva categoría de productos de cocina.
INSERT INTO categoria (nombre) VALUES
    ('BEBIDA'),
    ('CHAROLA'),
    ('SNACK'),
    ('POSTRE'),
    ('EXTRAS');
