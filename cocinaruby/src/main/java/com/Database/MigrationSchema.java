package com.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Clase para crear y eliminar tablas en la base de datos
public class MigrationSchema {

    // Crea una nueva tabla en la base de datos
    public static void create(String tableName, TableCallback callback, Connection conn) throws SQLException {
        TableBuilder table = new TableBuilder(tableName);
        callback.build(table);

        String sql = table.toSQL();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Elimina una tabla si existe
    public static void dropIfExists(String tableName, Connection conn) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Constructor de tablas - permite definir columnas y restricciones
    public static class TableBuilder {
        private String tableName;
        private List<String> columns = new ArrayList<>();
        private List<String> constraints = new ArrayList<>();
        private String engine = "InnoDB";
        private String charset = "utf8mb4";

        public TableBuilder(String tableName) {
            this.tableName = tableName;
        }

        public TableBuilder(){
            
        }

        // Crea una columna ID autoincremental
        public TableBuilder id(String name) {
            columns.add("id_"+ name + " INT AUTO_INCREMENT PRIMARY KEY");
            return this;
        }

        // Crea una columna ID con un nombre id_ + el nombre de la tabla
        public TableBuilder id() {
            return id("id_" + tableName);
        }

        // Crea una columna de tipo entero
        public TableBuilder integer(String name) {
            columns.add(name + " INT");
            return this;
        }

        // Crea una columna de tipo entero grande
        public TableBuilder bigInteger(String name) {
            columns.add(name + " BIGINT");
            return this;
        }

        // Crea una columna de texto con longitud específica
        public TableBuilder string(String name, int length) {
            columns.add(name + " VARCHAR(" + length + ")");
            return this;
        }

        // Crea una columna de texto con longitud 255 por defecto
        public TableBuilder string(String name) {
            return string(name, 255);
        }

        // Crea una columna de texto largo
        public TableBuilder text(String name) {
            columns.add(name + " TEXT");
            return this;
        }

        // Crea una columna decimal con precisión y escala
        public TableBuilder decimal(String name, int precision, int scale) {
            columns.add(name + " DECIMAL(" + precision + "," + scale + ")");
            return this;
        }

        // Crea una columna booleana (0 o 1)
        public TableBuilder bool(String name) {
            columns.add(name + " TINYINT(1) DEFAULT 0");
            return this;
        }

        // Crea una columna de tipo fecha
        public TableBuilder date(String name) {
            columns.add(name + " DATE");
            return this;
        }

        // Crea una columna de fecha y hora
        public TableBuilder datetime(String name) {
            columns.add(name + " DATETIME");
            return this;
        }

        // Crea una columna timestamp
        public TableBuilder timestamp(String name) {
            columns.add(name + " TIMESTAMP NULL");
            return this;
        }

        // Agrega columnas created_at y updated_at automáticas
        public TableBuilder timestamps() {
            columns.add("created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            columns.add("updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            return this;
        }

        // Permite que la última columna agregada sea nula
        public TableBuilder nullable() {
            if (!columns.isEmpty()) {
                int lastIndex = columns.size() - 1;
                String lastColumn = columns.get(lastIndex);
                if (!lastColumn.contains("NULL")) {
                    columns.set(lastIndex, lastColumn + " NULL");
                }
            }
            return this;
        }

        // Define un valor por defecto para la última columna
        public TableBuilder defaultValue(String value) {
            if (!columns.isEmpty()) {
                int lastIndex = columns.size() - 1;
                String lastColumn = columns.get(lastIndex);
                columns.set(lastIndex, lastColumn + " DEFAULT " + value);
            }
            return this;
        }

        // Marca la última columna como única
        public TableBuilder unique() {
            if (!columns.isEmpty()) {
                int lastIndex = columns.size() - 1;
                String lastColumn = columns.get(lastIndex);
                columns.set(lastIndex, lastColumn + " UNIQUE");
            }
            return this;
        }

        // Marca la última columna como no nula
        public TableBuilder notNull() {
            if (!columns.isEmpty()) {
                int lastIndex = columns.size() - 1;
                String lastColumn = columns.get(lastIndex);
                columns.set(lastIndex, lastColumn + " NOT NULL");
            }
            return this;
        }

        // Crea una llave foránea
        public TableBuilder foreign(String column, String refTable, String refColumn) {
            constraints.add("FOREIGN KEY (" + column + ") REFERENCES " + refTable + "(" + refColumn + ")");
            return this;
        }

        // Crea una llave foránea que referencia a la columna "id"
        public TableBuilder foreign(String column, String refTable) {
            return foreign(column, refTable, "id");
        }

        // Al eliminar el registro padre, elimina los hijos
        public TableBuilder onDeleteCascade() {
            if (!constraints.isEmpty()) {
                int lastIndex = constraints.size() - 1;
                String lastConstraint = constraints.get(lastIndex);
                constraints.set(lastIndex, lastConstraint + " ON DELETE CASCADE");
            }
            return this;
        }

        // Al eliminar el registro padre, pone NULL en los hijos
        public TableBuilder onDeleteSetNull() {
            if (!constraints.isEmpty()) {
                int lastIndex = constraints.size() - 1;
                String lastConstraint = constraints.get(lastIndex);
                constraints.set(lastIndex, lastConstraint + " ON DELETE SET NULL");
            }
            return this;
        }

        // Crea un índice en las columnas especificadas
        public TableBuilder index(String... columns) {
            constraints.add("INDEX (" + String.join(", ", columns) + ")");
            return this;
        }

        // Define el motor de almacenamiento de MySQL
        public TableBuilder engine(String engine) {
            this.engine = engine;
            return this;
        }

        // Define el charset de la tabla
        public TableBuilder charset(String charset) {
            this.charset = charset;
            return this;
        }

        // Convierte la tabla a una sentencia SQL CREATE TABLE
        public String toSQL() {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ").append(tableName).append(" (\n");

            sql.append("  ").append(String.join(",\n  ", columns));

            if (!constraints.isEmpty()) {
                sql.append(",\n  ").append(String.join(",\n  ", constraints));
            }

            sql.append("\n) ENGINE=").append(engine)
               .append(" DEFAULT CHARSET=").append(charset);

            return sql.toString();
        }
    }

    // Interfaz funcional para construir tablas
    @FunctionalInterface
    public interface TableCallback {
        void build(TableBuilder table);
    }
}
