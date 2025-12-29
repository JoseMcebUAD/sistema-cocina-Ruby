package com.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

public class InitializeDB {
    /**
     * 
     * @param JDBCString conexion jdbc onc la base de datos
     */
    public void initializeDB(Connection conn,String nombreDB) throws SQLException{
        try(conn;
            Statement stmt = conn.createStatement();){
                ResultSet rs = stmt.executeQuery("SHOW DATABASE LIKE '"+nombreDB +"'");
                //la base de datos no ha sido creada
                if(!rs.next()){
                    System.out.println("Creando la base de datos: " + nombreDB);
                    //crear la base de datos
                    stmt.executeQuery("CREATE DATABASE IF NOT EXISTS"+ nombreDB + "CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
                    System.out.println("Base de datos '" + nombreDB + "' creada exitosamente.");

                }else{
                    System.out.println("base de datos ya ha sido creada");
                }
            }
    }

        /**
     * Ejecuta el script SQL para crear las tablas e insertar datos iniciales.
     *
     * @param nombreBD Nombre de la base de datos
     * @param host Host del servidor MySQL
     * @param puerto Puerto del servidor MySQL
     * @param usuario Usuario de MySQL
     * @param contrasena Contraseña de MySQL
     */
    public static void ejecutarScriptSQL(Connection conn, String nombreDB ) {
  
        try (conn;
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(nombreDB +".sql"))) {

            System.out.println("Ejecutando script SQL para crear tablas...");

            StringBuilder sqlScript = new StringBuilder();
            String linea;

            while ((linea = reader.readLine()) != null) {
                // Ignorar comentarios y líneas vacías
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("--") || linea.startsWith("/*") || linea.startsWith("*")) {
                    continue;
                }

                sqlScript.append(linea).append(" ");

                // Si la línea termina con punto y coma, ejecutar el comando
                if (linea.endsWith(";")) {
                    String comando = sqlScript.toString().trim();

                    // Ignorar comandos específicos de MySQL que pueden causar problemas
                    if (!comando.startsWith("SET") &&
                        !comando.startsWith("/*!") &&
                        !comando.startsWith("START TRANSACTION") &&
                        !comando.startsWith("COMMIT")) {

                        try {
                            stmt.execute(comando);
                        } catch (SQLException e) {
                            // Continuar con el siguiente comando si hay un error
                            System.err.println("Error ejecutando comando SQL: " + e.getMessage());
                        }
                    }

                    sqlScript.setLength(0); // Limpiar el buffer
                }
            }

            System.out.println("Script SQL ejecutado exitosamente.");
            System.out.println("Base de datos inicializada con tablas y datos de ejemplo.");

        } catch (IOException e) {
            System.err.println("Advertencia: No se pudo leer el archivo " +nombreDB+".sql: " + e.getMessage());
            System.err.println("Las tablas deberán crearse manualmente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Error al ejecutar el script SQL: " + e.getMessage(),
                "Error de Script SQL",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
