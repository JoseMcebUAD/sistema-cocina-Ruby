package com.Database.migrations;

import java.sql.SQLException;

import com.Database.Migration;
import com.Database.Schema;

/**
 * Migración: Crear tabla de productos
 * Fecha: 2025-01-04
 */
public class CreateProductosTable20250104 extends Migration {

    @Override
    public void up() {
        try {
            Schema.create("productos", table -> {
                table.id();
                table.string("nombre", 200).notNull();
                table.text("descripcion").nullable();
                table.decimal("precio", 10, 2).notNull();
                table.integer("stock").defaultValue("0");
                table.bool("disponible").defaultValue("1");
                table.timestamps();
            }, conexion);

            System.out.println("    ✓ Tabla 'productos' creada");

        } catch (SQLException e) {
            throw new RuntimeException("Error al crear tabla productos: " + e.getMessage(), e);
        }
    }

    @Override
    public void down() {
        try {
            Schema.dropIfExists("productos", conexion);
            System.out.println("    ✓ Tabla 'productos' eliminada");

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar tabla productos: " + e.getMessage(), e);
        }
    }
}
