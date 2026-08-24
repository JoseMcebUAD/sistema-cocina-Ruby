-- V31: Agrega token_expiracion y huella a la tabla cliente.
--
-- token_expiracion : Fecha y hora en que expira el session_token del cliente web.
-- huella           : Fingerprint del navegador para identificar al visitante anónimo.

ALTER TABLE cliente
    ADD COLUMN token_expiracion DATETIME     NULL AFTER telefono,
    ADD COLUMN huella           VARCHAR(255) NULL AFTER token_expiracion;
