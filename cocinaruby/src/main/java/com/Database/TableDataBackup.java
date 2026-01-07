package com.Database;

import java.sql.*;
import java.util.*;

/**
 * Maneja el respaldo y restauración de datos de tablas
 */
public class TableDataBackup {

    private Connection connection;

    public TableDataBackup(Connection connection) {
        this.connection = connection;
    }

    /**
     * Respalda todos los datos de una tabla
     */
    public TableBackup backupTable(String tableName) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        // Obtener metadatos de la tabla
        String sql = "SELECT * FROM " + tableName;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Obtener nombres de columnas
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnName(i));
            }

            // Leer todas las filas
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (String column : columns) {
                    row.put(column, rs.getObject(column));
                }
                rows.add(row);
            }
        }

        return new TableBackup(tableName, columns, rows);
    }

    /**
     * Restaura los datos en una tabla
     * Ignora columnas que no existen y filas que causan errores
     */
    public RestoreResult restoreTable(TableBackup backup) throws SQLException {
        int totalRows = backup.rows.size();
        int insertedRows = 0;
        int skippedRows = 0;
        List<String> errors = new ArrayList<>();

        if (totalRows == 0) {
            return new RestoreResult(0, 0, errors);
        }

        // Obtener columnas actuales de la tabla
        Set<String> currentColumns = getCurrentTableColumns(backup.tableName);

        // Filtrar columnas que existen en la tabla actual
        List<String> validColumns = new ArrayList<>();
        for (String column : backup.columns) {
            if (currentColumns.contains(column.toLowerCase())) {
                validColumns.add(column);
            }
        }

        if (validColumns.isEmpty()) {
            errors.add("⚠ No hay columnas compatibles para restaurar en la tabla " + backup.tableName);
            return new RestoreResult(0, totalRows, errors);
        }

        // Construir SQL de inserción
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
        sqlBuilder.append(backup.tableName).append(" (");
        sqlBuilder.append(String.join(", ", validColumns));
        sqlBuilder.append(") VALUES (");
        sqlBuilder.append(String.join(", ", Collections.nCopies(validColumns.size(), "?")));
        sqlBuilder.append(")");

        String insertSql = sqlBuilder.toString();

        // Insertar fila por fila
        for (Map<String, Object> row : backup.rows) {
            try {
                insertRow(insertSql, validColumns, row);
                insertedRows++;
            } catch (SQLException e) {
                skippedRows++;
                String errorMsg = "Error al insertar fila en " + backup.tableName + ": " + e.getMessage();
                errors.add(errorMsg);
                // Continuar con la siguiente fila
            }
        }

        return new RestoreResult(insertedRows, skippedRows, errors);
    }

    /**
     * Inserta una fila en la tabla
     */
    private void insertRow(String sql, List<String> columns, Map<String, Object> row) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < columns.size(); i++) {
                Object value = row.get(columns.get(i));
                pstmt.setObject(i + 1, value);
            }
            pstmt.executeUpdate();
        }
    }

    /**
     * Obtiene las columnas actuales de una tabla
     */
    private Set<String> getCurrentTableColumns(String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }

        return columns;
    }

    /**
     * Obtiene todas las tablas de la base de datos (excepto migrations)
     */
    public List<String> getAllTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (!tableName.equalsIgnoreCase("migrations")) {
                    tables.add(tableName);
                }
            }
        }

        return tables;
    }

    /**
     * Clase que almacena el respaldo de una tabla
     */
    public static class TableBackup {
        public final String tableName;
        public final List<String> columns;
        public final List<Map<String, Object>> rows;

        public TableBackup(String tableName, List<String> columns, List<Map<String, Object>> rows) {
            this.tableName = tableName;
            this.columns = columns;
            this.rows = rows;
        }
    }

    /**
     * Resultado de la restauración
     */
    public static class RestoreResult {
        public final int insertedRows;
        public final int skippedRows;
        public final List<String> errors;

        public RestoreResult(int insertedRows, int skippedRows, List<String> errors) {
            this.insertedRows = insertedRows;
            this.skippedRows = skippedRows;
            this.errors = errors;
        }
    }
}
