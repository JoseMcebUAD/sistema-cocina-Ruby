package com;

import com.Config.CConexion;
import com.Config.Constants;
import com.Config.DatabasePool;
import util.PrinterServiceHolder;
import util.session.SessionUsuario;
import util.PrinterMonitor;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestor de configuración y recursos del sistema.
 *
 * Flujo de inicialización recomendado:
 * 1. new AppRunner() - Solo crea la instancia
 * 2. initializeDatabase() - Conecta a BD
 * 3. initializeSession() - Verifica token de usuario
 * 4. initializePrinterService() - (Opcional) Inicializa impresora
 * 5. loadUI(fxmlPath) - Carga la interfaz según configuración
 * 6. getScene() - Obtiene Scene para mostrar en Stage
 *
 * El front (Main.java) tiene control total sobre cuándo inicializar cada componente.
 */
public class AppRunner {

    // Componentes de JavaFX (se crean DESPUÉS de las configuraciones)
    private Parent root;
    private Scene scene;

    // Componentes del sistema
    private CConexion conexion;
    private Connection dbConnection;
    private SessionUsuario sessionUsuario;
    private boolean printerServiceInitialized;

    // Estado de inicialización
    private boolean databaseInitialized;
    private boolean sessionInitialized;

    /**
     * Constructor vacío.
     * NO inicializa nada automáticamente. El front debe llamar los métodos init* explícitamente.
     */
    public AppRunner() {
        this.printerServiceInitialized = false;
        this.databaseInitialized = false;
        this.sessionInitialized = false;
    }

