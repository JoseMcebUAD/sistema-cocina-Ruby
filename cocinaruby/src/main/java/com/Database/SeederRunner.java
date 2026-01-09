package com.Database;

import com.Config.CConexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Sistema de ejecución de seeders para poblar la base de datos con datos iniciales
 *
 * Comandos:
 *   seeder:up [NombreSeeder] - Ejecuta un seeder específico
 *   seeder:up-all            - Ejecuta todos los seeders disponibles
 */
public class SeederRunner {

    private static final String SEEDERS_PATH = "src/main/java/com/Database/seeders";
    private Connection connection;
    private SeederLoader seederLoader;

    public SeederRunner() {
        CConexion con = new CConexion();
        this.connection = con.establecerConexionDb();
        this.seederLoader = new SeederLoader(SEEDERS_PATH);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("❌ Error: Debes proporcionar un comando");
            showUsage();
            System.exit(1);
        }

        String action = args[0];
        printHeader(action);

        SeederRunner runner = new SeederRunner();

        try {
            switch (action.toLowerCase()) {
                case "seeder:up":
                    if (args.length < 2) {
                        System.err.println("❌ Error: Debes proporcionar el nombre del seeder");
                        System.err.println("Uso: seeder.bat seeder:up NombreSeeder");
                        System.exit(1);
                    }
                    runner.runSeeder(args[1]);
                    break;
                case "seeder:up-all":
                    runner.runAllSeeders();
                    break;
                default:
                    System.err.println("❌ Comando desconocido: " + action);
                    showUsage();
                    System.exit(1);
            }

            printFooter();

        } catch (Exception e) {
            System.err.println("\n❌ ERROR FATAL: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Ejecuta un seeder específico por nombre
     */
    private void runSeeder(String seederName) throws Exception {
        System.out.println("\n>>> Ejecutando seeder: " + seederName + "\n");

        List<SeederLoader.SeederFile> allSeeders = seederLoader.loadSeedersFile();

        if (allSeeders.isEmpty()) {
            System.out.println("ℹ No se encontraron seeders en: " + SEEDERS_PATH);
            return;
        }

        // Buscar el seeder específico
        SeederLoader.SeederFile targetSeeder = null;
        for (SeederLoader.SeederFile seederFile : allSeeders) {
            if (seederFile.className.equalsIgnoreCase(seederName)) {
                targetSeeder = seederFile;
                break;
            }
        }

        if (targetSeeder == null) {
            System.err.println("❌ Error: No se encontró el seeder '" + seederName + "'");
            System.out.println("\nSeeders disponibles:");
            for (SeederLoader.SeederFile seederFile : allSeeders) {
                System.out.println("  - " + seederFile.className);
            }
            return;
        }

        executeSeeder(targetSeeder);
        System.out.println("\n✓ Seeder ejecutado exitosamente.");
    }

    /**
     * Ejecuta todos los seeders disponibles
     */
    private void runAllSeeders() throws Exception {
        System.out.println("\n>>> Ejecutando todos los seeders...\n");

        List<SeederLoader.SeederFile> allSeeders = seederLoader.loadSeedersFile();

        if (allSeeders.isEmpty()) {
            System.out.println("ℹ No se encontraron seeders en: " + SEEDERS_PATH);
            return;
        }

        System.out.println("📋 Seeders a ejecutar: " + allSeeders.size() + "\n");

        int count = 0;
        for (SeederLoader.SeederFile seederFile : allSeeders) {
            count++;
            System.out.println("[" + count + "/" + allSeeders.size() + "] Ejecutando: " + seederFile.className);

            try {
                executeSeeder(seederFile);
                System.out.println("    ✓ Completado\n");
            } catch (Exception e) {
                System.err.println("    ❌ Error en seeder: " + seederFile.className);
                System.err.println("    💬 Mensaje: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        System.out.println("\n✓ Todos los seeders se ejecutaron exitosamente.");
    }

    /**
     * Ejecuta un seeder individual con transacción
     */
    private void executeSeeder(SeederLoader.SeederFile seederFile) throws Exception {
        try {
            long startTime = System.currentTimeMillis();

            // Iniciar transacción
            connection.setAutoCommit(false);

            // Cargar y ejecutar seeder
            Seeder seeder = seederLoader.loadSeedersInstance(seederFile);
            seeder.run();

            // Confirmar transacción
            connection.commit();

            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("    ⏱ Tiempo de ejecución: " + executionTime + "ms");

        } catch (Exception e) {
            // Rollback en caso de error
            try {
                connection.rollback();
                System.err.println("    ⚠ Transacción revertida (rollback)");
            } catch (SQLException rollbackEx) {
                System.err.println("    ⚠ Error al hacer rollback: " + rollbackEx.getMessage());
            }

            throw new Exception("Fallo en seeder: " + seederFile.getFullName(), e);

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("⚠ Error al restaurar auto-commit: " + e.getMessage());
            }
        }
    }

    // Métodos auxiliares de impresión

    private static void printHeader(String action) {
        System.out.println("=".repeat(70));
        System.out.println("  🌱 SEEDER RUNNER - Comando: " + action.toUpperCase());
        System.out.println("=".repeat(70));
    }

    private static void printFooter() {
        System.out.println("=".repeat(70));
        System.out.println("  ✓ SEEDER RUNNER - Completado");
        System.out.println("=".repeat(70));
    }

    private static void showUsage() {
        System.out.println("\nComandos disponibles:");
        System.out.println("  seeder:up [NombreSeeder] - Ejecuta un seeder específico");
        System.out.println("  seeder:up-all            - Ejecuta todos los seeders");
    }
}
