package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA13CrearTablaAperturaCaja20260302 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("apertura_caja", table -> {
                table.id("apertura");
                table.integer("idRel_usuario").notNull();
                table.decimal("monto_inicial", 10, 2).notNull();
                table.datetime("fecha_apertura").notNull();
                table.foreign("idRel_usuario", "usuario", "id_usuario");
            }, conexion);

            System.out.println("Tabla 'apertura_caja' creada exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla apertura_caja: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("apertura_caja", conexion);
            System.out.println("Tabla 'apertura_caja' eliminada exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla apertura_caja: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
