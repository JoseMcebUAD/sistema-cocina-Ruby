CREATE TABLE tarjeta_transferencia (
    id_tarjeta          INT          NOT NULL AUTO_INCREMENT,
    clabe               VARCHAR(18)  NOT NULL COMMENT 'CLABE interbancaria (18 dígitos)',
    numero_tarjeta      VARCHAR(19)  NOT NULL COMMENT 'Número de tarjeta (hasta 16 dígitos + separadores)',
    nombre_destinatario VARCHAR(100) NOT NULL,
    banco               VARCHAR(60)  NOT NULL COMMENT 'Nombre del banco (ej. BBVA, BANAMEX, SANTANDER)',
    estatus             VARCHAR(15)  NOT NULL DEFAULT 'NO_DISPONIBLE'
                            COMMENT 'DISPONIBLE | NO_DISPONIBLE',
    PRIMARY KEY (id_tarjeta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT 'Tarjetas bancarias habilitadas para recibir transferencias del checkout web. Solo una puede tener estatus DISPONIBLE.';
