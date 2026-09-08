-- Permite eliminar un RegistroCliente dejando los pedidos asociados con id_registro_cliente = NULL.
ALTER TABLE pedido_domicilio_cocina
    DROP FOREIGN KEY fk_ped_dom_coc_registro;

ALTER TABLE pedido_domicilio_cocina
    MODIFY COLUMN id_registro_cliente INT NULL;

ALTER TABLE pedido_domicilio_cocina
    ADD CONSTRAINT fk_ped_dom_coc_registro
        FOREIGN KEY (id_registro_cliente) REFERENCES registro_cliente (id_registro_cliente)
        ON UPDATE CASCADE ON DELETE SET NULL;
