# Glosario de Patrones — Spring Boot API

Referencia rápida de reglas, anotaciones, imports y convenciones usadas en este proyecto.
Aplica estos patrones idénticos en proyectos futuros.

---

## 1. Entidades JPA

### Imports obligatorios

```java
import jakarta.persistence.*;                                  // @Entity, @Table, @Id, @Column, @OneToMany…
import lombok.*;                                               // @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor
import com.fasterxml.jackson.annotation.JsonIgnore;           // Cortar ciclos bidireccionales en serialización
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Evitar errores con proxies Hibernate lazy
import org.hibernate.annotations.Immutable;                   // Solo para entidades mapeadas a vistas SQL
```

### Anotaciones de clase y columna

| Elemento        | Regla                                                                     |
|-----------------|---------------------------------------------------------------------------|
| Clase entidad   | `@Entity` + `@Table(name = "nombre_snake_case")`                         |
| PK simple       | `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`            |
| PK compartida   | `@Id` sin `@GeneratedValue` + `@MapsId` en la relación `@OneToOne`      |
| Columna         | `@Column(name = "nombre_snake_case", nullable = false)`                  |
| Enum            | `@Enumerated(EnumType.STRING)` — siempre STRING, nunca ORDINAL           |
| Definición enum | Inner class estática dentro de la entidad que lo usa                     |
| Fecha/hora      | `LocalDateTime` — nunca `java.util.Date`                                 |
| Decimal/precio  | `BigDecimal` en Java, `DECIMAL(10,2)` en MySQL                           |
| Booleano        | `boolean` primitivo, `TINYINT(1) DEFAULT 1` en MySQL                    |
| Auto-fecha      | `@Column(updatable = false)` + valor en migración SQL si aplica          |

### Reglas de relaciones

| Relación               | Regla                                                                              |
|------------------------|------------------------------------------------------------------------------------|
| `@OneToMany`           | `FetchType.LAZY` + `CascadeType.ALL` + `orphanRemoval = true`                    |
| `@ManyToOne`           | `FetchType.LAZY` + `@JoinColumn(name = "id_xxx")`                                |
| `@OneToOne` (padre)    | `cascade = CascadeType.ALL`, `mappedBy = "campo"`, `FetchType.LAZY`              |
| `@OneToOne` (hijo)     | `@MapsId` + `@JoinColumn(name = "id_padre")` — PK compartida con el padre        |
| Excepción `EAGER`      | Solo cuando el objeto hijo siempre se necesita junto al padre (ej: Inventario)    |

### Serialización JSON — evitar ciclos y errores lazy

```java
// En la entidad HIJA: cortar el ciclo bidireccional
@JsonIgnore
private Pedido pedido;

// En entidades con proxies Hibernate que se serialicen directamente:
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MiEntidad { ... }
```

### Patrón addXxx() — sincronización bidireccional

Cuando el padre maneja una colección, agregar un método helper para mantener ambos lados sincronizados:

```java
// En el Aggregate Root (ej: Pedido):
public void addItem(ComidaPedido item) {
    item.setPedido(this);
    this.items.add(item);
}
```

### Entidad mapeada a una vista SQL

```java
@Entity
@Table(name = "nombre_vista")
@Immutable                    // Hibernate: deshabilita INSERT / UPDATE / DELETE
public class VistaXxx {
    @Id
    private Integer id;
    // Solo @Getter — sin @Setter ni @Builder
}
```

### Enums como inner classes

```java
public class Comida {
    public enum Temporalidad { FIJO, TEMPORAL, ESPECIAL }
    public enum Estatus      { DISPONIBLE, NO_DISPONIBLE, AGOTADO }

    @Enumerated(EnumType.STRING)
    private Temporalidad temporalidad;

    @Enumerated(EnumType.STRING)
    private Estatus estatus;
}
```

---

## 2. Seguridad JWT + Spring Security

### Imports clave — SecurityConfig

```java
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.referrer.ReferrerPolicyHeaderWriter.ReferrerPolicy;
```

