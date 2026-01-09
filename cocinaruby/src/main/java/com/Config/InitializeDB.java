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

   
}
