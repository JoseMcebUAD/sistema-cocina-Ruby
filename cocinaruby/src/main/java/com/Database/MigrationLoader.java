package com.Database;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carga y ordena archivos de migración desde el directorio de migraciones
 * Formato esperado: NombreClaseYYYYMMDD.java
 */
public class MigrationLoader {

    private static final Pattern MIGRATION_PATTERN = Pattern.compile("^(.+)(\\d{8})\\.java$");
    private String migrationsPath;

    public MigrationLoader(String migrationsPath) {
        this.migrationsPath = migrationsPath;
    }

    /**
     * Carga todas las migraciones del directorio, ordenadas por fecha
     */
    public List<MigrationFile> loadMigrationFiles() {
        List<MigrationFile> migrations = new ArrayList<>();
        File migrationsDir = new File(migrationsPath);

        if (!migrationsDir.exists() || !migrationsDir.isDirectory()) {
            System.err.println("⚠ Directorio de migraciones no encontrado: " + migrationsPath);
            return migrations;
        }

        File[] files = migrationsDir.listFiles((dir, name) -> name.endsWith(".java"));

        if (files == null || files.length == 0) {
            System.out.println("ℹ No se encontraron archivos de migración en: " + migrationsPath);
            return migrations;
        }

        for (File file : files) {
            MigrationFile migrationFile = parseMigrationFile(file);
            if (migrationFile != null) {
                migrations.add(migrationFile);
            }
        }

        // Ordenar por versión (fecha)
        Collections.sort(migrations, (m1, m2) -> m1.version.compareTo(m2.version));

        return migrations;
    }

    /**
     * Parsea un archivo de migración y extrae su información
     */
    private MigrationFile parseMigrationFile(File file) {
        String fileName = file.getName();
        Matcher matcher = MIGRATION_PATTERN.matcher(fileName);

        if (matcher.matches()) {
            String className = matcher.group(1);  // El nombre viene primero
            String version = matcher.group(2);    // La fecha viene al final

            return new MigrationFile(version, className, file);
        } else {
            System.err.println("⚠ Archivo ignorado (formato inválido): " + fileName);
            System.err.println("  Formato esperado: NombreClaseYYYYMMDD.java");
            return null;
        }
    }

    /**
     * Carga dinámicamente una instancia de Migration desde un archivo
     */
    public Migration loadMigrationInstance(MigrationFile migrationFile) throws Exception {
        try {
            // Intentar cargar la clase desde el package com.Database.migrations
            String fullClassName = "com.Database.migrations." + migrationFile.className + migrationFile.version;
            Class<?> migrationClass = Class.forName(fullClassName);

            // Verificar que extiende de Migration
            if (!Migration.class.isAssignableFrom(migrationClass)) {
                throw new Exception("La clase " + fullClassName + " no extiende de Migration");
            }

            // Crear instancia
            Constructor<?> constructor = migrationClass.getDeclaredConstructor();
            return (Migration) constructor.newInstance();

        } catch (ClassNotFoundException e) {
            throw new Exception("No se pudo encontrar la clase compilada: com.Database.migrations." +
                              migrationFile.className + "\n" +
                              "Asegúrate de compilar el proyecto primero: mvn compile", e);
        } catch (Exception e) {
            throw new Exception("Error al cargar la migración " + migrationFile.getFullName() + ": " +
                              e.getMessage(), e);
        }
    }

    /**
     * Clase que representa un archivo de migración
     */
    public static class MigrationFile {
        public final String version;
        public final String className;
        public final File file;

        public MigrationFile(String version, String className, File file) {
            this.version = version;
            this.className = className;
            this.file = file;
        }

        public String getFullName() {
            return className + version; 
        }

        @Override
        public String toString() {
            return getFullName();
        }
    }
}
