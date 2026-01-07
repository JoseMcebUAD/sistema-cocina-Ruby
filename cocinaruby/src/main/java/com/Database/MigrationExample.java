package com.Database;

import java.sql.SQLException;

/**
 * Ejemplo de migración para crear una tabla de usuarios
 *
 * up()   - Se ejecuta para crear la tabla
 * down() - Se ejecuta para eliminar la tabla (rollback)
 */
public class MigrationExample extends Migration {

    

    @Override
    public void up() {
        try {
            // Crear tabla de usuarios
            MigrationSchema.create("usuarios", table -> {
                // Columna ID autoincremental (será "id_usuarios")
                table.id();

                // Columnas de texto
                table.string("nombre", 100).notNull();
                table.string("apellido", 100).notNull();
                table.string("email").unique().notNull();
                table.string("password").notNull();

                // Columnas opcionales
                table.string("telefono", 20).nullable();
                table.text("direccion").nullable();

                // Columnas numericas
                table.integer("edad").nullable();
                table.decimal("salario", 10, 2).nullable();

                // Columna booleana
                table.bool("activo").defaultValue("1");

                // Columnas de fecha
                table.date("fecha_nacimiento").nullable();

                // Timestamps automáticos (created_at y updated_at)
                table.timestamps();

            }, conexion);

            System.out.println("Tabla 'usuarios' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            // Eliminar la tabla si existe
            MigrationSchema.dropIfExists("usuarios", conexion);
            System.out.println("Tabla 'usuarios' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
