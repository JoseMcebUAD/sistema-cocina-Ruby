package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA1CrearTablaTipoUsuario20250105 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("tipo_usuario", table -> {
                table.id("tipo_usuario");
                table.string("nombre_tipo_usuario", 50).notNull();
                table.string("permisos_usuario", 1000).nullable();
            }, conexion);

            System.out.println("Tabla 'tipo_usuario' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("tipo_usuario", conexion);
            System.out.println("Tabla 'tipo_usuario' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
