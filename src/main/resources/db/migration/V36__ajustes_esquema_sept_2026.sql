-- 1. metodo_pago_principal nullable en pedido
ALTER TABLE pedido
  MODIFY COLUMN metodo_pago_principal
    ENUM('TARJETA','EFECTIVO','TRANSFERENCIA') NULL;

-- 2. Convertir direccion a JSON en registro_cliente
--    Primero envolver texto existente en JSON string válido para no romper datos actuales
UPDATE registro_cliente
  SET direccion = JSON_QUOTE(direccion)
  WHERE direccion IS NOT NULL;

ALTER TABLE registro_cliente
  MODIFY COLUMN direccion JSON NULL;

-- 3. Extender descripcion a VARCHAR(400) en tablas de catálogo
ALTER TABLE comida
  MODIFY COLUMN descripcion VARCHAR(400) NULL;
ALTER TABLE complemento
  MODIFY COLUMN descripcion VARCHAR(400) NULL;
ALTER TABLE basico
  MODIFY COLUMN descripcion VARCHAR(400) NULL;
ALTER TABLE producto_cocina
  MODIFY COLUMN descripcion VARCHAR(400) NULL;
ALTER TABLE desayuno
  MODIFY COLUMN descripcion VARCHAR(400) NULL;
ALTER TABLE paquete
  MODIFY COLUMN descripcion VARCHAR(400) NULL;
