-- Añadir POSTRE a las columnas ENUM que referencian tipo de producto/catálogo

ALTER TABLE producto_cocina
    MODIFY tipo_producto ENUM('SNACK','CHAROLA','BEBIDA','POSTRE') NOT NULL;

ALTER TABLE archivo_modulo
    MODIFY tipo_catalogo_producto ENUM('BASICO','COMIDA','DESAYUNO','SNACK','CHAROLA','BEBIDA','POSTRE') NULL;

ALTER TABLE favorito_cliente
    MODIFY tipo_catalogo_producto ENUM('BASICO','COMIDA','DESAYUNO','SNACK','CHAROLA','BEBIDA','POSTRE') NOT NULL;
