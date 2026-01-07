package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA6CrearTablaOrden20250105 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("orden", table -> {
                table.id("orden");
                table.integer("idRel_cliente").nullable();
                table.integer("idRel_tipo_pago").notNull();
                table.datetime("fecha_expedicion_orden").notNull().defaultValue("CURRENT_TIMESTAMP");
                table.string("notas_orden", 100).nullable();
                table.decimal("precio_orden", 10, 2).notNull().defaultValue("0.00");
                table.decimal("pago_cliente", 5, 2).nullable().defaultValue("0.00");
                table.bool("facturado").defaultValue("0");

                table.foreign("idRel_cliente", "cliente", "id_cliente").onDeleteSetNull();
                table.foreign("idRel_tipo_pago", "tipo_pago", "id_tipo_pago");

                table.index("fecha_expedicion_orden");
                table.index("facturado");
            }, conexion);

            System.out.println("Tabla 'orden' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("orden", conexion);
            System.out.println("Tabla 'orden' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
