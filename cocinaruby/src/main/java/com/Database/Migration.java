package com.Database;
import java.sql.Connection;

import javax.xml.validation.Schema;

import com.Config.CConexion;

/**
 * esta clase abstracta funciona para hacer las migraciones de manera generica
 * Utiliza el patron de diseño del builder del Schema 
 */
public abstract class Migration {

    //string de la fecha en formato YYYY-MM-DD
    public String fechaCreado;
    //nombre de la base de datos que estamos usando, si estamos usando una distribuida
    protected String DBName;
    //conexion con la base de datos
    protected Connection conexion;

    //schema de la base de datos 
    protected Schema schema;

    //builder para generar la base de datos

    //constructor para instanciar la conexion y el schema
    public Migration(){
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
    public abstract void up();

    public abstract void down();


}
