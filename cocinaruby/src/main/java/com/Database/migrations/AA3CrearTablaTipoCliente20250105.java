package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA3CrearTablaTipoCliente20250105 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("tipo_cliente", table -> {
                table.id("tipo_cliente");
                table.string("nombre_tipo_cliente", 50).notNull();
            }, conexion);

            System.out.println("Tabla 'tipo_cliente' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("tipo_cliente", conexion);
            System.out.println("Tabla 'tipo_cliente' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
