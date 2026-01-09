package com.Database;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Database.MigrationLoader.MigrationFile;

public class SeederLoader {
    private static final Pattern SEEDER_COMPILATION = Pattern.compile("^(.+)\\.java");
    private String seedersPath;

    public SeederLoader(String seedersPath){
        this.seedersPath = seedersPath;
    }
    /**Carga todos los archivos del los seeders */
    public List<SeederFile> loadSeedersFile(){
        List<SeederFile> seedersFiles = new ArrayList<>();
        File seedersDir = new File(this.seedersPath);

        if(!seedersDir.exists() || !seedersDir.isDirectory()){
            System.err.println("El directorio de seeders no ha sido credado");
            return seedersFiles;
        }

        File[] files = seedersDir.listFiles((dir, name) -> name.endsWith(".java"));

        if (files == null || files.length == 0) {
            System.out.println("ℹ No se encontraron archivos de migración en: " + seedersDir);
            return seedersFiles;
        }

        for (File file : files) {
            SeederFile seederFile = parseSeederFile(file);
            if (seederFile != null) {
                seedersFiles.add(seederFile);
            }
        }

        return seedersFiles;

    }    
    /**Obtiene toda la informacion de los seeders */
    public SeederFile parseSeederFile(File file){
        String fileName = file.getName();
        Matcher mathcer = SEEDER_COMPILATION.matcher(fileName);

        if(mathcer.matches()){
            String className = mathcer.group(1);

            return new SeederFile(className, file);
        }else{
            System.out.println("No se ha encontrado el archivo: " + fileName);
            return null;
        }

    }

        /**
     * Carga dinámicamente una instancia de Seeder desde un archivo
     */
    public Seeder loadSeedersInstance(SeederFile seederFile) throws Exception {
        try {
            // Intentar cargar la clase desde el package com.Database.seeders
            String fullClassName = "com.Database.seeders." + seederFile.className;
            Class<?> seederClass = Class.forName(fullClassName);

            // Verificar que extiende de Seeder
            if (!Seeder.class.isAssignableFrom(seederClass)) {
                throw new Exception("La clase " + fullClassName + " no extiende de Seeder");
            }

            // Crear instancia
            Constructor<?> constructor = seederClass.getDeclaredConstructor();
            return (Seeder) constructor.newInstance();

        } catch (ClassNotFoundException e) {
            throw new Exception("No se pudo encontrar la clase compilada: com.Database.seeders." +
                              seederFile.className + "\n" +
                              "Asegúrate de compilar el proyecto primero: mvn compile", e);
        } catch (Exception e) {
            throw new Exception("Error al cargar el seeder " + seederFile.getFullName() + ": " +
                              e.getMessage(), e);
        }
    }


    /*clase para organizar los paths de los seeders */
    public static class SeederFile{
        public final String className;
        public final File file;

        public SeederFile(String className, File file){
            this.className = className;
            this.file = file;
        }

        public String getFullName(){
            return className;
        }

        @Override()
        public String toString(){
            return getFullName();
        }

    }



}
