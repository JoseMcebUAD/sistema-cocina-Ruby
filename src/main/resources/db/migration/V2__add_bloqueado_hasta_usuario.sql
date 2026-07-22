ALTER TABLE usuario
    ADD COLUMN bloqueado_hasta DATETIME NULL DEFAULT NULL
        COMMENT 'Si no es NULL y está en el futuro, la cuenta está bloqueada';
