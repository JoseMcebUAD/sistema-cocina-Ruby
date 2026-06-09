# Guía de Entorno de Desarrollo (Local)

## Pre-requisitos

| Herramienta | Versión mínima | Uso |
|---|---|---|
| JDK | 21 | Compilar y ejecutar la app (IntelliJ) |
| Docker Desktop | cualquier versión estable | Levantar MySQL y la app en contenedor |
| IntelliJ IDEA | Community o Ultimate | IDE principal |
| Maven | 3.9+ (incluido en el wrapper) | Build |

---

## Opción A — Docker (recomendado)

Levanta tanto la base de datos (MySQL 8.0 en puerto **3307**) como la app (puerto **8080**) en contenedores.

```bash
docker compose -f docker-compose.dev.yml up --build
```

Lo que hace internamente:

1. Construye la imagen desde `Dockerfile.dev` (Maven 3.9 + Temurin 21).
2. Descarga dependencias Maven y las cachea en el volumen `maven_cache` (solo la primera vez).
3. Espera a que MySQL esté saludable (`healthcheck`) antes de arrancar la app.
4. Ejecuta `mvn spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests`.

Parar y destruir los contenedores (los volúmenes persisten):

```bash
docker compose -f docker-compose.dev.yml down
```

Destruir también los volúmenes (base de datos incluida):

```bash
docker compose -f docker-compose.dev.yml down -v
```

> **Nota:** con Docker, la app se conecta a MySQL vía el hostname `db` (nombre del servicio). Si corres la app localmente (Opción B), la URL cambia a `localhost:3306`.

---

## Opción B — IntelliJ + MySQL local

### 1. Base de datos

Levanta solo el contenedor de MySQL (sin la app):

```bash
docker compose -f docker-compose.dev.yml up db
```

O usa tu instancia local de MySQL. La base de datos debe existir:

```sql
CREATE DATABASE cocina_rubi;
```

### 2. Perfil de Spring

El perfil `dev` carga `src/main/resources/application-dev.properties` con los siguientes valores ya configurados para local:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cocina_rubi
spring.datasource.username=root
spring.datasource.password=
jwt.secret=dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWNvY2luYS1yaWNreS1zeXN0ZW0=
```

> **Nunca subas credenciales reales al repositorio.** Estos valores son solo para desarrollo local.

### 3. Run/Debug Configuration en IntelliJ

1. Menú superior → los tres puntos junto a **Run/Debug** → **Edit Configurations**. o en la opcion CurrentFile ,Unamed, etc
2. Selecciona la configuración de la app (o crea una nueva de tipo **Spring Boot** o Application ).
3. Clic en **Modify options** → activa **Add VM options**.
4. En el campo **VM options** agrega:

```
-Dspring.profiles.active=dev
```

5. Clic en **Run** o **Debug**.

---

## Flyway

Las migraciones se ejecutan automáticamente al iniciar la app. El esquema actual está en la versión **1**:

| Versión | Archivo | Descripción |
|---|---|---|
| V1 | `V1__crear_esquema.sql` | 20 tablas |


Para agregar una migración nueva: crea `VN__descripcion.sql` en `src/main/resources/db/migration/`. Flyway la ejecuta al siguiente inicio.

---

## Comandos Maven útiles

```bash
# Compilar sin ejecutar tests
mvn clean package -DskipTests

# Ejecutar tests (excluye módulo de impresora)
mvn test -Dexcludes="**/printer/**"

# Ejecutar la app directamente con Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Puertos

| Servicio | Puerto local |
|---|---|
| API Spring Boot | 8080 |
| MySQL (Docker) | 3307 → 3306 interno |
| Swagger UI | http://localhost:8080/sistema-rubi-doc-api.html |
| Actuator | http://localhost:8080/actuator/health |
