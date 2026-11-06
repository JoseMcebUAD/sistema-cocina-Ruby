# Skill: nueva-entidad

Scaffoldea una entidad completa siguiendo todas las convenciones del proyecto Sistema Cocina Rubi.

## Cuándo usar

Invoca este skill con `/nueva-entidad` cuando el usuario quiera crear una nueva entidad con su stack completo: migración Flyway, Entity JPA, Repository, DTOs, Service y Controller.

---

## Instrucciones para Claude

Al ser invocado, pregunta al usuario:

1. **Nombre de la entidad** (PascalCase, ej: `PromocionEspecial`)
2. **Nombre de la tabla SQL** (snake_case, ej: `promocion_especial`)
3. **Campos** de la tabla: nombre, tipo SQL y tipo Java para cada uno
4. **¿Tiene relaciones?** con otras entidades (ManyToOne, OneToMany, etc.)
5. **¿Requiere auditoría personalizada?** (descripción legible en `/auditoria`)
6. **¿Tiene campos sensibles?** que no deben aparecer en el snapshot de auditoría

Con esa información, crea los archivos en este orden:

---

### Paso 1 — Migración Flyway

Archivo: `src/main/resources/db/migration/V{siguiente}__crear_tabla_{nombre_tabla}.sql`

- Determina el número siguiente revisando los archivos existentes en `src/main/resources/db/migration/`
- Incluye `CREATE TABLE IF NOT EXISTS`, PK con `AUTO_INCREMENT`, `CONSTRAINT` para FK si aplica
- Agrega `COMMENT` a la tabla describiendo su propósito

```sql
CREATE TABLE IF NOT EXISTS nombre_tabla (
    id_nombre_entidad INT AUTO_INCREMENT PRIMARY KEY,
    -- campos...
    CONSTRAINT fk_nombre_entidad_otra_tabla
        FOREIGN KEY (id_otra_tabla) REFERENCES otra_tabla(id_otra_tabla)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Descripción de la tabla';
```

---

### Paso 2 — Entidad JPA

Archivo: `src/main/java/com/cocinarubi/domain/entity/NombreEntidad.java`

- Anotaciones Lombok: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`
- `@Entity @Table(name = "nombre_tabla") @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})`
- ID: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_nombre_entidad")`
- Relaciones `@ManyToOne` con `fetch = FetchType.LAZY` cuando aplique
- Javadoc de clase que describa su responsabilidad y relaciones

---

### Paso 3 — Repository

Archivo: `src/main/java/com/cocinarubi/dao/NombreEntidadRepository.java`

```java
public interface NombreEntidadRepository extends JpaRepository<NombreEntidad, Integer> {
    // Queries personalizados con @Query si el service los necesita
}
```

---

### Paso 4 — Request DTO

Archivo: `src/main/java/com/cocinarubi/presentation/dto/request/NombreEntidadRequestDTO.java`

- Constructor vacío + constructor completo
- Getters y setters manuales (sin Lombok en DTOs)
- Validaciones Jakarta: `@NotNull`, `@NotBlank`, `@Positive`, `@Size`, etc.
- `@JsonProperty("nombreCampo")` en cada campo

---

### Paso 5 — Response DTO

Archivo: `src/main/java/com/cocinarubi/presentation/dto/response/NombreEntidadResponseDTO.java`

- Constructor completo (record o clase plana)
- Solo campos que deben exponerse al cliente
- Si hay relaciones, incluir el ID y el nombre del objeto relacionado (no el objeto completo)

---

### Paso 6 — Service

Archivo: `src/main/java/com/cocinarubi/domain/service/NombreEntidadService.java`

Estructura base:

```java
/**
 * Gestiona [descripción de responsabilidad].
 * Capa: Service — lógica de negocio de [dominio].
 */
@Service
public class NombreEntidadService {

    private final NombreEntidadRepository nombreEntidadRepository;

    public NombreEntidadService(NombreEntidadRepository nombreEntidadRepository) {
        this.nombreEntidadRepository = nombreEntidadRepository;
    }

    @Transactional(readOnly = true)
    public List<NombreEntidadResponseDTO> findAll() { ... }

    @Transactional(readOnly = true)
    public NombreEntidadResponseDTO findById(int id) { ... }

    @Transactional
    public NombreEntidadResponseDTO save(NombreEntidadRequestDTO dto) { ... }

    @Transactional
    public NombreEntidadResponseDTO update(int id, NombreEntidadRequestDTO dto) { ... }

    public void delete(int id) { ... }

    private NombreEntidad findEntityById(int id) {
        return nombreEntidadRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                "NombreEntidad no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private NombreEntidadResponseDTO toResponseDTO(NombreEntidad entidad) { ... }
}
```

- Javadoc en métodos de más de 10 líneas
- Siempre lanzar `BusinessException` (nunca `RuntimeException` genérica)
- `toResponseDTO` privado, no un mapper separado salvo que el usuario lo pida explícitamente

---

### Paso 7 — Controller

Archivo: `src/main/java/com/cocinarubi/presentation/controller/NombreEntidadController.java`

- `@RestController @RequestMapping("/nombre-entidad")` — ruta en **kebab-case**
- `@Tag(name = "...", description = "...")` para Swagger
- Todos los métodos devuelven `ResponseEntity<ApiResponse<T>>`
- POST retorna `HttpStatus.CREATED` (201)
- DELETE retorna `ResponseEntity.noContent().build()`
- `@Valid` en todos los `@RequestBody`

---

### Paso 8 — Auditoría

Sigue `GUIA_AUDITORIA_NUEVA_TABLA.md`. Siempre hacer los tres pasos obligatorios:

1. **`AuditAspect.GETTERS_ID`** — agrega `"getIdNombreEntidad"` si no está ya en la lista
   ([AuditAspect.java](src/main/java/com/cocinarubi/aop/AuditAspect.java) ~línea 166)

2. **`AuditoriaParser`** — agrega un `case "nombre_tabla"` con descripciones en español
   ([AuditoriaParser.java](src/main/java/com/cocinarubi/domain/service/auditoria/AuditoriaParser.java) ~línea 45)

3. **`AuditoriaRepository`** — agrega `WHEN a.tabla = 'nombre_tabla' THEN 'Nombre visible'`
   en el `CASE` del JPQL, tanto en la query principal como en el `countQuery`
   ([AuditoriaRepository.java](src/main/java/com/cocinarubi/dao/AuditoriaRepository.java) ~línea 18)

4. **MixIn** (solo si hay campos sensibles) — crear `aop/mixin/NombreEntidadAuditMixin.java`
   y registrarlo en el constructor de `AuditAspect.java` ~línea 57

---

### Paso 9 — Re-indexar

Al terminar todos los archivos, ejecuta `mcp__codebase-memory-mcp__index_repository` (mode: `full`).

---

## Checklist de entrega

Confirma al usuario que se crearon o modificaron:

- [ ] `V{n}__crear_tabla_{nombre_tabla}.sql`
- [ ] `domain/entity/NombreEntidad.java`
- [ ] `dao/NombreEntidadRepository.java`
- [ ] `dto/request/NombreEntidadRequestDTO.java`
- [ ] `dto/response/NombreEntidadResponseDTO.java`
- [ ] `domain/service/NombreEntidadService.java`
- [ ] `presentation/controller/NombreEntidadController.java`
- [ ] `AuditAspect.java` — GETTERS_ID
- [ ] `AuditoriaParser.java` — case nuevo
- [ ] `AuditoriaRepository.java` — WHEN nuevo (query + countQuery)
- [ ] MixIn (si aplica)
- [ ] Re-indexado del grafo