    /**
     * Inicializa la conexión a la base de datos.
     * Debe llamarse ANTES de cualquier operación que requiera BD.
     *
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public boolean initializeDatabase() {
        if (databaseInitialized) {
            System.out.println("⚠ Base de datos ya inicializada");
            return true;
        }

        try {
            conexion = new CConexion();
            dbConnection = conexion.establecerConexionDb();

            if (dbConnection != null && !dbConnection.isClosed()) {
                System.out.println("✓ Conexión a base de datos establecida");
                databaseInitialized = true;
                return true;
            } else {
                System.err.println("❌ No se pudo establecer conexión a la base de datos");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inicializa la sesión de usuario y verifica el token.
     * Debe llamarse DESPUÉS de initializeDatabase() si se necesita verificar token guardado.
     *
     * @return true si la sesión fue inicializada correctamente
     */
    public boolean initializeSession() {
        if (sessionInitialized) {
            System.out.println("⚠ Sesión ya inicializada");
            return true;
        }

        try {
            sessionUsuario = SessionUsuario.getInstance();
            sessionInitialized = true;
            System.out.println("✓ Sesión de usuario inicializada");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar sesión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Determina qué vista cargar basándose en las credenciales y token del usuario.
     * Requiere que initializeSession() haya sido llamado primero.
     *
     * Casos:
     * - Usuario tiene credenciales Y token NO expiró → menu.fxml
     * - Usuario NO tiene credenciales O token expiró → auth.fxml
     * - No hay información de sesión guardada → auth.fxml
     *
     * @return Ruta del FXML a cargar ("/com/view/auth.fxml" o "/com/view/menu.fxml")
     */
    public String determineViewPath() {
        if (!sessionInitialized || sessionUsuario == null) {
            System.err.println("⚠ Sesión no inicializada, cargando vista de autenticación");
            return "/com/view/auth.fxml";
        }

        try {
            boolean isAllowed = sessionUsuario.isUsuarioAllowedInMenu();

            // isAllowed = true → Usuario válido Y token vigente → ir a menu
            // isAllowed = false → Sin credenciales O token expiró → ir a login
            String viewPath = isAllowed ? "/com/view/menu.fxml" : "/com/view/auth.fxml";

            if (isAllowed) {
                System.out.println("✓ Usuario autenticado y token vigente → Cargando menú");
            } else {
                System.out.println("⚠ Sin credenciales o token expirado → Cargando autenticación");
            }

            return viewPath;
        } catch (Exception e) {
            System.err.println("⚠ Error al verificar sesión: " + e.getMessage());
            e.printStackTrace();
            return "/com/view/auth.fxml";
        }
    }

    /**
     * Carga la interfaz de usuario desde un archivo FXML.
     * Debe llamarse DESPUÉS de inicializar las configuraciones necesarias.
     *
     * @param fxmlPath Ruta del archivo FXML a cargar
     * @return true si la UI fue cargada correctamente, false en caso contrario
     */
    public boolean loadUI(String fxmlPath) {
        try {
            System.out.println("⏳ Cargando interfaz: " + fxmlPath);
            this.root = FXMLLoader.load(getClass().getResource(fxmlPath));
            this.scene = new Scene(root);
            System.out.println("✓ Interfaz cargada correctamente");
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al cargar la interfaz: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inicializa el servicio de impresora usando la configuración de Constants.
     * Es opcional y puede llamarse en cualquier momento.
     *
     * @return true si la impresora fue inicializada correctamente
     */
    public boolean initializePrinterService() {
        if (printerServiceInitialized) {
            System.out.println("⚠ Servicio de impresora ya inicializado");
            return true;
        }

        try {
            PrinterServiceHolder.INSTANCE.init(Constants.NOMBRE_IMPRESORA);
            System.out.println("✓ Servicio de impresora inicializado: " + Constants.NOMBRE_IMPRESORA);
            printerServiceInitialized = true;
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar la impresora: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inicia el monitor de impresora.
     * Requiere que initializePrinterService() haya sido llamado primero.
     */
    public void startPrinterMonitor() {
        if (!printerServiceInitialized) {
            System.err.println("⚠ Servicio de impresora no inicializado, no se puede iniciar monitor");
            return;
        }

        if (!PrinterMonitor.isRunning()) {
            PrinterMonitor.start();
            System.out.println("✓ Monitor de impresora iniciado");
        } else {
            System.out.println("⚠ Monitor de impresora ya está en ejecución");
        }
    }

    /**
     * Detiene el monitor de impresora si está en ejecución.
     */
    public void stopPrinterMonitor() {
        if (PrinterMonitor.isRunning()) {
            PrinterMonitor.stop();
            System.out.println("✓ Monitor de impresora detenido");
        }
    }

    /**
     * Guarda el timestamp de cierre de sesión.
     * Debe llamarse antes de cerrar la aplicación.
     */
    public void saveSessionToken() {
        if (!sessionInitialized || sessionUsuario == null) {
            System.err.println("⚠ Sesión no inicializada, no se puede guardar token");
            return;
        }

        try {
            sessionUsuario.guardarInicioSesion();
            System.out.println("✓ Token de sesión guardado");
        } catch (Exception e) {
            System.err.println("⚠ Error al guardar token de sesión: " + e.getMessage());
        }
    }

    /**
     * Cierra la conexión a la base de datos.
     * Debe llamarse al finalizar la aplicación.
     */
    public void closeDatabase() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                databaseInitialized = false;
                System.out.println("✓ Conexión a base de datos cerrada");
            }
        } catch (SQLException e) {
            System.err.println("⚠ Error al cerrar la base de datos: " + e.getMessage());
        }
    }

    /**
     * Limpia todos los recursos del sistema.
     * Debe llamarse en el método stop() de la aplicación.
     */
    public void cleanup() {
        stopPrinterMonitor();
        closeDatabase();

        // Cerrar pool de conexiones HikariCP
        DatabasePool.shutdown();

        System.out.println("✓ Recursos del sistema liberados");
    }

    // ==================== GETTERS ====================

    public Parent getRoot() {
        if (root == null) {
            System.err.println("⚠ Root no inicializado. Llama primero a loadUI()");
        }
        return root;
    }

    public Scene getScene() {
        if (scene == null) {
            System.err.println("⚠ Scene no inicializado. Llama primero a loadUI()");
        }
        return scene;
    }

    public SessionUsuario getSessionUsuario() {
        if (!sessionInitialized) {
            System.err.println("⚠ Sesión no inicializada. Llama primero a initializeSession()");
        }
        return sessionUsuario;
    }

    public CConexion getConexion() {
        if (!databaseInitialized) {
            System.err.println("⚠ Base de datos no inicializada. Llama primero a initializeDatabase()");
        }
        return conexion;
    }

    public Connection getDbConnection() {
        if (!databaseInitialized) {
            System.err.println("⚠ Base de datos no inicializada. Llama primero a initializeDatabase()");
        }
        return dbConnection;
    }

    public boolean isDatabaseInitialized() {
        return databaseInitialized;
    }

    public boolean isSessionInitialized() {
        return sessionInitialized;
    }

    public boolean isPrinterServiceInitialized() {
        return printerServiceInitialized;
    }
}
