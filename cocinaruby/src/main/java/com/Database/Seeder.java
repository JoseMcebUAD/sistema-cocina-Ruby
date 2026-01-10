package com.Database;

import java.sql.Connection;

import com.Config.CConexion;

public abstract class Seeder {
    
    //string de la fecha en formato YYYY-MM-DD
    public String fechaCreado;
    //nombre de la base de datos que estamos usando, si estamos usando una distribuida
    protected String DBName;
    //conexion con la base de datos
    protected Connection conexion;
    //nombre de la tabla de la base de datos
    protected String TableName;

    //schema de la base de datos 
    protected MigrationSchema schema;

    //builder para generar la base de datos

    //constructor para instanciar la conexion y el schema
    public Seeder(){
        CConexion con = new CConexion();
        this.conexion = con.establecerConexionDb();
    }

    public String getDBName(){
        return this.DBName;
    }

    public String getFechaCreado(){
        return this.fechaCreado;
    }

    public void setFechaCreado(String fechaCreado){
        this.fechaCreado = fechaCreado;
    }

    /**
     * Método que debe implementarse para insertar los datos del seeder
     */
    public abstract void run();

}
