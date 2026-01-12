package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA9CrearTablaOrdenMesa20250111 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("orden_mesa", table -> {
                table.integer("id_orden").notNull().primary();
                table.string("numero_mesa", 30).nullable();

                table.foreign("id_orden", "orden", "id_orden").onDeleteCascade().onUpdateCascade();
            }, conexion);

            System.out.println("Tabla 'orden_mesa' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("orden_mesa", conexion);
            System.out.println("Tabla 'orden_mesa' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
