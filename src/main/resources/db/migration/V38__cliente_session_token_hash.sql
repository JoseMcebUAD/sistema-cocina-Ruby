-- V38: Reemplaza session_token en texto plano por hash SHA-256 hex.
-- Motivo: un dump/leak de la tabla cliente permitia suplantacion directa.
-- El hash preserva la unicidad y el indice pero es no-reversible.

ALTER TABLE cliente ADD COLUMN session_token_hash CHAR(64);

-- Migra los tokens existentes: los usuarios web mantienen su sesion vigente
-- porque el filtro hasheara el token entrante y comparara contra este valor.
UPDATE cliente
   SET session_token_hash = LOWER(SHA2(session_token, 256))
 WHERE session_token IS NOT NULL;

-- Ahora el hash es la fuente de verdad para lookups
CREATE UNIQUE INDEX idx_cliente_session_hash ON cliente (session_token_hash);

ALTER TABLE cliente DROP COLUMN session_token;

ALTER TABLE cliente MODIFY COLUMN session_token_hash CHAR(64) NOT NULL;
