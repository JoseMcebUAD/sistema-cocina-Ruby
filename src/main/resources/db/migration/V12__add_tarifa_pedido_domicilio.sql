-- V12: Snapshot de tarifa de envío en pedido_domicilio (flujo WEB).
--
-- Se agrega la columna `tarifa` para congelar el costo de envío en el momento del pedido,
-- de forma análoga a `pedido_domicilio_cocina.precio_tarifa` (COCINA). Antes, el costo de
-- envío para WEB se calculaba dinámicamente desde `ruta.tarifa_envio`, lo que impedía
-- preservar el precio real cobrado si la tarifa de la ruta cambiaba después.

ALTER TABLE pedido_domicilio
    ADD COLUMN tarifa DECIMAL(6,2) NULL AFTER longitud;

-- Backfill de registros existentes con la tarifa vigente de su ruta.
UPDATE pedido_domicilio pd
JOIN ruta r ON r.id_ruta = pd.id_ruta
SET pd.tarifa = r.tarifa_envio
WHERE pd.tarifa IS NULL;

ALTER TABLE pedido_domicilio
    MODIFY COLUMN tarifa DECIMAL(6,2) NOT NULL;
