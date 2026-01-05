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

echo Comando no reconocido: %1
echo Usa: migrate.bat help
exit /b 1

:help
echo.
echo === COMANDOS DE MIGRACION ===
echo.
echo up              - Ejecuta las migraciones pendientes
echo down            - Revierte el ultimo batch de migraciones
echo reup            - Recrea todas las migraciones (DOWN + UP)
echo reup-with-data  - Recrea migraciones preservando datos
echo status          - Muestra el estado de las migraciones
echo help            - Muestra esta ayuda
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
