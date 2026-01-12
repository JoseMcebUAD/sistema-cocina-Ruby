package com.Database.seeders;

import com.Database.Seeder;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Seeder para insertar el usuario administrador inicial y tipo de usuario
 */
public class AdminSeeder extends Seeder {

    @Override
    public void run() {
        System.out.println("Ejecutando seeder: AdminSeeder...");

        try {
            // Insertar tipo de usuario administrador
            insertTipoUsuario(1, "Administrador", "todos");

            // Insertar usuario administrador
            insertUsuario(1, 1, "admin", "$2a$05$xGhNUHEhe.MxD0LmvW/mAOm5ZqbhEEuan8RP1VFojRZrMWlpX3T6q");

            //insertar los tipos de pago
            insertTipoPago(1,"Efectivo");
            insertTipoPago(2,"Transferencia");
            insertTipoPago(3,"Tarjeta");

            System.out.println("Seeders ejecutados exitosamente");

        } catch (SQLException e) {
            System.err.println("Error al ejecutar seeder: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void insertTipoUsuario(int id, String nombre, String permisos) throws SQLException {
        String sql = "INSERT INTO tipo_usuario (id_tipo_usuario, nombre_tipo_usuario, permisos_usuario) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nombre_tipo_usuario = ?, permisos_usuario = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, nombre);
            ps.setString(3, permisos);
            ps.setString(4, nombre);
            ps.setString(5, permisos);
            ps.executeUpdate();
        }
    }

    private void insertUsuario(int id, int idTipoUsuario, String nombreUsuario, String contrasena) throws SQLException {
        String sql = "INSERT INTO usuario (id_usuario, idRel_tipo_usuario, nombre_usuario, contrasena_usuario) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nombre_usuario = ?, contrasena_usuario = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, idTipoUsuario);
            ps.setString(3, nombreUsuario);
            ps.setString(4, contrasena);
            ps.setString(5, nombreUsuario);
            ps.setString(6, contrasena);
            ps.executeUpdate();
        }
    }
    
    private void insertTipoPago(int id, String nombreTipoPago) throws SQLException{
        String sql = "INSERT INTO tipo_pago (id_tipo_pago, nombre_tipo_pago) " +
                     "VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE nombre_tipo_pago = ?";
    
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, nombreTipoPago);
            ps.setString(3, nombreTipoPago);
            ps.executeUpdate();
        }
        
    }
}
