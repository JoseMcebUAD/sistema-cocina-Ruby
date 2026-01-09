@echo off
REM Script para ejecutar seeders en Windows

if "%1"=="" (
    echo Uso: seeder.bat [comando]
    echo Intenta con: seeder.bat help
    exit /b 1
)

if "%1"=="help" goto help
if "%1"=="seeder:up" goto seeder_up
if "%1"=="seeder:up-all" goto seeder_up_all
if "%1"=="make:seeder" goto make_seeder

echo Comando no reconocido: %1
echo Usa: seeder.bat help
exit /b 1

:help
echo.
echo === COMANDOS DE SEEDERS ===
echo.
echo seeder:up [NombreSeeder] - Ejecuta un seeder especifico
echo seeder:up-all            - Ejecuta todos los seeders disponibles
echo make:seeder [nombre]     - Crea un nuevo seeder
echo help                     - Muestra esta ayuda
echo.
exit /b 0

:seeder_up
if "%2"=="" (
    echo Error: Debes proporcionar el nombre del seeder
    echo Uso: seeder.bat seeder:up NombreSeeder
    exit /b 1
)
echo Ejecutando seeder: %2...
call mvn clean compile
call mvn exec:java -Dexec.mainClass=com.Database.SeederRunner -Dexec.args="seeder:up %2"
exit /b %ERRORLEVEL%

:seeder_up_all
echo Ejecutando todos los seeders...
call mvn clean compile
call mvn exec:java -Dexec.mainClass=com.Database.SeederRunner -Dexec.args=seeder:up-all
exit /b %ERRORLEVEL%

:make_seeder
if "%2"=="" (
    echo Error: Debes proporcionar un nombre para el seeder
    echo Uso: seeder.bat make:seeder nombreSeeder
    exit /b 1
)

REM Capitalizar primera letra
setlocal EnableDelayedExpansion
set "nombre=%~2"
set "primera=!nombre:~0,1!"
set "resto=!nombre:~1!"

REM Convertir primera letra a mayuscula
for %%A in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
    set "primera=!primera:%%A=%%A!"
)
for %%A in (a b c d e f g h i j k l m n o p q r s t u v w x y z) do (
    for %%B in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do (
        if /i "%%A"=="%%B" set "primera=!primera:%%A=%%B!"
    )
)

set "nombreCapitalizado=!primera!!resto!"
set "nombreClase=!nombreCapitalizado!Seeder"
set "nombreArchivo=!nombreClase!.java"
set "rutaSeeders=src\main\java\com\Database\seeders"

echo Creando seeder: !nombreClase!

REM Crear directorio si no existe
if not exist "!rutaSeeders!" (
    mkdir "!rutaSeeders!"
)

REM Crear archivo de seeder
(
echo package com.Database.seeders;
echo.
echo import com.Database.Seeder;
echo import java.sql.PreparedStatement;
echo import java.sql.SQLException;
echo.
echo public class !nombreClase! extends Seeder {
echo.
echo     @Override
echo     public void run^(^) {
echo         System.out.println^("Ejecutando seeder: !nombreClase!..."^);
echo.
echo         try {
echo             // TODO: Implementar la lógica del seeder aquí
echo             // Ejemplo:
echo             // String sql = "INSERT INTO tabla ^(campo1, campo2^) VALUES ^(?, ?^)";
echo             // try ^(PreparedStatement ps = conexion.prepareStatement^(sql^)^) {
echo             //     ps.setString^(1, "valor1"^);
echo             //     ps.setString^(2, "valor2"^);
echo             //     ps.executeUpdate^(^);
echo             // }
echo.
echo             System.out.println^("✓ Datos insertados exitosamente"^);
echo.
echo         } catch ^(SQLException e^) {
echo             System.err.println^("Error al ejecutar seeder: " + e.getMessage^(^)^);
echo             throw new RuntimeException^(e^);
echo         }
echo     }
echo }
) > "!rutaSeeders!\!nombreArchivo!"

echo.
echo Seeder creado exitosamente: !rutaSeeders!\!nombreArchivo!
echo.
echo Para ejecutarlo usa:
echo   seeder.bat seeder:up !nombreClase!
echo.
echo O para ejecutar todos los seeders:
echo   seeder.bat seeder:up-all
endlocal
exit /b 0
