package com.Database.migrations;

import com.Database.Migration;
import com.Database.MigrationSchema;
import java.sql.SQLException;

public class AA3CrearTablaCliente20250105 extends Migration {

    @Override
    public void up() {
        try {
            MigrationSchema.create("cliente", table -> {
                table.id("cliente");
                table.string("nombre_cliente", 50).nullable();
                table.string("direcciones", 400).nullable();
                table.string("numero_tarjeta", 50).nullable();
                table.string("telefono", 25).nullable();
            }, conexion);

            System.out.println("Tabla 'cliente' creada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void down() {
        try {
            MigrationSchema.dropIfExists("cliente", conexion);
            System.out.println("Tabla 'cliente' eliminada exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al eliminar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
