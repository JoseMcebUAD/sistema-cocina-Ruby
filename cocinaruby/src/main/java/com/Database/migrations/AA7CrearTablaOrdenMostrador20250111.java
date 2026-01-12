package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA7CrearTablaOrdenMostrador20250111 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("orden_mostrador", table -> {
                table.integer("id_orden").notNull().primary();
                table.string("nombre", 100).nullable();

                table.foreign("id_orden", "orden", "id_orden").onDeleteCascade().onUpdateCascade();
            }, conexion);

            System.out.println("Tabla 'orden_mostrador' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("orden_mostrador", conexion);
            System.out.println("Tabla 'orden_mostrador' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
