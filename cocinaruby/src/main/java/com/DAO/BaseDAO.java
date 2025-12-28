package com.DAO;

import java.sql.Connection;
import java.sql.SQLException;

import com.Config.CConexion;

/**
 * Clase base abstracta para todos los DAOs.
 * Proporciona la infraestructura común de conexión a base de datos
 * y elimina la duplicación de código en las clases DAO.
 */
public abstract class BaseDAO {
    
    protected CConexion conector;
    
    /**
     * Constructor estándar para la aplicación.
     * Usa la conexión de producción.
     */
    public BaseDAO() {
        this.conector = new CConexion();
    }
    
    /**
     * Constructor para Pruebas (Inyección de Dependencias).
     * @param conector Un conector de base de datos (ej. uno de prueba).
     */
    public BaseDAO(CConexion conector) {
        this.conector = conector;
    }
    
    /**
     * Método helper para obtener una conexión a la base de datos.
     * @return Connection objeto de conexión a la BD.
     * @throws SQLException si ocurre un error al establecer la conexión.
     */
    protected Connection getConnection() throws SQLException {
        return conector.establecerConexionDb();
    }
}