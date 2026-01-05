package com.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja el registro de migraciones ejecutadas en la base de datos
 */
public class MigrationRecord {

    private Connection connection;

    public MigrationRecord(Connection connection) {
        this.connection = connection;
    }

    /**
     * Crea la tabla de migraciones si no existe
     */
    public void createMigrationsTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS migrations (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "version VARCHAR(8) NOT NULL, " +
                     "class VARCHAR(255) NOT NULL, " +
                     "time_ms BIGINT NOT NULL, " +
                     "batch INT NOT NULL, " +
                     "executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "UNIQUE KEY unique_migration (version, class)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Registra una migración ejecutada
     */
    public void recordMigration(String version, String className, long executionTimeMs, int batch) throws SQLException {
        String sql = "INSERT INTO migrations (version, class, time_ms, batch) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, version);
            pstmt.setString(2, className);
            pstmt.setLong(3, executionTimeMs);
            pstmt.setInt(4, batch);
            pstmt.executeUpdate();
        }
    }

    /**
     * Elimina el registro de una migración
     */
    public void deleteMigration(String version, String className) throws SQLException {
        String sql = "DELETE FROM migrations WHERE version = ? AND class = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, version);
            pstmt.setString(2, className);
            pstmt.executeUpdate();
        }
    }

    /**
     * Obtiene todas las migraciones ejecutadas
     */
    public List<MigrationInfo> getExecutedMigrations() throws SQLException {
        List<MigrationInfo> migrations = new ArrayList<>();
        String sql = "SELECT version, class, time_ms, batch, executed_at FROM migrations ORDER BY version ASC, class ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MigrationInfo info = new MigrationInfo(
                    rs.getString("version"),
                    rs.getString("class"),
                    rs.getLong("time_ms"),
                    rs.getInt("batch"),
                    rs.getTimestamp("executed_at")
                );
                migrations.add(info);
            }
        }

        return migrations;
    }

    /**
     * Obtiene el número del siguiente batch
     */
    public int getNextBatchNumber() throws SQLException {
        String sql = "SELECT COALESCE(MAX(batch), 0) + 1 as next_batch FROM migrations";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("next_batch");
            }
            return 1;
        }
    }

    /**
     * Obtiene las migraciones del último batch
     */
    public List<MigrationInfo> getLastBatchMigrations() throws SQLException {
        List<MigrationInfo> migrations = new ArrayList<>();
        String sql = "SELECT version, class, time_ms, batch, executed_at " +
                     "FROM migrations WHERE batch = (SELECT MAX(batch) FROM migrations) " +
                     "ORDER BY version DESC, class DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MigrationInfo info = new MigrationInfo(
                    rs.getString("version"),
                    rs.getString("class"),
                    rs.getLong("time_ms"),
                    rs.getInt("batch"),
                    rs.getTimestamp("executed_at")
                );
                migrations.add(info);
            }
        }

        return migrations;
    }

    /**
     * Verifica si una migración ya fue ejecutada
     */
    public boolean isMigrationExecuted(String version, String className) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM migrations WHERE version = ? AND class = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, version);
            pstmt.setString(2, className);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }

        return false;
    }

    /**
     * Clase interna para almacenar información de una migración
     */
    public static class MigrationInfo {
        public final String version;
        public final String className;
        public final long executionTimeMs;
        public final int batch;
        public final java.sql.Timestamp executedAt;

        public MigrationInfo(String version, String className, long executionTimeMs, int batch, java.sql.Timestamp executedAt) {
            this.version = version;
            this.className = className;
            this.executionTimeMs = executionTimeMs;
            this.batch = batch;
            this.executedAt = executedAt;
        }

        public String getFullName() {
            return version + "-" + className;
        }
    }
}
