package com.Database;

import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tests para la clase Schema
 * Ejecutar con: mvn test -Dtest=SchemaTest
 */
public class SchemaTest extends BaseTest {

    @Test
    public void testCreateSimpleTable() throws SQLException {
        // Crear una tabla simple
        MigrationSchema.create("test_usuarios", table -> {
            table.id();
            table.string("nombre", 100).notNull();
            table.string("email").unique();
        }, connection);

        // Verificar que la tabla existe
        assertTrue("La tabla test_usuarios debe existir", tableExists("test_usuarios"));
    }

    @Test
    public void testDropTableIfExists() throws SQLException {
        // Crear tabla
        MigrationSchema.create("test_productos", table -> {
            table.id();
            table.string("nombre");
        }, connection);

        assertTrue("La tabla debe existir", tableExists("test_productos"));

        // Eliminar tabla
        MigrationSchema.dropIfExists("test_productos", connection);

        assertFalse("La tabla no debe existir", tableExists("test_productos"));
    }

    @Test
    public void testDropTableThatDoesNotExist() throws SQLException {
        // Eliminar tabla que no existe (no debe lanzar error)
        MigrationSchema.dropIfExists("tabla_inexistente", connection);

        // Si llegamos aquí, el test pasó
        assertTrue(true);
    }

    @Test
    public void testCreateTableWithAllColumnTypes() throws SQLException {
        MigrationSchema.create("test_categorias", table -> {
            table.id();
            table.string("nombre", 100);
            table.text("descripcion");
            table.integer("orden");
            table.bigInteger("contador");
            table.decimal("precio", 10, 2);
            table.bool("activo");
            table.date("fecha_inicio");
            table.datetime("fecha_registro");
            table.timestamp("ultima_actualizacion");
            table.timestamps();
        }, connection);

        assertTrue("La tabla debe existir", tableExists("test_categorias"));

        // Verificar algunas columnas
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("DESCRIBE test_categorias");
            boolean hasNombre = false;
            boolean hasDescripcion = false;
            boolean hasCreatedAt = false;

            while (rs.next()) {
                String columnName = rs.getString("Field");
                if (columnName.equals("nombre")) hasNombre = true;
                if (columnName.equals("descripcion")) hasDescripcion = true;
                if (columnName.equals("created_at")) hasCreatedAt = true;
            }

            assertTrue("Debe tener columna nombre", hasNombre);
            assertTrue("Debe tener columna descripcion", hasDescripcion);
            assertTrue("Debe tener columna created_at", hasCreatedAt);
        }
    }

    @Test
    public void testCreateTableWithModifiers() throws SQLException {
        MigrationSchema.create("test_ventas", table -> {
            table.id();
            table.string("codigo").notNull().unique();
            table.decimal("total", 10, 2).notNull();
            table.string("descripcion").nullable();
            table.integer("cantidad").defaultValue("1");
            table.bool("pagado").defaultValue("0");
        }, connection);

        assertTrue("La tabla debe existir", tableExists("test_ventas"));
    }

    @Test(expected = SQLException.class)
    public void testCreateTableThatAlreadyExists() throws SQLException {
        // Crear tabla
        MigrationSchema.create("test_productos", table -> {
            table.id();
            table.string("nombre");
        }, connection);

        // Intentar crear la misma tabla debe fallar
        MigrationSchema.create("test_productos", table -> {
            table.id();
            table.string("nombre");
        }, connection);
    }

    @Test
    public void testInsertDataAfterCreation() throws SQLException {
        // Crear tabla
        MigrationSchema.create("test_usuarios", table -> {
            table.id();
            table.string("nombre", 100).notNull();
            table.string("email").notNull();
        }, connection);

        // Insertar datos
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO test_usuarios (nombre, email) VALUES ('Juan', 'juan@test.com')");
        }

        // Verificar que se insertó
        assertEquals("Debe haber 1 fila", 1, countRows("test_usuarios"));
    }
}
