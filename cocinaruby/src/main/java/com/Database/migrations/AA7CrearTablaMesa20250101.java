package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA7CrearTablaMesa20250101 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("mesa", table -> {
                table.id("mesa");
                table.enumColumn("estado_mesa", "DISPONIBLE", "OCUPADO", "SUSPENDIDO")
                     .notNull()
                     .defaultValue("'DISPONIBLE'");

                table.index("estado_mesa");
            }, conexion);

            System.out.println("Tabla 'mesa' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla mesa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("mesa", conexion);
            System.out.println("Tabla 'mesa' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla mesa: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
