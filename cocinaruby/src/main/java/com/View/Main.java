package com.View;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.AppRunner;
import util.UsuarioTokenSistema;

/**
 * Clase Main para iniciar la aplicación Cocina Ruby
 * Usa AppRunner para inicializar la base de datos y utilidades del sistema
 */
public class Main extends Application {

    private AppRunner app;
    private UsuarioTokenSistema tokenSistema;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Determinar vista a cargar
        String fxmlPath = "/com/view/auth.fxml";

        // Inicializar AppRunner (inicializa DB, Parent y Scene)
        app = new AppRunner(fxmlPath);

        // Opcional: Inicializar impresora (descomentar si se necesita)
        // app.initializePrinterService();
        // app.startPrinterMonitor();

        // Obtener Scene de AppRunner
        Scene scene = app.getScene();
        scene.setFill(Color.TRANSPARENT);

        // Configurar stage
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        // Mostrar ventana
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // Guardar timestamp de cierre de sesión
        if (app != null) {
            try {
                app.setTokenUsuario();
                System.out.println("✓ Timestamp de cierre guardado");
            } catch (Exception e) {
                System.err.println("⚠ Error al guardar timestamp: " + e.getMessage());
            }

            // Detener monitor de impresora si está activo
            app.stopPrinterMonitor();
            app.closeDatabase();
        }

        System.out.println("✓ Aplicación cerrada correctamente");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
