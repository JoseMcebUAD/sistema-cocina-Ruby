package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA2CrearTablaUsuario20250105 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("usuario", table -> {
                table.id("usuario");
                table.integer("idRel_tipo_usuario").notNull();
                table.string("nombre_usuario", 50).notNull();
                table.string("contrasena_usuario", 200).notNull();

                table.foreign("idRel_tipo_usuario", "tipo_usuario", "id_tipo_usuario");
            }, conexion);

            System.out.println("Tabla 'usuario' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("usuario", conexion);
            System.out.println("Tabla 'usuario' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
