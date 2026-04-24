package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA15CrearTablaRetiroCaja20260302 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("retiro_caja", table -> {
                table.id("retiro");
                table.integer("idRel_apertura").notNull();
                table.decimal("monto_retirado", 10, 2).notNull();
                table.string("razon_retiro", 500).notNull();
                table.datetime("fecha_retiro").notNull();
                table.foreign("idRel_apertura", "apertura_caja", "id_apertura");
            }, conexion);

            System.out.println("Tabla 'retiro_caja' creada exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla retiro_caja: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("retiro_caja", conexion);
            System.out.println("Tabla 'retiro_caja' eliminada exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla retiro_caja: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
