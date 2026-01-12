package com.Database;

import com.Config.CConexion;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase base para tests de migraciones
 * Proporciona conexión a BD y limpieza automática
 */
public abstract class BaseTest {

    protected Connection connection;
    protected MigrationRecord migrationRecord;

    @Before
    public void setUp() throws SQLException {
        // Establecer conexión usando la base de datos de prueba
        CConexion con = new CConexion("config-test.properties");
        connection = con.establecerConexionDb();
        migrationRecord = new MigrationRecord(connection);

        // Crear tabla de migraciones si no existe
        migrationRecord.createMigrationsTableIfNotExists();

        // Limpiar datos de pruebas anteriores
        cleanupTestData();
    }

    @After
    public void tearDown() throws SQLException {
        // Limpiar después de cada test
        cleanupTestData();

        // Cerrar conexión
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Limpia todas las tablas de prueba.
     * Como estamos usando una base de datos separada para tests,
     * es seguro limpiar TODAS las migraciones.
     */
    protected void cleanupTestData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Desactivar verificación de llaves foráneas temporalmente
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Eliminar tablas de prueba comunes
            String[] testTables = {
                "test_usuarios",
                "test_productos",
                "test_categorias",
                "test_ventas"
            };

            for (String table : testTables) {
                try {
                    stmt.execute("DROP TABLE IF EXISTS " + table);
                } catch (SQLException e) {
                    // Ignorar si la tabla no existe
                }
            }

            // Limpiar TODAS las migraciones (seguro porque estamos en BD de prueba)
            try {
                stmt.execute("DELETE FROM migrations");
            } catch (SQLException e) {
                // Ignorar si la tabla no existe
            }

            // Reactivar verificación de llaves foráneas
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    /**
     * Verifica que una tabla existe
     */
    protected boolean tableExists(String tableName) throws SQLException {
        String query = "SHOW TABLES LIKE '" + tableName + "'";
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(query)) {
            return rs.next();
        }
    }

    /**
     * Cuenta las filas en una tabla
     */
    protected int countRows(String tableName) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }
}
