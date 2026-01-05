package com.Database;

import com.Config.CConexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema avanzado de migraciones con soporte para:
 * - Lectura automática de archivos YYYYMMDD-NombreClase.java
 * - Registro en tabla de migraciones
 * - Transacciones con rollback automático
 * - Respaldo y restauración de datos
 *
 * Comandos:
 *   up              - Ejecuta migraciones pendientes
 *   down            - Revierte el último batch de migraciones
 *   reup            - Recrea todas las migraciones (DOWN + UP)
 *   reup-with-data  - Recrea migraciones preservando datos
 *   status          - Muestra estado de migraciones
 */
public class MigrationRunner {

    private static final String MIGRATIONS_PATH = "src/main/java/com/Database/migrations";
    private Connection connection;
    private MigrationRecord migrationRecord;
    private MigrationLoader migrationLoader;
    private TableDataBackup tableDataBackup;

    public MigrationRunner() {
        CConexion con = new CConexion();
        this.connection = con.establecerConexionDb();
        this.migrationRecord = new MigrationRecord(connection);
        this.migrationLoader = new MigrationLoader(MIGRATIONS_PATH);
        this.tableDataBackup = new TableDataBackup(connection);
    }

    public static void main(String[] args) {
        String action = args.length > 0 ? args[0] : "up";

        printHeader(action);

        MigrationRunner runner = new MigrationRunner();

        try {
            // Crear tabla de migraciones si no existe
            runner.migrationRecord.createMigrationsTableIfNotExists();

            switch (action.toLowerCase()) {
                case "up":
                    runner.runMigrationsUp();
                    break;
                case "down":
                    runner.runMigrationsDown();
                    break;
                case "reup":
                    runner.runReup(false);
                    break;
                case "reup-with-data":
                    runner.runReup(true);
                    break;
                case "status":
                    runner.showStatus();
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
     * Ejecuta migraciones pendientes
     */
    private void runMigrationsUp() throws Exception {
        System.out.println("\n>>> Ejecutando migraciones pendientes...\n");

        List<MigrationLoader.MigrationFile> allMigrations = migrationLoader.loadMigrationFiles();

        if (allMigrations.isEmpty()) {
            System.out.println("ℹ No hay archivos de migración disponibles.");
            return;
        }

        // Filtrar migraciones pendientes
        List<MigrationLoader.MigrationFile> pendingMigrations = new ArrayList<>();
        for (MigrationLoader.MigrationFile file : allMigrations) {
            if (!migrationRecord.isMigrationExecuted(file.version, file.className)) {
                pendingMigrations.add(file);
            }
        }

        if (pendingMigrations.isEmpty()) {
            System.out.println("✓ No hay migraciones pendientes. Todo está actualizado.");
            return;
        }

        int batchNumber = migrationRecord.getNextBatchNumber();
        System.out.println("📦 Batch número: " + batchNumber);
        System.out.println("📋 Migraciones a ejecutar: " + pendingMigrations.size() + "\n");

        int count = 0;
        for (MigrationLoader.MigrationFile migrationFile : pendingMigrations) {
            count++;
            executeMigrationUp(migrationFile, count, pendingMigrations.size(), batchNumber);
        }

        System.out.println("\n✓ Todas las migraciones se ejecutaron exitosamente.");
    }

    /**
     * Ejecuta una migración individual con transacción
     */
    private void executeMigrationUp(MigrationLoader.MigrationFile migrationFile, int current, int total, int batch) throws Exception {
        System.out.println("[" + current + "/" + total + "] Ejecutando: " + migrationFile.getFullName());

        try {
            // Iniciar transacción
            connection.setAutoCommit(false);

            long startTime = System.currentTimeMillis();

            // Cargar y ejecutar migración
            Migration migration = migrationLoader.loadMigrationInstance(migrationFile);
            migration.up();

            long executionTime = System.currentTimeMillis() - startTime;

            // Registrar migración
            migrationRecord.recordMigration(migrationFile.version, migrationFile.className, executionTime, batch);

            // Confirmar transacción
            connection.commit();

            System.out.println("    ✓ Completado en " + executionTime + "ms\n");

        } catch (Exception e) {
            // Rollback en caso de error
            try {
                connection.rollback();
                System.err.println("    ⚠ Transacción revertida (rollback)");
            } catch (SQLException rollbackEx) {
                System.err.println("    ⚠ Error al hacer rollback: " + rollbackEx.getMessage());
            }

            // Mostrar error detallado
            System.err.println("\n❌ ERROR EN MIGRACIÓN: " + migrationFile.getFullName());
            System.err.println("📄 Archivo: " + migrationFile.file.getAbsolutePath());
            System.err.println("💬 Mensaje: " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();

            throw new Exception("Fallo en migración: " + migrationFile.getFullName(), e);

        } finally {
            // Restaurar auto-commit
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("⚠ Error al restaurar auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Revierte el último batch de migraciones
     */
    private void runMigrationsDown() throws Exception {
        System.out.println("\n>>> Revirtiendo último batch de migraciones...\n");

        List<MigrationRecord.MigrationInfo> lastBatch = migrationRecord.getLastBatchMigrations();

        if (lastBatch.isEmpty()) {
            System.out.println("ℹ No hay migraciones para revertir.");
            return;
        }

        System.out.println("📦 Batch a revertir: " + lastBatch.get(0).batch);
        System.out.println("📋 Migraciones a revertir: " + lastBatch.size() + "\n");

        int count = 0;
        for (MigrationRecord.MigrationInfo info : lastBatch) {
            count++;
            executeMigrationDown(info, count, lastBatch.size());
        }

        System.out.println("\n✓ Batch revertido exitosamente.");
    }

    /**
     * Ejecuta el down de una migración con transacción
     */
    private void executeMigrationDown(MigrationRecord.MigrationInfo info, int current, int total) throws Exception {
        System.out.println("[" + current + "/" + total + "] Revirtiendo: " + info.getFullName());

        try {
            // Iniciar transacción
            connection.setAutoCommit(false);

            // Cargar y ejecutar down
            MigrationLoader.MigrationFile migrationFile = new MigrationLoader.MigrationFile(
                info.version, info.className, null
            );
            Migration migration = migrationLoader.loadMigrationInstance(migrationFile);
            migration.down();

            // Eliminar registro de migración
            migrationRecord.deleteMigration(info.version, info.className);

            // Confirmar transacción
            connection.commit();

            System.out.println("    ✓ Revertido\n");

        } catch (Exception e) {
            // Rollback en caso de error
            try {
                connection.rollback();
                System.err.println("    ⚠ Transacción revertida (rollback)");
            } catch (SQLException rollbackEx) {
                System.err.println("    ⚠ Error al hacer rollback: " + rollbackEx.getMessage());
            }

            System.err.println("\n❌ ERROR AL REVERTIR: " + info.getFullName());
            System.err.println("💬 Mensaje: " + e.getMessage());
            e.printStackTrace();

            throw new Exception("Fallo al revertir migración: " + info.getFullName(), e);

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("⚠ Error al restaurar auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Recrea todas las migraciones (DOWN ALL + UP ALL)
     */
    private void runReup(boolean preserveData) throws Exception {
        if (preserveData) {
            System.out.println("\n>>> Recreando migraciones CON preservación de datos...\n");
        } else {
            System.out.println("\n>>> Recreando migraciones SIN preservar datos...\n");
        }

        Map<String, TableDataBackup.TableBackup> backups = new HashMap<>();

        // Paso 1: Respaldar datos si es necesario
        if (preserveData) {
            System.out.println("📦 Paso 1/3: Respaldando datos...\n");
            List<String> tables = tableDataBackup.getAllTables();

            for (String tableName : tables) {
                try {
                    TableDataBackup.TableBackup backup = tableDataBackup.backupTable(tableName);
                    backups.put(tableName, backup);
                    System.out.println("  ✓ " + tableName + " (" + backup.rows.size() + " filas)");
                } catch (SQLException e) {
                    System.err.println("  ⚠ Error al respaldar " + tableName + ": " + e.getMessage());
                }
            }
            System.out.println();
        }

        // Paso 2: Revertir todas las migraciones
        System.out.println(preserveData ? "📦 Paso 2/3: Revirtiendo migraciones...\n" : "📦 Paso 1/2: Revirtiendo migraciones...\n");
        revertAllMigrations();

        // Paso 3: Ejecutar todas las migraciones
        System.out.println(preserveData ? "\n📦 Paso 3/3: Ejecutando migraciones...\n" : "\n📦 Paso 2/2: Ejecutando migraciones...\n");
        runMigrationsUp();

        // Paso 4: Restaurar datos si es necesario
        if (preserveData && !backups.isEmpty()) {
            System.out.println("\n📦 Restaurando datos...\n");

            for (Map.Entry<String, TableDataBackup.TableBackup> entry : backups.entrySet()) {
                String tableName = entry.getKey();
                TableDataBackup.TableBackup backup = entry.getValue();

                try {
                    TableDataBackup.RestoreResult result = tableDataBackup.restoreTable(backup);

                    if (result.insertedRows > 0) {
                        System.out.println("  ✓ " + tableName + ": " + result.insertedRows + " filas restauradas");
                    }

                    if (result.skippedRows > 0) {
                        System.out.println("  ⚠ " + tableName + ": " + result.skippedRows + " filas omitidas");
                        for (String error : result.errors) {
                            System.out.println("    - " + error);
                        }
                    }

                } catch (SQLException e) {
                    System.err.println("  ⚠ Error al restaurar " + tableName + ": " + e.getMessage());
                }
            }
        }

        System.out.println("\n✓ Recreación de migraciones completada.");
    }

    /**
     * Revierte todas las migraciones
     */
    private void revertAllMigrations() throws Exception {
        while (true) {
            List<MigrationRecord.MigrationInfo> lastBatch = migrationRecord.getLastBatchMigrations();
            if (lastBatch.isEmpty()) {
                break;
            }

            for (MigrationRecord.MigrationInfo info : lastBatch) {
                executeMigrationDown(info, 1, 1);
            }
        }
    }

    /**
     * Muestra el estado de las migraciones
     */
    private void showStatus() throws Exception {
        System.out.println("\n>>> Estado de migraciones\n");

        List<MigrationRecord.MigrationInfo> executed = migrationRecord.getExecutedMigrations();

        if (executed.isEmpty()) {
            System.out.println("ℹ No hay migraciones ejecutadas.");
        } else {
            System.out.println("📋 Migraciones ejecutadas:\n");
            System.out.printf("%-12s %-40s %-10s %-8s%n", "VERSION", "CLASE", "TIEMPO", "BATCH");
            System.out.println("-".repeat(75));

            for (MigrationRecord.MigrationInfo info : executed) {
                System.out.printf("%-12s %-40s %-10s %-8s%n",
                    info.version,
                    info.className,
                    info.executionTimeMs + "ms",
                    "#" + info.batch
                );
            }
        }

        // Mostrar migraciones pendientes
        List<MigrationLoader.MigrationFile> allMigrations = migrationLoader.loadMigrationFiles();
        List<MigrationLoader.MigrationFile> pending = new ArrayList<>();

        for (MigrationLoader.MigrationFile file : allMigrations) {
            if (!migrationRecord.isMigrationExecuted(file.version, file.className)) {
                pending.add(file);
            }
        }

        if (!pending.isEmpty()) {
            System.out.println("\n⏳ Migraciones pendientes:\n");
            for (MigrationLoader.MigrationFile file : pending) {
                System.out.println("  - " + file.getFullName());
            }
        }

        System.out.println();
    }

    // Métodos auxiliares de impresión

    private static void printHeader(String action) {
        System.out.println("=".repeat(70));
        System.out.println("  🚀 MIGRATION RUNNER - Comando: " + action.toUpperCase());
        System.out.println("=".repeat(70));
    }

    private static void printFooter() {
        System.out.println("=".repeat(70));
        System.out.println("  ✓ MIGRATION RUNNER - Completado");
        System.out.println("=".repeat(70));
    }

    private static void showUsage() {
        System.out.println("\nComandos disponibles:");
        System.out.println("  up              - Ejecuta migraciones pendientes");
        System.out.println("  down            - Revierte el último batch");
        System.out.println("  reup            - Recrea todas las migraciones (DOWN + UP)");
        System.out.println("  reup-with-data  - Recrea migraciones preservando datos");
        System.out.println("  status          - Muestra estado de migraciones");
    }
}
