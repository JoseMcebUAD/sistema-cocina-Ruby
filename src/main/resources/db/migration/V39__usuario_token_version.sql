-- V39: token_version en usuario para invalidar JWT emitidos al cambiar credenciales.
-- Cada JWT lleva el claim "ver"; el filtro rechaza tokens con ver < usuario.token_version.
-- Incrementar esta columna revoca todos los tokens activos del usuario.

ALTER TABLE usuario ADD COLUMN token_version INT NOT NULL DEFAULT 0;
