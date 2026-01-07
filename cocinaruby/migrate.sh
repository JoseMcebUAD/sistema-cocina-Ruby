#!/bin/bash
# Script para ejecutar migraciones en Linux/Mac

if [ "$#" -ne 1 ]; then
    echo "Uso: ./migrate.sh [comando]"
    echo "Intenta con: ./migrate.sh help"
    exit 1
fi

if [ "$1" = "help" ]; then
    echo ""
    echo "=== COMANDOS DE MIGRACION ==="
    echo ""
    echo "up              - Ejecuta las migraciones pendientes"
    echo "down            - Revierte el último batch de migraciones"
    echo "reup            - Recrea todas las migraciones (DOWN + UP)"
    echo "reup-with-data  - Recrea migraciones preservando datos"
    echo "status          - Muestra el estado de las migraciones"
    echo "help            - Muestra esta ayuda"
    echo ""
    exit 0
fi

if [ "$1" = "up" ]; then
    echo "Ejecutando migraciones UP..."
    mvn compile exec:java -Dexec.mainClass="com.Database.MigrationRunner" -Dexec.args="up"
elif [ "$1" = "down" ]; then
    echo "Revirtiendo migraciones DOWN..."
    mvn compile exec:java -Dexec.mainClass="com.Database.MigrationRunner" -Dexec.args="down"
elif [ "$1" = "reup" ]; then
    echo "Recreando migraciones (sin preservar datos)..."
    mvn compile exec:java -Dexec.mainClass="com.Database.MigrationRunner" -Dexec.args="reup"
elif [ "$1" = "reup-with-data" ]; then
    echo "Recreando migraciones (preservando datos)..."
    mvn compile exec:java -Dexec.mainClass="com.Database.MigrationRunner" -Dexec.args="reup-with-data"
elif [ "$1" = "status" ]; then
    echo "Consultando estado de migraciones..."
    mvn compile exec:java -Dexec.mainClass="com.Database.MigrationRunner" -Dexec.args="status"
else
    echo "Comando no reconocido: $1"
    echo "Usa: ./migrate.sh help"
    exit 1
fi
