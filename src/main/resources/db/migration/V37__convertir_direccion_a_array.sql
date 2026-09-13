-- V37: Convierte la columna direccion de registro_cliente de un JSON string escalar
--      a un JSON array para soportar múltiples direcciones por cliente.
--      Los registros existentes quedan como un array de un elemento.
UPDATE registro_cliente
    SET direccion = JSON_ARRAY(JSON_UNQUOTE(direccion))
    WHERE direccion IS NOT NULL;
