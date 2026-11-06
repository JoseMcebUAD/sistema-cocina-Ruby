-- ── Catálogo de ofertas ──────────────────────────────────────────────────────
CREATE TABLE oferta (
    id_oferta      INT           NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(150)  NOT NULL,
    descripcion    VARCHAR(400)  NULL,
    precio_oferta  DECIMAL(7,2)  NOT NULL,
    prioridad      INT           NOT NULL DEFAULT 0
        COMMENT 'Mayor valor = se intenta aplicar antes en el algoritmo greedy',
    activo         TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_inicio   DATE          NULL
        COMMENT 'NULL = sin restricción de fecha de inicio',
    fecha_fin      DATE          NULL
        COMMENT 'NULL = sin restricción de fecha de fin',
    PRIMARY KEY (id_oferta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Líneas de regla de cada oferta ──────────────────────────────────────────
CREATE TABLE oferta_linea (
    id_oferta_linea    INT          NOT NULL AUTO_INCREMENT,
    id_oferta          INT          NOT NULL,
    modo_match         VARCHAR(40)  NOT NULL
        COMMENT 'COMIDA_ESPECIFICA|COMIDA_POR_TIPO|PRODUCTO_ESPECIFICO|PRODUCTO_POR_CATEGORIA|PRODUCTO_POR_SUBCATEGORIA|PRODUCTO_GENERAL|BASICO_ESPECIFICO|BASICO_GENERAL|COMBO_ESPECIFICO|COMBO_GENERAL',
    id_referencia      INT          NULL
        COMMENT 'idComida|idProductoCocina|idCategoria|idSubcategoria|idBasico|idCombo; NULL para modos _GENERAL y COMIDA_POR_TIPO',
    tipo_comida_filtro VARCHAR(10)  NULL
        COMMENT 'FIJA|ESPECIAL; solo aplica cuando modo_match = COMIDA_POR_TIPO',
    cantidad           INT          NOT NULL DEFAULT 1,
    PRIMARY KEY (id_oferta_linea),
    CONSTRAINT fk_ol_oferta
        FOREIGN KEY (id_oferta) REFERENCES oferta (id_oferta) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Aplicaciones de oferta dentro de un pedido ──────────────────────────────
CREATE TABLE oferta_pedido (
    id_oferta_pedido  INT          NOT NULL AUTO_INCREMENT,
    id_pedido         INT          NOT NULL,
    id_oferta         INT          NOT NULL,
    precio_aplicado   DECIMAL(7,2) NOT NULL
        COMMENT 'Snapshot de precio_oferta al momento de la orden',
    ahorro            DECIMAL(7,2) NOT NULL
        COMMENT 'Suma de precios normales de líneas consumidas menos precio_aplicado',
    PRIMARY KEY (id_oferta_pedido),
    CONSTRAINT fk_op_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_op_oferta
        FOREIGN KEY (id_oferta) REFERENCES oferta (id_oferta) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Líneas de pedido consumidas por cada aplicación de oferta ───────────────
CREATE TABLE oferta_pedido_linea (
    id_oferta_pedido_linea  INT          NOT NULL AUTO_INCREMENT,
    id_oferta_pedido        INT          NOT NULL,
    tipo_entidad            VARCHAR(30)  NOT NULL
        COMMENT 'COMIDA|PRODUCTO_COCINA|BASICO|COMBO',
    id_linea_pedido         INT          NOT NULL
        COMMENT 'PK polimórfico sin FK física: id de comida_pedido|producto_cocina_pedido|basico_pedido|combo_pedido',
    id_producto_catalogo    INT          NOT NULL
        COMMENT 'Denormalizado: idComida|idProductoCocina|idBasico|idCombo para la respuesta',
    nombre_producto         VARCHAR(150) NOT NULL
        COMMENT 'Snapshot del nombre al momento de la orden',
    cantidad_consumida      INT          NOT NULL DEFAULT 1,
    PRIMARY KEY (id_oferta_pedido_linea),
    CONSTRAINT fk_opl_oferta_pedido
        FOREIGN KEY (id_oferta_pedido)
            REFERENCES oferta_pedido (id_oferta_pedido) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
