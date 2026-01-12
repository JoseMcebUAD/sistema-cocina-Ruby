package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA9CrearTablaOrdenDomicilio20250111 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("orden_domicilio", table -> {
                table.integer("id_orden").notNull().primary();
                table.integer("idRel_cliente").nullable();
                table.string("direccion", 400).nullable();

                table.foreign("id_orden", "orden", "id_orden").onDeleteCascade().onUpdateCascade();
                table.foreign("idRel_cliente", "cliente", "id_cliente").onDeleteSetNull().onUpdateCascade();
            }, conexion);

            System.out.println("Tabla 'orden_domicilio' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("orden_domicilio", conexion);
            System.out.println("Tabla 'orden_domicilio' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
