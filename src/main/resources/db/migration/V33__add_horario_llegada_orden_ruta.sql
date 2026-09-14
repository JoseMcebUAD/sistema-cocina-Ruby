-- Agrega rango de horario estimado de llegada a la tabla orden_ruta.
-- Permite comunicar a los clientes en qué ventana horaria recibirán su pedido.
ALTER TABLE orden_ruta
    ADD COLUMN hora_llegada_desde TIME NULL,
    ADD COLUMN hora_llegada_hasta TIME NULL;
