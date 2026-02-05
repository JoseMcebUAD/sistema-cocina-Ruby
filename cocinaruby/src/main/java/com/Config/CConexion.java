package com.Config;
/**
 * Clase principal para conectarse 
 */

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.JOptionPane;

public class CConexion {

    private Connection conectar = null;

    private final String usuario;
    private final String contrasena;
    private final String cadena;
    private final Properties config;

    public CConexion() {
        this("config.properties");
    }

    public CConexion(String rutaArchivoConfig){
        this.config = this.cargarConfiguracion(rutaArchivoConfig);

        this.usuario = this.config.getProperty("db.usuario");
        this.contrasena = this.config.getProperty("db.contrasena");

        String host = this.config.getProperty("db.host");
        String puerto = this.config.getProperty("db.puerto");
        String bd = this.config.getProperty("db.nombre");

        this.cadena = "jdbc:mariadb://" + host + ":" + puerto + "/" + bd;

    }
    /**
     * Carga la configuración desde el classpath (dentro del JAR/resources)
     *
     * @param rutaArchivoConfig Nombre del archivo en resources
     * @return Properties con la configuración
     */
    private Properties cargarConfiguracion(String rutaArchivoConfig){
        Properties prop = new Properties();

        try(InputStream input = getClass().getClassLoader().getResourceAsStream(rutaArchivoConfig)){
            if (input == null) {
                JOptionPane.showMessageDialog(null, "No se encontró el archivo de configuracion: " + rutaArchivoConfig);
                return prop;
            }
            prop.load(input);
        }catch(IOException exception){
            JOptionPane.showMessageDialog(null, "No se ha podido cargar el archivo de configuracion");
        }

        return prop;
    }

    public Connection establecerConexionDb(){
        try {
            conectar = DriverManager.getConnection(cadena, usuario, contrasena);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "No se pudo conectar a la base de datos: " + e.getMessage());
        }
        return conectar;
    }
}
