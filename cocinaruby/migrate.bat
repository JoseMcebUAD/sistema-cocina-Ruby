@echo off
REM Script para ejecutar migraciones en Windows

if "%1"=="" (
    echo Uso: migrate.bat [comando]
    echo Intenta con: migrate.bat help
    exit /b 1
)

if "%1"=="help" goto help
if "%1"=="up" goto up
if "%1"=="down" goto down
if "%1"=="reup" goto reup
if "%1"=="reup-with-data" goto reup-with-data
if "%1"=="status" goto status
if "%1"=="make:migration" goto make_migration

echo Comando no reconocido: %1
echo Usa: migrate.bat help
exit /b 1

:help
echo.
echo === COMANDOS DE MIGRACION ===
echo.
echo up                       - Ejecuta las migraciones pendientes
echo down                     - Revierte el ultimo batch de migraciones
echo reup                     - Recrea todas las migraciones (DOWN + UP)
echo reup-with-data           - Recrea migraciones preservando datos
echo status                   - Muestra el estado de las migraciones
echo make:migration [nombre]  - Crea una nueva migracion
echo help                     - Muestra esta ayuda
echo.
exit /b 0

:up
echo Ejecutando migraciones UP...
call mvn clean compile
call mvn exec:java -Dexec.mainClass=com.Database.MigrationRunner -Dexec.args=up
exit /b %ERRORLEVEL%

:down
echo Revirtiendo migraciones DOWN...
call mvn clean compile
call mvn exec:java -Dexec.mainClass=com.Database.MigrationRunner -Dexec.args=down
exit /b %ERRORLEVEL%

:reup
echo Recreando migraciones (sin preservar datos)...
call mvn clean compile
call mvn exec:java -Dexec.mainClass=com.Database.MigrationRunner -Dexec.args=reup
exit /b %ERRORLEVEL%

:reup-with-data
echo Recreando migraciones (preservando datos)...
call mvn clean compile
call mvn exec:java -Dexec.mainClass=com.Database.MigrationRunner -Dexec.args=reup-with-data
exit /b %ERRORLEVEL%

:status
echo Consultando estado de migraciones...
call mvn clean compile
call mvn exec:java -Dexec.mainClass=com.Database.MigrationRunner -Dexec.args=status
exit /b %ERRORLEVEL%

:make_migration
if "%2"=="" (
    echo Error: Debes proporcionar un nombre para la migracion
    echo Uso: migrate.bat make:migration nombreMigracion
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

REM Obtener fecha actual en formato YYYYMMDD
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set "fecha=!datetime:~0,8!"

REM Crear nombre completo de la clase
set "nombreClase=!nombreCapitalizado!!fecha!"
set "nombreArchivo=!nombreClase!.java"
set "rutaMigraciones=src\main\java\com\Database\migrations"

echo Creando migracion: !nombreClase!

REM Crear archivo de migracion
(
echo package com.Database.migrations;
echo.
echo import com.Database.Migration;
echo.
echo public class !nombreClase! extends Migration{
echo.
echo     @Override
echo     public void up^(^) {
echo         // TODO Auto-generated method stub
echo         throw new UnsupportedOperationException^("Unimplemented method 'up'"^);
echo     }
echo.
echo     @Override
echo     public void down^(^) {
echo         // TODO Auto-generated method stub
echo         throw new UnsupportedOperationException^("Unimplemented method 'down'"^);
echo     }
echo.
echo }
) > "!rutaMigraciones!\!nombreArchivo!"

echo.
echo Migracion creada exitosamente: !rutaMigraciones!\!nombreArchivo!
endlocal
exit /b 0
