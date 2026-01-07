package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA5CrearTablaTipoPago20250105 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("tipo_pago", table -> {
                table.id("tipo_pago");
                table.string("nombre_tipo_pago", 30).notNull();
            }, conexion);

            System.out.println("Tabla 'tipo_pago' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("tipo_pago", conexion);
            System.out.println("Tabla 'tipo_pago' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
