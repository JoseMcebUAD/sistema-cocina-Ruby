# Guía de implementación de Redis Cache

Todos los cambios son en `src/main/java/com/cocinarubi/` salvo que se indique otra ruta.

---

## 1. Dependencia en `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

---

## 2. Variables de conexión en `application-dev.properties`

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Para producción, usa variables de entorno con `.env` (ya configurado en el proyecto).

---

## 3. Redis en Docker (`docker-compose.yml`)

Agrega el servicio al compose existente:

```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  restart: unless-stopped
```

---

## 4. Clase de configuración — `config/RedisCacheConfig.java` (nueva)

```java
package com.cocinarubi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Configura Redis como proveedor de caché con TTL diferenciado por nombre de caché.
 * Capa: Config — infraestructura de caché.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configs = Map.of(
            "menu-web",      defaults.entryTtl(Duration.ofMinutes(3)),
            "complementos",  defaults.entryTtl(Duration.ofMinutes(5)),
            "rutas",         defaults.entryTtl(Duration.ofMinutes(10)),
            "imagenes",      defaults.entryTtl(Duration.ofMinutes(30))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults.entryTtl(Duration.ofMinutes(5)))
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
```

---

## 5. `@Cacheable` — dónde agregar

### 5.1 Menú web — `domain/service/MenuWebService.java`

```java
@Override
@Cacheable(value = "menu-web", key = "'all'")
public MenuWebResponseDTO getMenu() { ... }
```

### 5.2 Complementos — `domain/service/ComplementoService.java`

```java
@Cacheable(value = "complementos", key = "'disponibles'")
public List<ComplementoResponseDTO> findDisponibles() { ... }
```

### 5.3 Rutas — `domain/service/RutaService.java` (o el nombre que tenga)

```java
@Cacheable(value = "rutas", key = "'activas'")
public List<RutaResponseDTO> findActivas() { ... }
```

### 5.4 Imágenes — `domain/service/files/ArchivoService` (o el servicio que maneje URLs)

Cachear el método que retorna la lista de URLs de un módulo:
```java
@Cacheable(value = "imagenes", key = "#modulo + '-' + #id")
public List<ArchivoResponseDTO> listarPorModulo(String modulo, Integer id) { ... }
```

---

## 6. `@CacheEvict` — dónde agregar (invalidación en mutaciones)

Agrega en los métodos `save`, `update`, `delete` de cada servicio.
`allEntries = true` limpia todo el caché de esa clave; útil cuando el menú
es un objeto único agregado.

### 6.1 `MenuWebService` — se invierte: el evict va en los servicios que lo alimentan

| Servicio que muta | Método | Anotación |
|---|---|---|
| `ComidaService` | `save`, `update`, `delete` | `@CacheEvict(value = "menu-web", allEntries = true)` |
| `BasicoService` | `save`, `update`, `delete` | `@CacheEvict(value = "menu-web", allEntries = true)` |
| `DesayunoService` | `save`, `update`, `delete` | `@CacheEvict(value = "menu-web", allEntries = true)` |
| `PaqueteService` | `save`, `update`, `delete` | `@CacheEvict(value = "menu-web", allEntries = true)` |
| `ProductoCocinaService` | `save`, `update`, `delete` | `@CacheEvict(value = "menu-web", allEntries = true)` |
| `CategoriaService` | `save`, `update`, `delete` | `@CacheEvict(value = "menu-web", allEntries = true)` |

### 6.2 Complementos

| Servicio | Método | Anotación |
|---|---|---|
| `ComplementoService` | `save`, `update`, `delete` | `@CacheEvict(value = "complementos", allEntries = true)` |
| `BasicoService` | `save`, `update`, `delete` | `@CacheEvict(value = "complementos", allEntries = true)` |

### 6.3 Rutas

| Servicio | Método | Anotación |
|---|---|---|
| `RutaService` | `save`, `update`, `delete` | `@CacheEvict(value = "rutas", allEntries = true)` |

### 6.4 Imágenes

| Servicio | Método | Anotación |
|---|---|---|
| Servicio de archivos | `upload`, `delete` | `@CacheEvict(value = "imagenes", allEntries = true)` |

---

## 7. Serialización — qué deben tener los DTOs

Redis serializa los objetos a JSON con `GenericJackson2JsonRedisSerializer`.
Para deserializar correctamente, **cada DTO que entre al caché debe tener constructor sin argumentos**.

Los DTOs de este proyecto ya cumplen (tienen `public DTO() {}`).

Si en el futuro se usa Lombok `@Value` (inmutable) o `@Builder` sin `@NoArgsConstructor`,
la deserialización fallará con `InvalidDefinitionException`.

---

## 8. Resumen de archivos a tocar

| Archivo | Cambio |
|---|---|
| `pom.xml` | Añadir dependencia Redis |
| `src/main/resources/application-dev.properties` | Host y puerto Redis |
| `docker-compose.yml` | Servicio redis |
| `config/RedisCacheConfig.java` | **Crear** — bean CacheManager con TTLs |
| `domain/service/MenuWebService.java` | `@Cacheable("menu-web")` en `getMenu()` |
| `domain/service/ComplementoService.java` | `@Cacheable` + `@CacheEvict` |
| `domain/service/RutaService.java` | `@Cacheable` + `@CacheEvict` |
| Servicio de archivos | `@Cacheable` + `@CacheEvict` |
| `ComidaService`, `BasicoService`, `DesayunoService`, `PaqueteService`, `ProductoCocinaService`, `CategoriaService` | `@CacheEvict("menu-web")` en mutaciones |
