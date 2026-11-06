-- Nuevas columnas en la tabla anuncio
ALTER TABLE anuncio
    ADD COLUMN prioridad         VARCHAR(10)  NOT NULL DEFAULT 'MEDIA'
        COMMENT 'Prioridad del anuncio: ALTA, MEDIA, BAJA',
    ADD COLUMN activo            TINYINT(1)   NOT NULL DEFAULT 1
        COMMENT '1 = visible para clientes, 0 = oculto',
    ADD COLUMN imagen_url        VARCHAR(500) NULL
        COMMENT 'URL segura de Cloudinary',
    ADD COLUMN imagen_public_id  VARCHAR(255) NULL
        COMMENT 'Public ID de Cloudinary para eliminación';

-- Tabla de asociación polimórfica anuncio ↔ producto del catálogo
CREATE TABLE IF NOT EXISTS anuncio_producto (
    id_anuncio_producto INT         NOT NULL AUTO_INCREMENT,
    id_anuncio          INT         NOT NULL,
    tipo_producto       VARCHAR(30) NOT NULL
        COMMENT 'COMIDA | DESAYUNO | COMPLEMENTO | PRODUCTO_COCINA | BASICO | COMBO',
    id_producto         INT         NOT NULL,
    PRIMARY KEY (id_anuncio_producto),
    CONSTRAINT fk_anuncio_prod_anuncio
        FOREIGN KEY (id_anuncio) REFERENCES anuncio (id_anuncio) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
