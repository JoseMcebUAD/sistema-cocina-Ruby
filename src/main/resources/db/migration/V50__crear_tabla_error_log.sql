CREATE TABLE error_log (
    id_error    INT          NOT NULL AUTO_INCREMENT,
    path        VARCHAR(255) NOT NULL,
    metodo_http VARCHAR(10)  NOT NULL,
    tipo_error  VARCHAR(255) NOT NULL,
    mensaje     TEXT         NULL,
    stack_trace TEXT         NULL,
    id_usuario  INT          NULL,
    id_sucursal INT          NULL,
    creado_en   DATETIME     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_error),
    INDEX idx_error_fecha (creado_en),
    INDEX idx_error_tipo  (tipo_error)
);
