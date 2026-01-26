package com.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Pool de conexiones a la base de datos usando HikariCP.
 *
 * Ventajas sobre DriverManager directo:
 * - Reutiliza conexiones existentes (~0.5ms vs ~50ms por conexión)
 * - Manejo automático de conexiones inactivas
 * - Validación de conexiones antes de entregarlas
 * - Thread-safe para aplicaciones concurrentes
 *
 * Uso:
 *   Connection conn = DatabasePool.getConnection();
 *   // usar conexión...
 *   conn.close(); // Devuelve al pool, no cierra físicamente
 */
public class DatabasePool {

    private static HikariDataSource dataSource;
    private static boolean initialized = false;
    private static final Object lock = new Object();

    /**
     * Inicializa el pool de conexiones.
     * Se llama automáticamente en el primer getConnection() si no se ha inicializado.
     *
     * @param configFile Ruta al archivo de configuración (ej: "config.properties")
     */
    public static void initialize(String configFile) {
        synchronized (lock) {
            if (initialized) {
                System.out.println("⚠ DatabasePool ya está inicializado");
                return;
            }

            try {
                Properties props = loadProperties(configFile);
                HikariConfig config = createHikariConfig(props);
                dataSource = new HikariDataSource(config);
                initialized = true;
                System.out.println("✓ Pool de conexiones HikariCP inicializado");
                System.out.println("  - Máximo conexiones: " + config.getMaximumPoolSize());
                System.out.println("  - Mínimo idle: " + config.getMinimumIdle());
            } catch (Exception e) {
                System.err.println("❌ Error al inicializar pool de conexiones: " + e.getMessage());
                throw new RuntimeException("No se pudo inicializar el pool de conexiones", e);
            }
        }
    }

    /**
     * Inicializa con configuración por defecto (config.properties).
     */
    public static void initialize() {
        initialize("config.properties");
    }

    /**
     * Obtiene una conexión del pool.
     * Si el pool no está inicializado, lo inicializa automáticamente.
     *
     * IMPORTANTE: Siempre cerrar la conexión con conn.close() o usar try-with-resources.
     * Esto devuelve la conexión al pool, no la cierra físicamente.
     *
     * @return Connection lista para usar
     * @throws SQLException si no se puede obtener una conexión
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return dataSource.getConnection();
    }

    /**
     * Cierra el pool de conexiones y libera todos los recursos.
     * Debe llamarse al cerrar la aplicación.
     */
    public static void shutdown() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                initialized = false;
                System.out.println("✓ Pool de conexiones cerrado");
            }
        }
    }

    /**
     * Verifica si el pool está inicializado y activo.
     *
     * @return true si el pool está listo para usar
     */
    public static boolean isInitialized() {
        return initialized && dataSource != null && !dataSource.isClosed();
    }

    /**
     * Obtiene estadísticas del pool (útil para debugging).
     *
     * @return String con información del estado del pool
     */
    public static String getPoolStats() {
        if (!initialized || dataSource == null) {
            return "Pool no inicializado";
        }
        return String.format(
            "Pool Stats: Active=%d, Idle=%d, Waiting=%d, Total=%d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
            dataSource.getHikariPoolMXBean().getTotalConnections()
        );
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Carga las propiedades desde el archivo de configuración.
     */
    private static Properties loadProperties(String configFile) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        }
        return props;
    }

    /**
     * Crea la configuración de HikariCP basada en las propiedades.
     */
    private static HikariConfig createHikariConfig(Properties props) {
        HikariConfig config = new HikariConfig();

        // Configuración de conexión
        String host = props.getProperty("db.host", "localhost");
        String puerto = props.getProperty("db.puerto", "3306");
        String nombre = props.getProperty("db.nombre", "sistema-cocina-ruby");

        String jdbcUrl = "jdbc:mariadb://" + host + ":" + puerto + "/" + nombre;
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(props.getProperty("db.usuario", "root"));
        config.setPassword(props.getProperty("db.contrasena", ""));

        // Configuración del pool
        config.setMaximumPoolSize(10);          // Máximo de conexiones simultáneas
        config.setMinimumIdle(2);               // Mínimo de conexiones en espera
        config.setIdleTimeout(300000);          // 5 minutos antes de cerrar conexión idle
        config.setConnectionTimeout(20000);     // 20 segundos para obtener conexión
        config.setMaxLifetime(1800000);         // 30 minutos vida máxima de conexión
        config.setLeakDetectionThreshold(60000); // Detectar leaks después de 60 segundos

        // Configuración de validación
        config.setConnectionTestQuery("SELECT 1");

        // Nombre del pool (útil para logs)
        config.setPoolName("CocinaRubyPool");

        return config;
    }
}
