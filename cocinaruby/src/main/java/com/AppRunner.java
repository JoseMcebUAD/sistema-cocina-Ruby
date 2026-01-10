package com;

import com.Config.CConexion;
import com.Config.Constants;
import util.PrinterServiceHolder;
import util.PrinterMonitor;
import util.UsuarioTokenSistema;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Builder para correr el programa
 * Inicializa la base de datos, Parent y Scene automáticamente
 */
public class AppRunner {

    // Componentes de JavaFX
    private Parent root;
    private Scene scene;

    // Componentes del sistema
    private CConexion conexion;
    private Connection dbConnection;
    private UsuarioTokenSistema tokenSistema;
    private boolean printerServiceInitialized;

    //variables
    private String rutaXml;

    /**
     * Constructor que inicializa la base de datos, Parent y Scene automáticamente
     * @param fxmlPath Ruta del archivo FXML a cargar
     */
    public AppRunner(String fxmlPath) throws Exception {
        // Inicializar base de datos
        initializeDatabase();
        this.rutaXml = fxmlPath;

        // Inicializar tokenSistema
        this.tokenSistema = new UsuarioTokenSistema();
        this.VerificarTokenUsuario(); 

        this.root = FXMLLoader.load(getClass().getResource(this.rutaXml));

        // Crear Scene
        this.scene = new Scene(root);
        this.printerServiceInitialized = false;

        System.out.println("✓ AppRunner inicializado con " + this.rutaXml);
    }

    /**
     * Inicializa la conexión a la base de datos
     */
    private void initializeDatabase() {
        try {
            conexion = new CConexion();
            dbConnection = conexion.establecerConexionDb();

            if (dbConnection != null && !dbConnection.isClosed()) {
                System.out.println("✓ Conexión a base de datos establecida");
            } else {
                System.err.println("⚠ No se pudo establecer conexión a la base de datos");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene o crea una instancia de UsuarioTokenSistema
     */
    public UsuarioTokenSistema getTokenSistema() {
        if (tokenSistema == null) {
            tokenSistema = new UsuarioTokenSistema();
            System.out.println("✓ UsuarioTokenSistema inicializado");
        }
        return tokenSistema;
    }
    /*
    * regresa una ruta de un fxml
    * Si hay cualquier error o no se encuentra el archivo, carga auth.fxml
     */
    public void VerificarTokenUsuario() {
        try {
            boolean tokenUsuario = this.tokenSistema.obtenerTokenUsuario();
            // Si tokenUsuario es false, significa que el token expiró (han pasado 3 horas)
            // Si es true, significa que el token aún es válido o no hay cierre guardado
            this.rutaXml = tokenUsuario ? "/com/view/menu.fxml" : "/com/view/auth.fxml";
        } catch (Exception e) {
            // Si hay cualquier error, cargar auth.fxml
            System.err.println("⚠ Error al verificar token: " + e.getMessage());
            this.rutaXml = "/com/view/auth.fxml";
        }
    }
    /**
     * Inicializa el servicio de impresora (llamar manualmente si se necesita)
     */
    public void initializePrinterService() {
        if (!printerServiceInitialized) {
            try {
                PrinterServiceHolder.INSTANCE.init(Constants.NOMBRE_IMPRESORA);
                System.out.println("✓ Servicio de impresora inicializado: " + Constants.NOMBRE_IMPRESORA);
                printerServiceInitialized = true;
            } catch (Exception e) {
                System.err.println("⚠ Error al inicializar la impresora: " + e.getMessage());
            }
        }
    }

    /**
     * Inicia el monitor de impresora (llamar manualmente si se necesita)
     */
    public void startPrinterMonitor() {
        if (!PrinterMonitor.isRunning()) {
            PrinterMonitor.start();
            System.out.println("✓ Monitor de impresora iniciado");
        }
    }

    /**
     * Detiene el monitor de impresora
     */
    public void stopPrinterMonitor() {
        if (PrinterMonitor.isRunning()) {
            PrinterMonitor.stop();
            System.out.println("✓ Monitor de impresora detenido");
        }
    }

    /**
     * Cierra la conexión a la base de datos
     */
    public void closeDatabase() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.out.println("✓ Conexión a base de datos cerrada");
            }
        } catch (SQLException e) {
            System.err.println("⚠ Error al cerrar la base de datos: " + e.getMessage());
        }
    }

    public void setTokenUsuario() throws IOException{
        this.tokenSistema.setTokenUsuario();
    }

    // ==================== GETTERS ====================

    public Parent getRoot() {
        return root;
    }

    public Scene getScene() {
        return scene;
    }

    public CConexion getConexion() {
        return conexion;
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    public boolean isPrinterServiceInitialized() {
        return printerServiceInitialized;
    }
}
