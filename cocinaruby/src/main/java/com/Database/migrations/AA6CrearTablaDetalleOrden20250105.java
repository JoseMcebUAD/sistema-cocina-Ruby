package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;
import java.sql.Statement;

public class AA6CrearTablaDetalleOrden20250105 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("detalle_orden", table -> {
                table.id("detalle_orden");
                table.integer("idRel_orden").notNull();
                table.integer("cantidad").notNull();
                table.string("especificaciones_detalle_orden", 100).nullable();
                table.decimal("precio_detalle_orden", 10, 2).notNull();

                table.foreign("idRel_orden", "orden", "id_orden").onDeleteCascade();
            }, conexion);

            System.out.println("Tabla 'detalle_orden' creada exitosamente");

            // Crear triggers para actualizar automáticamente el precio_orden
            String triggerInsert = """
                CREATE TRIGGER trg_detalle_insert
                AFTER INSERT ON detalle_orden
                FOR EACH ROW
                BEGIN
                  UPDATE orden
                  SET precio_orden = precio_orden + NEW.precio_detalle_orden
                  WHERE id_orden = NEW.idRel_orden;
                END
                """;

            String triggerUpdate = """
                CREATE TRIGGER trg_detalle_update
                AFTER UPDATE ON detalle_orden
                FOR EACH ROW
                BEGIN
                  UPDATE orden
                  SET precio_orden = precio_orden
                      - OLD.precio_detalle_orden
                      + NEW.precio_detalle_orden
                  WHERE id_orden = NEW.idRel_orden;
                END
                """;

            String triggerDelete = """
                CREATE TRIGGER trg_detalle_delete
                AFTER DELETE ON detalle_orden
                FOR EACH ROW
                BEGIN
                  UPDATE orden
                  SET precio_orden = precio_orden - OLD.precio_detalle_orden
                  WHERE id_orden = OLD.idRel_orden;
                END
                """;

            try (Statement stmt = conexion.createStatement()) {
                stmt.execute(triggerInsert);
                stmt.execute(triggerUpdate);
                stmt.execute(triggerDelete);
                System.out.println("Triggers para 'detalle_orden' creados exitosamente");
            }

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla o triggers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            // Eliminar triggers primero
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("DROP TRIGGER IF EXISTS trg_detalle_insert");
                stmt.execute("DROP TRIGGER IF EXISTS trg_detalle_update");
                stmt.execute("DROP TRIGGER IF EXISTS trg_detalle_delete");
                System.out.println("Triggers eliminados exitosamente");
            }

            // Luego eliminar la tabla
            MigrationSchema.dropIfExists("detalle_orden", conexion);
            System.out.println("Tabla 'detalle_orden' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla o triggers: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
