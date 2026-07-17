-- V11: Soporte de clientes y datos de contacto para pedidos creados desde COCINA.
--
-- registro_cliente : Directorio reutilizable de clientes manuales (pedidoCreadoDesde=COCINA).
-- pedido_domicilio_cocina : Extensión 1:1 de pedido para COCINA+DOMICILIO.
--                           Guarda snapshot de ruta y precio para preservar historial.
-- pedido_cocina           : Extensión 1:1 de pedido para COCINA+PICK_UP/MOSTRADOR.
--                           Solo almacena el nombre del cliente para el despacho.

CREATE TABLE IF NOT EXISTS registro_cliente (
    id_registro_cliente INT          NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(255) NOT NULL,
    telefono            VARCHAR(16)  NOT NULL,
    id_ruta             INT          NULL,
    direccion           VARCHAR(255) NULL,
    PRIMARY KEY (id_registro_cliente),
    CONSTRAINT fk_reg_cli_ruta
        FOREIGN KEY (id_ruta) REFERENCES ruta (id_ruta)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pedido_domicilio_cocina (
    id_pedido           INT          NOT NULL,
    id_registro_cliente INT          NOT NULL,
    id_ruta             INT          NOT NULL,
    domicilio           VARCHAR(255) NOT NULL,
    precio_tarifa       DECIMAL(6,2) NOT NULL,
    PRIMARY KEY (id_pedido),
    CONSTRAINT fk_ped_dom_coc_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ped_dom_coc_registro
        FOREIGN KEY (id_registro_cliente) REFERENCES registro_cliente (id_registro_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_ped_dom_coc_ruta
        FOREIGN KEY (id_ruta) REFERENCES ruta (id_ruta)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pedido_cocina (
    id_pedido      INT          NOT NULL,
    nombre_cliente VARCHAR(255) NULL,
    PRIMARY KEY (id_pedido),
    CONSTRAINT fk_ped_cocina_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
