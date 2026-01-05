package com.Database;

import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Tests para la clase MigrationRecord
 * Ejecutar con: mvn test -Dtest=MigrationRecordTest
 */
public class MigrationRecordTest extends BaseTest {

    @Test
    public void testCreateMigrationsTable() throws SQLException {
        // Verificar que la tabla existe
        assertTrue("La tabla 'migrations' debe existir", tableExists("migrations"));
    }

    @Test
    public void testRecordMigration() throws SQLException {
        // Registrar una migración de prueba
        migrationRecord.recordMigration("99990101", "TestMigration", 100L, 1);

        // Verificar que se registró
        assertTrue("La migración debe estar registrada",
                migrationRecord.isMigrationExecuted("99990101", "TestMigration"));
    }

    @Test(expected = SQLException.class)
    public void testPreventDuplicates() throws SQLException {
        // Registrar una migración
        migrationRecord.recordMigration("99990101", "TestMigration", 100L, 1);

        // Intentar registrar la misma migración debe fallar
        migrationRecord.recordMigration("99990101", "TestMigration", 100L, 1);
    }

    @Test
    public void testDeleteMigration() throws SQLException {
        // Registrar una migración
        migrationRecord.recordMigration("99990101", "TestMigration", 100L, 1);

        // Verificar que existe
        assertTrue(migrationRecord.isMigrationExecuted("99990101", "TestMigration"));

        // Eliminar
        migrationRecord.deleteMigration("99990101", "TestMigration");

        // Verificar que ya no existe
        assertFalse("La migración debe estar eliminada",
                migrationRecord.isMigrationExecuted("99990101", "TestMigration"));
    }

    @Test
    public void testGetExecutedMigrations() throws SQLException {
        // Registrar varias migraciones
        migrationRecord.recordMigration("99990101", "Migration1", 100L, 1);
        migrationRecord.recordMigration("99990102", "Migration2", 150L, 1);
        migrationRecord.recordMigration("99990103", "Migration3", 200L, 2);

        // Obtener todas
        List<MigrationRecord.MigrationInfo> migrations = migrationRecord.getExecutedMigrations();

        // Verificar
        assertTrue("Debe haber al menos 3 migraciones", migrations.size() >= 3);

        // Verificar que todas están presentes
        boolean found1 = false, found2 = false, found3 = false;
        for (MigrationRecord.MigrationInfo info : migrations) {
            if (info.version.equals("99990101")) found1 = true;
            if (info.version.equals("99990102")) found2 = true;
            if (info.version.equals("99990103")) found3 = true;
        }

        assertTrue("Todas las migraciones deben estar presentes", found1 && found2 && found3);
    }

    @Test
    public void testGetNextBatchNumber() throws SQLException {
        // Sin migraciones, debe ser 1
        int firstBatch = migrationRecord.getNextBatchNumber();
        assertEquals("El primer batch debe ser 1", 1, firstBatch);

        // Registrar una migración en batch 1
        migrationRecord.recordMigration("99990101", "Migration1", 100L, 1);

        // El siguiente debe ser 2
        int secondBatch = migrationRecord.getNextBatchNumber();
        assertEquals("El siguiente batch debe ser 2", 2, secondBatch);

        // Registrar en batch 2
        migrationRecord.recordMigration("99990102", "Migration2", 100L, 2);

        // El siguiente debe ser 3
        int thirdBatch = migrationRecord.getNextBatchNumber();
        assertEquals("El siguiente batch debe ser 3", 3, thirdBatch);
    }

    @Test
    public void testGetLastBatchMigrations() throws SQLException {
        // Registrar migraciones en diferentes batches
        migrationRecord.recordMigration("99990101", "Migration1", 100L, 1);
        migrationRecord.recordMigration("99990102", "Migration2", 150L, 1);
        migrationRecord.recordMigration("99990103", "Migration3", 200L, 2);
        migrationRecord.recordMigration("99990104", "Migration4", 250L, 2);

        // Obtener último batch
        List<MigrationRecord.MigrationInfo> lastBatch = migrationRecord.getLastBatchMigrations();

        // Verificar
        assertEquals("El último batch debe tener 2 migraciones", 2, lastBatch.size());

        // Verificar que son las correctas (en orden inverso)
        assertEquals("Primera migración del último batch", "99990104", lastBatch.get(0).version);
        assertEquals("Segunda migración del último batch", "99990103", lastBatch.get(1).version);
    }

    @Test
    public void testIsMigrationExecuted() throws SQLException {
        // No debe existir inicialmente
        assertFalse(migrationRecord.isMigrationExecuted("99990101", "TestMigration"));

        // Registrar
        migrationRecord.recordMigration("99990101", "TestMigration", 100L, 1);

        // Ahora debe existir
        assertTrue(migrationRecord.isMigrationExecuted("99990101", "TestMigration"));

        // Otra migración no debe existir
        assertFalse(migrationRecord.isMigrationExecuted("99990102", "OtherMigration"));
    }

    @Test
    public void testExecutionTime() throws SQLException {
        long expectedTime = 12345L;

        // Registrar con tiempo específico
        migrationRecord.recordMigration("99990101", "TestMigration", expectedTime, 1);

        // Obtener y verificar
        List<MigrationRecord.MigrationInfo> migrations = migrationRecord.getExecutedMigrations();
        MigrationRecord.MigrationInfo info = null;

        for (MigrationRecord.MigrationInfo m : migrations) {
            if (m.version.equals("99990101")) {
                info = m;
                break;
            }
        }

        assertNotNull("La migración debe existir", info);
        assertEquals("El tiempo de ejecución debe coincidir", expectedTime, info.executionTimeMs);
    }

    @Test
    public void testEmptyBatch() throws SQLException {
        // Sin migraciones
        List<MigrationRecord.MigrationInfo> emptyBatch = migrationRecord.getLastBatchMigrations();

        // Debe retornar lista vacía
        assertNotNull("No debe retornar null", emptyBatch);
        assertEquals("Debe estar vacío", 0, emptyBatch.size());
    }
}
