-- 1. Campos de receta en la tabla comida existente.
ALTER TABLE comida ADD COLUMN descripcion_receta TEXT NULL;
ALTER TABLE comida ADD COLUMN porciones_base    INT  NOT NULL DEFAULT 1;

-- 2. Catalogo de productos / ingredientes.
CREATE TABLE producto (
    id_producto             INT PRIMARY KEY AUTO_INCREMENT,
    nombre                  VARCHAR(150) NOT NULL,
    tipo_medida             ENUM('PESO_EXACTO','PIEZA_VARIABLE') NOT NULL,
    unidad_base             VARCHAR(10)   NOT NULL,
    peso_promedio_pieza     DECIMAL(10,2) NULL,
    contenido_presentacion  DECIMAL(10,2) NOT NULL,
    unidad_presentacion     VARCHAR(10)   NOT NULL,
    costo_presentacion      DECIMAL(10,2) NOT NULL,
    costo_unitario_base     DECIMAL(10,4) NOT NULL,
    CONSTRAINT uq_producto_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_producto_tipo_medida ON producto (tipo_medida);

-- 3. Pivote comida <-> producto (ingredientes de la receta).
CREATE TABLE producto_receta (
    id_producto_receta  INT PRIMARY KEY AUTO_INCREMENT,
    id_comida           INT           NOT NULL,
    id_producto         INT           NOT NULL,
    cantidad_usada      DECIMAL(10,3) NOT NULL,
    unidad_usada        VARCHAR(10)   NOT NULL,
    es_aproximado       BOOLEAN       NOT NULL DEFAULT FALSE,
    costo_unitario      DECIMAL(10,4) NOT NULL,
    costo_total         DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_producto_receta_comida   FOREIGN KEY (id_comida)   REFERENCES comida (id_comida)     ON DELETE CASCADE,
    CONSTRAINT fk_producto_receta_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT uq_producto_receta_comida_producto UNIQUE (id_comida, id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_producto_receta_comida   ON producto_receta (id_comida);
CREATE INDEX idx_producto_receta_producto ON producto_receta (id_producto);
