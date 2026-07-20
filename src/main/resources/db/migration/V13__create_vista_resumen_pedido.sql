-- V13: Vista consolidada de pedidos (todos los orígenes y tipos).
--
-- Materializa como VIEW la consulta usada por VistaResumenPedidoRepository.findVistaConFiltros
-- para poder consultarla directamente desde SQL/BI sin depender de la capa JPA. Los filtros
-- (desde, hasta, tipoPedido, pedidoCreadoDesde) se aplican vía WHERE al momento de consultar.
--
-- Fuentes por origen/tipo:
--   COCINA + PICK_UP/MOSTRADOR -> pedido_cocina.nombre_cliente
--   COCINA + DOMICILIO         -> pedido_domicilio_cocina (ruta, domicilio, tarifa) + registro_cliente.nombre
--   WEB    + DOMICILIO         -> pedido_domicilio (ruta, direccion, tarifa) + cliente.nombre
--   WEB    + PICK_UP/MOSTRADOR -> cliente.nombre
--
-- COALESCE del nombre_cliente sigue el orden: pedido_cocina > registro_cliente > cliente,
-- espejo del JPQL para mantener paridad de resultados con la API.

CREATE OR REPLACE VIEW vista_resumen_pedido AS
SELECT
    p.id_pedido                                        AS id_pedido,
    p.impreso                                          AS impreso,
    COALESCE(pc.nombre_cliente, rc.nombre, cli.nombre) AS nombre_cliente,
    p.metodo_pago_principal                            AS metodo_pago_principal,
    p.metodo_pago_secundario                           AS metodo_pago_secundario,
    p.tipo_pedido                                      AS tipo_pedido,
    p.pedido_creado_desde                              AS pedido_creado_desde,
    p.fecha_expedicion_pedido                          AS fecha_expedicion_pedido,
    p.precio_final_orden                               AS precio_final_orden,
    p.pago_cliente_principal                           AS pago_cliente_principal,
    COALESCE(r1.nombre, r2.nombre)                     AS ruta,
    COALESCE(pd.direccion, pdc.domicilio)              AS domicilio,
    COALESCE(pd.tarifa, pdc.precio_tarifa)             AS precio_tarifa
FROM pedido p
LEFT JOIN pedido_cocina           pc  ON pc.id_pedido            = p.id_pedido
LEFT JOIN pedido_domicilio        pd  ON pd.id_pedido            = p.id_pedido
LEFT JOIN ruta                    r1  ON r1.id_ruta              = pd.id_ruta
LEFT JOIN pedido_domicilio_cocina pdc ON pdc.id_pedido           = p.id_pedido
LEFT JOIN registro_cliente        rc  ON rc.id_registro_cliente  = pdc.id_registro_cliente
LEFT JOIN ruta                    r2  ON r2.id_ruta              = pdc.id_ruta
LEFT JOIN cliente                 cli ON cli.uuid_cliente        = p.uuid_cliente;