### Esqueleto SecurityConfig

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h
                .frameOptions(f -> f.deny())
                .referrerPolicy(r -> r.policy(ReferrerPolicy.NO_REFERRER)))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(entryPointNoAutorizado)
                .accessDeniedHandler(manejadorAccesoDenegado))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/ruta-a/**").hasAnyRole("ROL_A", "ROL_B")
                .requestMatchers("/ruta-b/**").hasRole("ROL_B")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://mi-dominio.com"));      // nunca "*" en prod
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));               // para token renovado
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### Tabla de reglas de seguridad

| Regla                       | Detalle                                                                  |
|-----------------------------|--------------------------------------------------------------------------|
| Sesión                      | `STATELESS` — sin HttpSession, JWT en cada request                      |
| CSRF                        | Deshabilitado en APIs REST (no hay formularios HTML con cookie de sesión)|
| Frame options               | `deny()` — previene clickjacking                                         |
| Referrer policy             | `NO_REFERRER` — no filtra URLs internas en cabeceras                    |
| Password encoding           | `BCryptPasswordEncoder` — nunca texto plano ni MD5                      |
| Autenticación               | Por PIN o password hasheado con BCrypt                                   |
| Token JWT                   | HMAC-SHA, expiración 3–5 horas, renovación sliding hasta 7 días         |
| Roles en JWT                | Claim `"roles"` como String, prefijo obligatorio `ROLE_`                |
| Token renovado              | Header `Authorization` en la RESPUESTA con el nuevo token               |
| API Key externa             | Header `X-API-Key` con comparación `MessageDigest.isEqual` (timing-safe)|
| Bloqueo de cuenta           | Campo `bloqueadoHasta` (LocalDateTime) + `intentosFallidos` (int)       |

### Usuario implementa UserDetails

```java
@Entity
public class Usuario implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rolUsuario.getRolUsuario()));
    }

    @Override
    public String getPassword() { return this.pin; }   // hash BCrypt

    @Override
    public boolean isAccountNonLocked() {
        if (bloqueadoHasta == null) return true;
        return LocalDateTime.now().isAfter(bloqueadoHasta);
    }

    @Override public boolean isEnabled()                 { return this.activo; }
    @Override public boolean isAccountNonExpired()       { return true; }
    @Override public boolean isCredentialsNonExpired()   { return true; }
}
```

### JwtAuthenticationFilter — flujo completo

```java
// Extiende OncePerRequestFilter
// 1. Leer header "Authorization: Bearer {token}"
// 2. extraerUsername(token) — funciona incluso con tokens expirados
// 3. Cargar UserDetails y verificar isEnabled() + isAccountNonLocked()
// 4. Si token vigente        → autenticar + renovar (sliding expiration)
// 5. Si expirado + en ventana → autenticar + renovar de todos modos
// 6. Si expirado fuera ventana → responder 401 (forzar re-login)
// Enviar token nuevo en header "Authorization" de la respuesta
```

### EntryPoint y AccessDeniedHandler

```java
// 401 — No autenticado
@Component
public class EntryPointNoAutorizado implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         AuthenticationException ex) throws IOException {
        res.setStatus(401);
        res.setContentType("application/json");
        res.getWriter().write(objectMapper.writeValueAsString(
            ApiResponse.error(401, "No autorizado")));
    }
}

// 403 — Autenticado pero sin permiso
@Component
public class ManejadorAccesoDenegado implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        res.setStatus(403);
        res.setContentType("application/json");
        res.getWriter().write(objectMapper.writeValueAsString(
            ApiResponse.error(403, "Acceso denegado")));
    }
}
```

---

## 3. Migraciones Flyway

### Estructura de carpetas

```
src/main/resources/db/migration/
├── V1__crear_esquema.sql           ← Esquema completo inicial (todas las tablas)
├── V2__vista_resumen.sql           ← Vistas SQL
├── V3__datos_semilla.sql           ← Datos iniciales (usuarios, catálogos)
└── V{N}__descripcion_corta.sql     ← Un cambio por archivo, N secuencial
```

### Reglas de nomenclatura y contenido

| Regla                     | Detalle                                                                |
|---------------------------|------------------------------------------------------------------------|
| Nombre del archivo        | `V{N}__{descripcion_snake_case}.sql` — doble guion bajo obligatorio   |
| Numeración                | Secuencial sin saltar (V1, V2, V3…). No renombrar archivos ya aplicados|
| Idempotencia tablas       | `CREATE TABLE IF NOT EXISTS`                                           |
| Columna nueva             | `ALTER TABLE x ADD COLUMN nombre tipo restricciones`                   |
| Columna modificada        | `ALTER TABLE x MODIFY COLUMN nombre tipo nuevas_restricciones`         |
| FKs seguras               | `ON DELETE SET NULL` — permite borrar entidades sin romper pedidos     |
| Vistas actualizables      | `CREATE OR REPLACE VIEW` — se puede cambiar sin crear V nueva          |
| Datos semilla             | `INSERT INTO` o `INSERT IGNORE INTO` para idempotencia                 |
| Baseline DB existente     | `spring.flyway.baseline-on-migrate=true` en properties                 |

### Tipos MySQL usados en este proyecto

```sql
INT AUTO_INCREMENT PRIMARY KEY      -- IDs enteros
VARCHAR(100) NOT NULL               -- textos cortos obligatorios
VARCHAR(250) NULL                   -- textos opcionales (comentarios)
DECIMAL(10,2)                       -- precios y cantidades monetarias
TINYINT(1) DEFAULT 1               -- booleanos (activo, impreso)
DATETIME NULL                       -- LocalDateTime en Java
TINYINT DEFAULT 1                   -- contadores pequeños (cantidad)
```

### FK con borrado seguro (patrón del proyecto)

```sql
-- Permite eliminar la entidad referenciada sin borrar el pedido
ALTER TABLE tabla_detalle
    ADD CONSTRAINT fk_detalle_padre
    FOREIGN KEY (id_padre) REFERENCES tabla_padre(id_padre)
    ON DELETE SET NULL;
```

### V1 — esquema base mínimo

```sql
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario   VARCHAR(50) NOT NULL,
    pin              VARCHAR(255) NOT NULL,
    activo           TINYINT(1) DEFAULT 1,
    intentos_fallidos INT DEFAULT 0,
    bloqueado_hasta  DATETIME NULL
);

CREATE TABLE IF NOT EXISTS rol_usuario (
    id_usuario  INT PRIMARY KEY,                  -- PK compartida con usuario
    rol_usuario VARCHAR(50) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);
```

---

## 4. Docker

### Dockerfile Multi-stage (patrón del proyecto)

```dockerfile
# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cachear dependencias en capa separada (solo se re-descarga si cambia pom.xml)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine         # imagen mínima, sin Maven ni fuentes
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Variables de entorno requeridas en runtime

```bash
DATABASE_URL=jdbc:mysql://host:3306/nombre_db
DB_USERNAME=usuario_db
DB_PASSWORD=contraseña_db
JWT_SECRET=base64_secret_minimo_256_bits
# Opcional si se usa integración con n8n u otro sistema externo:
N8N_API_KEY=clave_api_interna
```

### Reglas Docker

| Regla                    | Detalle                                                              |
|--------------------------|----------------------------------------------------------------------|
| Multi-stage              | Build separado del runtime — imagen final sin Maven ni código fuente |
| JRE, no JDK              | `eclipse-temurin:21-jre-alpine` en producción (imagen más ligera)   |
| `-DskipTests`            | Los tests no corren durante el build de la imagen                    |
| Variables sensibles      | Nunca hardcodeadas en Dockerfile — siempre variables de entorno      |
| Puerto                   | `EXPOSE 8080` es informativo; mapearlo en el orquestador (Dokploy)  |
| Caché de dependencias    | `COPY pom.xml` + `dependency:go-offline` antes de copiar `src/`     |

---

## 5. Application Properties

### Estructura de profiles

```
src/main/resources/
├── application.properties           ← Base: variables de entorno, sin valores sensibles
├── application-dev.properties       ← Valores locales (NO commitear secrets reales)
└── application-test.properties      ← DB de test, mismo JWT secret que dev
```

El profile activo se elige con:
- Variable de entorno: `SPRING_PROFILES_ACTIVE=dev`
- En tests: `src/test/resources/application.properties` con `spring.profiles.active=test`

### application.properties (base — producción)

```properties
spring.application.name=nombre-app

# Datasource — siempre por env var
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA — naming explícito (respeta @Column exacto, sin conversión automática)
spring.jpa.hibernate.naming.physical-strategy=\
  org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.hibernate.ddl-auto=none

# Flyway — migraciones automáticas al arrancar
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JWT
jwt.secret=${JWT_SECRET}

# Swagger / OpenAPI
springdoc.swagger-ui.path=/mi-api-doc.html

# Actuator
management.endpoints.web.exposure.include=health
```

### application-dev.properties (desarrollo local)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nombre_db_dev
spring.datasource.username=root
spring.datasource.password=

jwt.secret=base64_secret_para_desarrollo_local

# Más detalle en logs de SQL (solo desarrollo)
# logging.level.org.hibernate.SQL=DEBUG
# logging.level.org.hibernate.type.descriptor.sql=TRACE
```

### application-test.properties (tests de integración)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nombre_db_test
spring.datasource.username=root
spring.datasource.password=

# MISMO secret que dev para que tokens JWT hardcodeados en tests funcionen
jwt.secret=base64_secret_para_desarrollo_local

# Permitir limpiar entre ejecuciones de test
spring.flyway.clean-disabled=false
```

### Regla crítica — Naming Strategy

> Usar siempre `PhysicalNamingStrategyStandardImpl` cuando las columnas JPA tienen
> nombres explícitos en `@Column(name = "...")`. Sin esto, Hibernate convierte
> `nombreComida` → `nombre_comida` automáticamente y puede ignorar el `@Column`.

---

## Resumen rápido — Checklist para proyecto nuevo

```
Entidades:
  [ ] @Entity + @Table(name = "snake_case")
  [ ] PK con @GeneratedValue(IDENTITY) o @MapsId para PK compartida
  [ ] @Column(name = "snake_case") en todos los campos
  [ ] @Enumerated(EnumType.STRING) en todos los enums
  [ ] Enums definidos como inner classes
  [ ] @OneToMany siempre LAZY + CascadeType.ALL + orphanRemoval = true
  [ ] @ManyToOne siempre LAZY
  [ ] @JsonIgnore en el lado "muchos" para cortar ciclos
  [ ] @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) si se serializa con lazy

Seguridad:
  [ ] SessionCreationPolicy.STATELESS
  [ ] CSRF deshabilitado
  [ ] BCryptPasswordEncoder como bean
  [ ] Usuario implementa UserDetails con isAccountNonLocked()
  [ ] JwtAuthenticationFilter extiende OncePerRequestFilter
  [ ] EntryPoint 401 y AccessDeniedHandler 403 devuelven JSON (no HTML)
  [ ] CORS con orígenes explícitos (no "*" en producción)
  [ ] Header Authorization expuesto en CORS para token renovado

Flyway:
  [ ] V1__crear_esquema.sql con IF NOT EXISTS
  [ ] V3__datos_semilla.sql con datos base
  [ ] FKs con ON DELETE SET NULL donde aplique
  [ ] spring.flyway.baseline-on-migrate=true en properties
  [ ] spring.flyway.clean-disabled=false en test properties

Docker:
  [ ] Multi-stage: build con Maven, runtime con JRE-alpine
  [ ] COPY pom.xml antes de COPY src/ (cachear dependencias)
  [ ] RUN mvn clean package -DskipTests
  [ ] Secrets solo por env var, nunca hardcodeados

Properties:
  [ ] application.properties solo con ${ENV_VAR}
  [ ] application-dev.properties con valores locales (en .gitignore si tiene secrets)
  [ ] application-test.properties con DB de test y mismo JWT_SECRET que dev
  [ ] PhysicalNamingStrategyStandardImpl activo
  [ ] ddl-auto=none (Flyway maneja el esquema)
```
