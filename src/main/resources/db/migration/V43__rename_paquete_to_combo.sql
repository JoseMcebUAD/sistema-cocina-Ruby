-- Renombrar tablas paquete → combo
RENAME TABLE paquete TO combo;
RENAME TABLE paquete_producto TO combo_producto;
RENAME TABLE paquete_pedido TO combo_pedido;

-- Renombrar columna PK en combo
ALTER TABLE combo RENAME COLUMN id_paquete TO id_combo;

-- Renombrar columnas en combo_producto
ALTER TABLE combo_producto RENAME COLUMN id_paquete_producto TO id_combo_producto;
ALTER TABLE combo_producto RENAME COLUMN id_paquete TO id_combo;

-- Renombrar columnas en combo_pedido
ALTER TABLE combo_pedido RENAME COLUMN id_paquete_pedido TO id_combo_pedido;
ALTER TABLE combo_pedido RENAME COLUMN id_paquete TO id_combo;
