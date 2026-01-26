package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;
import java.sql.Statement;

public class AA12ActualizarViewVentas20260124 extends Migration {

    @Override
    public void up() {
        String viewBuilder = """
            CREATE OR REPLACE VIEW view_ventas AS
            SELECT DISTINCT
                o.id_orden,
                o.idRel_tipo_pago,
                o.tipo_cliente,
                o.fecha_expedicion_orden,
                o.precio_orden,
                COALESCE(orden_especifica.nombre_cliente, 'Desconocido') AS nombre_cliente,
                o.pago_cliente,
                COALESCE(od.tarifa_domicilio, 0.0) AS tarifa_domicilio
            FROM orden AS o
            LEFT JOIN orden_domicilio AS od ON o.id_orden = od.id_orden
            LEFT JOIN (
                SELECT
                    om.id_orden,
                    om.nombre_persona AS nombre_cliente
                FROM orden_mostrador AS om
                UNION ALL
                SELECT
                    od2.id_orden,
                    COALESCE(c.nombre_cliente, 'Domicilio desconocido') AS nombre_cliente
                FROM orden_domicilio AS od2
                LEFT JOIN cliente AS c
                    ON c.id_cliente = od2.idRel_cliente
                UNION ALL
                SELECT
                    omesa.id_orden,
                    CONCAT('Mesa ', CAST(omesa.numero_mesa AS CHAR)) AS nombre_cliente
                FROM orden_mesa AS omesa
            ) AS orden_especifica
            ON orden_especifica.id_orden = o.id_orden
            ORDER BY o.fecha_expedicion_orden DESC
                """;
        try {
            Statement stm = conexion.createStatement();
            stm.execute(viewBuilder);
            stm.close();
            System.out.println("Vista 'view_ventas' actualizada con campo 'tarifa_domicilio' exitosamente");
        } catch(SQLException e){
            System.err.println("Error al actualizar la vista: " + e.getMessage());
            throw new RuntimeException("Fallo al actualizar view_ventas", e);
        }
    }

    @Override
    public void down() {
        // Revertir a la versión anterior de la vista (sin tarifa_domicilio)
        String viewBuilder = """
            CREATE OR REPLACE VIEW view_ventas AS
            SELECT DISTINCT
                o.id_orden,
                o.idRel_tipo_pago,
                o.tipo_cliente,
                o.fecha_expedicion_orden,
                o.precio_orden,
                COALESCE(orden_especifica.nombre_cliente, 'Desconocido') AS nombre_cliente,
                o.pago_cliente
            FROM orden AS o
            LEFT JOIN (
                SELECT
                    om.id_orden,
                    'Mostrador' AS nombre_cliente
                FROM orden_mostrador AS om
                UNION ALL
                SELECT
                    od.id_orden,
                    COALESCE(c.nombre_cliente, 'Domicilio desconocido') AS nombre_cliente
                FROM orden_domicilio AS od
                LEFT JOIN cliente AS c
                    ON c.id_cliente = od.idRel_cliente
                UNION ALL
                SELECT
                    omesa.id_orden,
                    CONCAT('Mesa ', CAST(omesa.numero_mesa AS CHAR)) AS nombre_cliente
                FROM orden_mesa AS omesa
            ) AS orden_especifica
            ON orden_especifica.id_orden = o.id_orden
            ORDER BY o.fecha_expedicion_orden DESC
                """;
        try {
            Statement stm = conexion.createStatement();
            stm.execute(viewBuilder);
            stm.close();
            System.out.println("Vista 'view_ventas' revertida a versión anterior exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al revertir la vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
