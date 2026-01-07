package com.Database;

import java.sql.SQLException;

/**
 * Ejemplo de migración con relaciones entre tablas
 * Crea tablas: categorias, productos, y ventas
 */
public class MigrationExampleWithRelations extends Migration {

    @Override
    public void up() {
        try {
            // 1. Crear tabla categorias (debe crearse primero porque productos la referencia)
            MigrationSchema.create("categorias", table -> {
                table.id(); // id_categorias
                table.string("nombre", 100).notNull().unique();
                table.text("descripcion").nullable();
                table.bool("activo").defaultValue("1");
                table.timestamps();
            }, conexion);

            // 2. Crear tabla productos
            MigrationSchema.create("productos", table -> {
                table.id(); // id_productos
                table.string("nombre", 200).notNull();
                table.text("descripcion").nullable();
                table.decimal("precio", 10, 2).notNull();
                table.integer("stock").defaultValue("0");

                // Llave foránea a categorias
                table.integer("categoria_id").notNull();
                table.foreign("categoria_id", "categorias", "id_categorias")
                     .onDeleteCascade(); // Si se elimina la categoría, se eliminan sus productos

                // Índice para búsquedas rápidas
                table.index("nombre", "categoria_id");

                table.timestamps();
            }, conexion);

            // 3. Crear tabla ventas
            MigrationSchema.create("ventas", table -> {
                table.id(); // id_ventas

                // Llave foránea a productos
                table.integer("producto_id").notNull();
                table.foreign("producto_id", "productos", "id_productos")
                     .onDeleteCascade();

                table.integer("cantidad").notNull();
                table.decimal("precio_unitario", 10, 2).notNull();
                table.decimal("total", 10, 2).notNull();

                table.string("cliente_nombre", 150).notNull();
                table.datetime("fecha_venta").notNull();

                table.timestamps();
            }, conexion);

            System.out.println("Tablas creadas exitosamente:");
            System.out.println("- categorias");
            System.out.println("- productos");
            System.out.println("- ventas");

        } catch (SQLException e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            // Eliminar en orden inverso por las llaves foráneas
            MigrationSchema.dropIfExists("ventas", conexion);
            MigrationSchema.dropIfExists("productos", conexion);
            MigrationSchema.dropIfExists("categorias", conexion);

            System.out.println("Tablas eliminadas exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar las tablas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para ejecutar la migración
    public static void main(String[] args) {
        MigrationExampleWithRelations migration = new MigrationExampleWithRelations();

        System.out.println("=== Ejecutando migración UP ===");
        migration.up();

        // Para hacer rollback, descomentar la siguiente línea:
        // System.out.println("\n=== Ejecutando migración DOWN ===");
        // migration.down();
    }
}
