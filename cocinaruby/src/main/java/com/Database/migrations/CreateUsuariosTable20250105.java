package com.Database.migrations;

import java.sql.SQLException;

import com.Database.Migration;
import com.Database.Schema;

/**
 * Migración: Crear tabla de usuarios
 * Fecha: 2025-01-05
 *
 * Formato: NombreClaseYYYYMMDD.java
 * - El nombre de la clase puede ser cualquier identificador Java válido
 * - La fecha (YYYYMMDD) va al final del nombre
 * - Esto evita problemas de sintaxis en Java (los nombres de clase no pueden empezar con números)
 */
public class CreateUsuariosTable20250105 extends Migration {

    @Override
    public void up() {
        try {
            Schema.create("usuarios", table -> {
                table.id();
                table.string("nombre", 100).notNull();
                table.string("apellido", 100).notNull();
                table.string("email").unique().notNull();
                table.string("password").notNull();
                table.string("telefono", 20).nullable();
                table.bool("activo").defaultValue("1");
                table.timestamps();
            }, conexion);

            System.out.println("    ✓ Tabla 'usuarios' creada");

        } catch (SQLException e) {
            throw new RuntimeException("Error al crear tabla usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public void down() {
        try {
            Schema.dropIfExists("usuarios", conexion);
            System.out.println("    ✓ Tabla 'usuarios' eliminada");

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar tabla usuarios: " + e.getMessage(), e);
        }
    }
}
