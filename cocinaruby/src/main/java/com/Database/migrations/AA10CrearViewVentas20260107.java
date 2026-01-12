package com.Database.migrations;

import java.sql.SQLException;
import java.sql.Statement;

import com.Database.Migration;
import com.Database.MigrationSchema;

public class AA10CrearViewVentas20260107 extends Migration {

    @Override
    public void up() {
        String viewBuilder = """
            CREATE OR REPLACE VIEW view_ventas AS
            SELECT 
                o.id_orden,
                o.idRel_tipo_pago, 
                o.tipo_cliente,
                o.fecha_expedicion_orden,
                o.precio_orden,
                orden_especifica.nombre_cliente,
                o.pago_cliente,

                COALESCE(
                    JSON_ARRAYAGG(
                        JSON_OBJECT(
                            'id_detalle_orden', detalle.id_detalle_orden,
                            'especificaciones_detalle_orden', detalle.especificaciones_detalle_orden,
                            'precio_detalle_orden', detalle.precio_detalle_orden,
                            'cantidad', detalle.cantidad
                        )
                    ),
                    JSON_ARRAY()
                ) AS detalle_orden

            FROM orden AS o

            LEFT JOIN detalle_orden AS detalle 
                ON detalle.idRel_orden = o.id_orden

            LEFT JOIN (
                SELECT 
                    om.id_orden,
                    NULL AS id_cliente,
                    'Mostrador' AS nombre_cliente
                FROM orden_mostrador AS om

                UNION ALL

                SELECT 
                    od.id_orden,
                    c.id_cliente,
                    c.nombre_cliente
                FROM orden_domicilio AS od
                LEFT JOIN cliente AS c 
                    ON c.id_cliente = od.idRel_cliente

                UNION ALL
                
                SELECT 
                    omesa.id_orden,
                    NULL AS id_cliente,
                    CONCAT('Mesa ', omesa.numero_mesa) AS nombre_cliente
                FROM orden_mesa AS omesa
            ) AS orden_especifica 
            ON orden_especifica.id_orden = o.id_orden

            GROUP BY 
                o.id_orden,
                o.idRel_tipo_pago,
                o.tipo_cliente,
                o.fecha_expedicion_orden,
                o.precio_orden,
                orden_especifica.nombre_cliente,
                o.pago_cliente;


                """;
        try(Statement stm = conexion.createStatement()){
            stm.execute(viewBuilder);
            stm.close();
        }catch(SQLException e){
            System.err.println("Error al crear la tabla o triggers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
      try {
            MigrationSchema.dropIfExists("view_ventas", conexion);
            System.out.println("Tabla 'view_ventas' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
