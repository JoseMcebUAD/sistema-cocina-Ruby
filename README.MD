# Cocina Ruby POS

Sistema de Punto de Venta (POS) para cocina economica desarrollado en Java.

## Descripcion

Cocina Ruby POS es una aplicacion de escritorio diseñada para gestionar las operaciones diarias de una cocina economica. Permite administrar ordenes de diferentes tipos, controlar mesas, gestionar clientes y generar tickets de venta.

### Caracteristicas principales

- **Gestion de ordenes**: Soporte para ordenes de mostrador, mesa y domicilio
- **Control de mesas**: Estados de mesa (disponible, ocupado, suspendido)
- **Gestion de clientes**: Registro y busqueda de clientes frecuentes
- **Sistema de ventas**: Historial de ventas con filtros por fecha, tipo de pago y cliente
- **Impresion de tickets**: Soporte para impresoras termicas ESC/POS
- **Autenticacion**: Sistema de usuarios con contraseñas encriptadas (BCrypt)
- **Sesiones persistentes**: Token de sesion para recordar usuario

## Tecnologias

| Tecnologia | Version | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje principal |
| JavaFX | 21 | Interfaz grafica |
| MariaDB | 10.x+ | Base de datos |
| HikariCP | 5.1.0 | Pool de conexiones |
| BCrypt | 0.10.2 | Encriptacion de contraseñas |
| ESC/POS Coffee | 4.1.0 | Impresion de tickets |
| Jackson | 2.17.0 | Serializacion JSON |
| Maven | 3.x | Gestion de dependencias |
| JUnit | 4.11 | Testing |

## Requisitos

### Software necesario

- **Java JDK 21** o superior
- **Maven 3.6+**
- **MariaDB 10.x** o MySQL 8.x
- (Opcional) Impresora termica compatible con ESC/POS

### Configuracion de base de datos

1. Instalar MariaDB/MySQL
2. Crear la base de datos (se crea automaticamente al ejecutar)
3. Configurar credenciales en `config.properties`

## Instalacion

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/sistema-cocina-ruby.git
cd sistema-cocina-ruby/cocinaruby
```

### 2. Configurar base de datos

Crear archivo `config.properties` en la raiz del proyecto `cocinaruby/`:

```properties
db.host=localhost
db.puerto=3306
db.nombre=sistema-cocina-ruby
db.usuario=root
db.contrasena=
```

### 3. Instalar dependencias

```bash
mvn clean install
```

### 4. Ejecutar migraciones

```bash
# Crear tablas
mvn exec:java@migrate-up

# (Opcional) Revertir migraciones
mvn exec:java@migrate-down
```

## Ejecucion

### Modo desarrollo

```bash
mvn javafx:run
```

### Generar el JAR
    Como JAVAFX no está disponible en tiempo de ejecución,
    es necesario descargar el SDK de JAVAFX en https://openjfx.io/

    Una vez hecho esto, generar el Jar con 
```bash
mvn clean package
```
El jar se genera en `/target`.

### Generar el exe
tener java jdk v21 en el path del sistema
```bash

jpackage ^
 --name CocinaRuby ^
 --input target ^
 --main-jar cocinaruby-1.0-SNAPSHOT.jar ^
 --main-class com.Launcher ^
 --type exe ^
 --java-options "--module-path C:\JAVASDK-21\openjfx-21.0.10_windows-x64_bin-sdk\javafx-sdk-21.0.10\lib --add-modules javafx.controls,javafx.fxml" ^
 --icon src\main\resources\icons\app.ico ^
 --win-console
 ```



## Estructura del proyecto

```
cocinaruby/
├── src/
│   ├── main/
│   │   ├── java/com/
│   │   │   ├── Config/         # Configuracion y conexion BD
│   │   │   ├── Controller/     # Controladores JavaFX
│   │   │   ├── DAO/            # Acceso a datos
│   │   │   ├── Database/       # Migraciones y seeders
│   │   │   ├── Model/          # Modelos de datos
│   │   │   ├── Service/        # Logica de negocio
│   │   │   └── View/           # Punto de entrada (Main)
│   │   └── resources/
│   │       ├── com/view/       # Archivos FXML
│   │       ├── css/            # Estilos
│   │       └── icons/          # Iconos
│   └── test/                   # Tests unitarios
├── config.properties           # Configuracion de BD
└── pom.xml                     # Dependencias Maven
```

## Comandos utiles

| Comando | Descripcion |
|---------|-------------|
| `mvn javafx:run` | Ejecutar aplicacion |
| `mvn clean install` | Compilar e instalar dependencias |
| `mvn test` | Ejecutar tests |
| `mvn exec:java@migrate-up` | Ejecutar migraciones |
| `mvn exec:java@migrate-down` | Revertir migraciones |
| `mvn jpackage:jpackage` | Generar ejecutable |

## Arquitectura

El proyecto sigue el patron **MVC** (Model-View-Controller):

- **Model**: Clases POJO que representan entidades (ModeloOrden, ModeloCliente, etc.)
- **View**: Archivos FXML con la interfaz grafica
- **Controller**: Controladores JavaFX que manejan la logica de UI
- **Service**: Capa de servicios con logica de negocio
- **DAO**: Capa de acceso a datos con patron Repository

### Patrones implementados

- **Strategy Pattern**: Para tipos de orden (mostrador, mesa, domicilio)
- **Singleton**: Para pool de conexiones (DatabasePool)
- **DAO Pattern**: Para acceso a base de datos
- **Dependency Injection**: Para testing de DAOs

## Licencia

Este proyecto es de uso privado.

---

Desarrollado para Cocina Ruby
