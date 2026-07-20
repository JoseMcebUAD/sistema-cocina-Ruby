CREATE TABLE auditoria (
    id_auditoria   INT         NOT NULL AUTO_INCREMENT,
    tabla          VARCHAR(60) NOT NULL,
    tipo_operacion ENUM('POST','PUT','DELETE') NOT NULL,
    id_registro    INT         NULL,
    id_usuario     INT         NULL,
    datos_antes    JSON        NULL,
    datos_despues  JSON        NULL,
    creado_en      DATETIME    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_auditoria),
    INDEX idx_auditoria_tabla   (tabla, id_registro),
    INDEX idx_auditoria_usuario (id_usuario),
    INDEX idx_auditoria_fecha   (creado_en)
);
