package com.DAO;

import java.sql.Connection;
import java.sql.SQLException;

import com.Config.CConexion;
import com.Config.DatabasePool;

/**
 * Clase base abstracta para todos los DAOs.
 * Proporciona la infraestructura común de conexión a base de datos
 * y elimina la duplicación de código en las clases DAO.
 *
 * Usa HikariCP para pooling de conexiones en producción.
 * Soporta inyección de CConexion para pruebas.
 */
public abstract class BaseDAO {

    protected CConexion conector;
    private final boolean usePool;

    /**
     * Constructor estándar para la aplicación.
     * Usa el pool de conexiones HikariCP (recomendado).
     */
    public BaseDAO() {
        this.conector = null;
        this.usePool = true;
    }

    /**
     * Constructor para Pruebas (Inyección de Dependencias).
     * Usa CConexion directamente sin pool (para tests aislados).
     * @param conector Un conector de base de datos (ej. uno de prueba).
     */
    public BaseDAO(CConexion conector) {
        this.conector = conector;
        this.usePool = false;
    }

    /**
     * Método helper para obtener una conexión a la base de datos.
     *
     * En producción: Usa HikariCP pool (rápido, reutiliza conexiones).
     * En tests: Usa CConexion directamente (aislado).
     *
     * IMPORTANTE: Siempre usar try-with-resources para cerrar la conexión.
     *
     * @return Connection objeto de conexión a la BD.
     * @throws SQLException si ocurre un error al establecer la conexión.
     */
    protected Connection getConnection() throws SQLException {
        if (usePool) {
            return DatabasePool.getConnection();
        }
        return conector.establecerConexionDb();
    }
}