package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA14CrearTablaCierreCaja20260302 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("cierre_caja", table -> {
                table.id("cierre");
                table.integer("idRel_apertura").notNull();
                table.datetime("fecha_cierre").notNull();
                table.decimal("monto_esperado", 10, 2).notNull();
                table.decimal("monto_real", 10, 2).notNull();
                table.decimal("diferencia", 10, 2).notNull();
                table.text("observaciones").nullable();
                table.foreign("idRel_apertura", "apertura_caja", "id_apertura");
            }, conexion);

            System.out.println("Tabla 'cierre_caja' creada exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla cierre_caja: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("cierre_caja", conexion);
            System.out.println("Tabla 'cierre_caja' eliminada exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla cierre_caja: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
