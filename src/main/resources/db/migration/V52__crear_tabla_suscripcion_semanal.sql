-- ── Suscripciones semanales de comidas ──────────────────────────────────────
CREATE TABLE suscripcion_semanal (
    id_suscripcion_semanal  INT           NOT NULL AUTO_INCREMENT,
    identificador_cliente   VARCHAR(150)  NOT NULL
        COMMENT 'Nombre o teléfono legible para el operador',
    uuid_cliente            VARCHAR(45)   NULL
        COMMENT 'FK lógica a registro_cliente; NULL hasta que el cliente activa con código',
    semana_inicio           DATE          NOT NULL
        COMMENT 'Lunes de la semana activa',
    comidas_por_dia         INT           NOT NULL DEFAULT 1
        COMMENT 'Número de comidas gratis contratadas por día',
    precio_suscripcion      DECIMAL(7,2)  NOT NULL
        COMMENT 'Precio cobrado al cliente por la semana',
    metodo_pago             VARCHAR(20)   NOT NULL
        COMMENT 'TARJETA | EFECTIVO | TRANSFERENCIA',
    codigo_recuperacion     VARCHAR(30)   NOT NULL
        COMMENT 'Código alfanumérico único para activar o recuperar la suscripción',
    activo                  TINYINT(1)    NOT NULL DEFAULT 1,
    origen                  VARCHAR(10)   NOT NULL
        COMMENT 'COCINA = creada por operador | WEB = comprada por el cliente',
    PRIMARY KEY (id_suscripcion_semanal),
    UNIQUE KEY uk_suscripcion_codigo_recuperacion (codigo_recuperacion),
    INDEX idx_suscripcion_semana_inicio (semana_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
