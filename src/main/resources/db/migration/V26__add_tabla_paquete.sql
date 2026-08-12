-- Módulo Paquete (Fase 3): agrupa productos (comida/desayuno/complemento/producto_cocina)
-- en promociones con precio propio, y permite venderlos como línea de pedido.

CREATE TABLE IF NOT EXISTS paquete (
    id_paquete INT NOT NULL AUTO_INCREMENT,
    precio DECIMAL(7,2) NOT NULL,
    descripcion VARCHAR(90) NULL,
    estatus ENUM('DISPONIBLE','NO_DISPONIBLE','AGOTADO') NOT NULL DEFAULT 'DISPONIBLE',
    PRIMARY KEY (id_paquete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Pivote polimórfica: id_producto no tiene FK física porque referencia distintas tablas
-- según tipo_producto (patrón replicado de favorito_cliente).
CREATE TABLE IF NOT EXISTS paquete_producto (
    id_paquete_producto INT NOT NULL AUTO_INCREMENT,
    id_paquete INT NOT NULL,
    tipo_producto ENUM('COMIDA','DESAYUNO','COMPLEMENTO','PRODUCTO_COCINA') NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id_paquete_producto),
    CONSTRAINT uq_paquete_producto UNIQUE (id_paquete, tipo_producto, id_producto),
    CONSTRAINT fk_pp_paquete FOREIGN KEY (id_paquete)
        REFERENCES paquete(id_paquete) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Línea de pedido: un paquete puede formar parte de un pedido con cantidad y precio congelados.
CREATE TABLE IF NOT EXISTS paquete_pedido (
    id_paquete_pedido INT NOT NULL AUTO_INCREMENT,
    id_pedido INT NOT NULL,
    id_paquete INT NOT NULL,
    precio_unitario DECIMAL(7,2) NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id_paquete_pedido),
    CONSTRAINT fk_pp_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_pp_paquete_ref FOREIGN KEY (id_paquete)
        REFERENCES paquete(id_paquete) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
