package com.View;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.AppRunner;

/**
 * Clase Main para iniciar la aplicación Cocina Ruby.
 *
 * Flujo de inicialización:
 * 1. Crear instancia de AppRunner
 * 2. agregar los config necesarios
 * 5. (Opcional) Inicializar impresora
 * 6. Cargar UI
 * 7. Mostrar ventana
 */
public class Main extends Application {

    private AppRunner app;

    @Override
    public void start(Stage primaryStage) throws Exception {
        app = new AppRunner();
        //verificamos que exits al base de datos
        if (!app.initializeDatabase()) {
            System.err.println("Fallo crítico: No se pudo conectar a la base de datos");
            System.exit(1);
            return;
        }
        //opcional
        if (!app.initializeSession()) {
            System.err.println("⚠ Advertencia: No se pudo inicializar la sesión");
            System.err.println("   Se cargará la vista de autenticación por defecto");
        }

        String fxmlPath = app.determineViewPath();

        // opional para las impresoras
        // app.initializePrinterService();
        // app.startPrinterMonitor();

        // 6. Cargar UI (DESPUÉS de las configuraciones)
        if (!app.loadUI(fxmlPath)) {
            System.err.println("   Verifica que el archivo FXML exista: " + fxmlPath);
            System.exit(1);
            return;
        }

        // 7. Obtener Scene y configurar Stage
        Scene scene = app.getScene();
        scene.setFill(Color.TRANSPARENT);

        // Configurar Stage
        primaryStage.setScene(scene);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        // Si carga menu.fxml, maximizar y no permitir redimensionar
        // Si carga auth.fxml, permitir redimensionar
        if (fxmlPath.contains("menu.fxml")) {
            primaryStage.setMaximized(true);
            primaryStage.setResizable(false);
        } else {
            primaryStage.setResizable(true);
        }

        // Mostrar ventana
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
 

        if (app != null) {
            // Guardar timestamp de cierre de sesión
            app.saveSessionToken();

            // Limpiar recursos (detiene printer monitor, cierra BD)
            app.cleanup();
        }

        super.stop();
        System.out.println("✓ Aplicación cerrada correctamente");
        System.out.println("========================================\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

