# Guía: agregar una nueva tabla al módulo de auditoría

Checklist completo para cuando se crea una nueva entidad/tabla que debe quedar registrada
en la tabla `auditoria`. Sigue los pasos en orden; algunos son opcionales según el caso.

---

## 1. Entidad JPA — registro automático

`EntityClassResolver` escanea `com.cocinarubi.domain.entity` al arrancar y registra
automáticamente toda clase anotada con `@Entity`.

**Regla de nombre de tabla:**
- Si la entidad tiene `@Table(name = "nombre_tabla")` → usa ese nombre exacto.
- Si no tiene `@Table` → convierte el nombre de la clase con camelCase → snake_case en minúsculas.
  Ejemplo: `HorarioAtencion` → `horario_atencion`.

**No se necesita ningún cambio** en `EntityClassResolver` siempre que la entidad esté
en el paquete correcto y siga una de las dos reglas anteriores.

---

## 2. Getter de ID — `AuditAspect.GETTERS_ID`

`AuditAspect` intenta extraer el `id` del objeto de respuesta probando los getters en
`GETTERS_ID` (array en [AuditAspect.java:166](src/main/java/com/cocinarubi/aop/AuditAspect.java#L166)).

**Si el ID de la nueva entidad ya existe en la lista → no hay nada que hacer.**

**Si el getter aún no está en la lista**, agrégalo:

```java
// AuditAspect.java — array GETTERS_ID
private static final String[] GETTERS_ID = {
    // ... getters existentes ...
    "getIdNuevaEntidad",   // <-- agregar aquí
};
```

El nombre debe coincidir exactamente con el método getter de la entidad
(por convención de Lombok/Java: `getIdNuevaEntidad` para el campo `idNuevaEntidad`).

---

## 3. Descripción legible — `AuditoriaParser`

`AuditoriaParser` genera la descripción en español que aparece en `GET /auditoria`.
Si la tabla nueva cae en el `default`, mostrará `"POST en nueva_entidad #5"`, que es
funcional pero genérico.

**Para agregar descripción personalizada**, añade un `case` en el `switch` de
[AuditoriaParser.java:45](src/main/java/com/cocinarubi/domain/service/auditoria/AuditoriaParser.java#L45):

```java
case "nueva_entidad" -> describirNuevaEntidad(accion, despues, antes, id, fecha);
```

Y agrega el método privado siguiendo el mismo patrón que los existentes:

```java
private String describirNuevaEntidad(TipoOperacion accion, JsonNode despues,
                                      JsonNode antes, Integer id, LocalDateTime fecha) {
    return switch (accion) {
        case POST -> {
            // Usa el nombre real del campo JSON serializado por la entidad
            String nombre = textOrElse(despues, "nombreCampo", "desconocido");
            yield "Se creó la entidad '" + nombre + "'";
        }
        case PUT -> {
            String nombreDespues = textOrElse(despues, "nombreCampo", null);
            String nombreAntes   = textOrElse(antes,   "nombreCampo", null);
            String nombre = nombreDespues != null ? nombreDespues
                          : (nombreAntes  != null ? nombreAntes : "#" + id);
            yield "Se actualizó la entidad '" + nombre + "'";
        }
        case DELETE -> "Se eliminó la entidad #" + id;
    };
}
```

> **Tip:** Los nombres de los campos en `despues`/`antes` corresponden a la serialización
> JSON de la entidad (nombres de campo tal como aparecerían en el response HTTP del controller).
> Con Lombok `@Getter`, el campo `nombreComida` serializa como `"nombreComida"`.

---

## 4. Nombre legible en el filtro — `AuditoriaRepository`

El JPQL en [AuditoriaRepository.java:18](src/main/java/com/cocinarubi/dao/AuditoriaRepository.java#L18)
tiene un `CASE` que mapea el nombre de tabla a un texto para mostrar en el frontend.
Si no se agrega, `nombreTabla` aparecerá como `'Desconocido'`.

Agrega un `WHEN` antes del `ELSE`:

```sql
WHEN a.tabla = 'nueva_entidad' THEN 'Nombre visible para el usuario'
```

Recuerda agregarlo también en el `countQuery` para mantener coherencia.

---

## 5. Campos sensibles — MixIn (opcional)

Solo aplica si la entidad tiene campos que **no deben aparecer en el snapshot JSON**
almacenado en `datos_antes` / `datos_despues` (tokens, contraseñas, IPs, etc.).

### Paso A — crear el MixIn

```java
// src/main/java/com/cocinarubi/aop/mixin/NuevaEntidadAuditMixin.java
package com.cocinarubi.aop.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class NuevaEntidadAuditMixin {
    @JsonIgnore abstract String getCampoSensible();
    @JsonIgnore abstract String getOtroCampoSensible();
}
```

El nombre del método debe coincidir con el getter real de la entidad.

### Paso B — registrar el MixIn en `AuditAspect`

En el constructor de [AuditAspect.java:57](src/main/java/com/cocinarubi/aop/AuditAspect.java#L57),
agregar el `addMixIn` junto a los existentes:

```java
this.auditObjectMapper = objectMapper.copy()
        .registerModule(hm)
        .addMixIn(Cliente.class,       ClienteAuditMixin.class)
        .addMixIn(Pedido.class,        PedidoAuditMixin.class)
        .addMixIn(NuevaEntidad.class,  NuevaEntidadAuditMixin.class);  // <-- agregar
```

Y el import correspondiente al inicio del archivo:

```java
import com.cocinarubi.aop.mixin.NuevaEntidadAuditMixin;
import com.cocinarubi.domain.entity.NuevaEntidad;
```

> El MixIn aplica **solo** al `auditObjectMapper` (copia aislada).
> Las respuestas HTTP de la API no se ven afectadas.

---

## 6. Excluir del audit — `@SkipAudit` (opcional)

Si el controller o un método específico **no** debe auditarse:

### Excluir todo el controller

```java
@SkipAudit
@RestController
@RequestMapping("/nueva-ruta")
public class NuevaEntidadController { ... }
```

### Excluir un método específico

```java
@SkipAudit
@PatchMapping("/{id}/accion-interna")
public ResponseEntity<Void> accionInterna(@PathVariable int id) { ... }
```

> `@SkipAudit` hace que `AuditAspect` salte completamente el método sin registrar nada,
> como si el pointcut no lo hubiera interceptado.

---

## Resumen de archivos a modificar

| Archivo | Cuándo |
|---|---|
| [AuditAspect.java — `GETTERS_ID`](src/main/java/com/cocinarubi/aop/AuditAspect.java#L166) | Si el getter del ID no está en la lista |
| [AuditoriaParser.java](src/main/java/com/cocinarubi/domain/service/auditoria/AuditoriaParser.java) | Siempre (para descripción personalizada) |
| [AuditoriaRepository.java — `CASE`](src/main/java/com/cocinarubi/dao/AuditoriaRepository.java#L18) | Siempre (para nombre visible en filtros) |
| `aop/mixin/NuevaEntidadAuditMixin.java` *(nuevo)* | Si hay campos sensibles |
| [AuditAspect.java — constructor](src/main/java/com/cocinarubi/aop/AuditAspect.java#L57) | Si se creó un MixIn |
| Controller nuevo | Si se debe excluir con `@SkipAudit` |

Los cambios **que nunca se necesitan** al agregar una tabla nueva:
- `EntityClassResolver` — escanea automáticamente.
- `AsyncConfig` — pool es compartido, no depende de las tablas.
- `Auditoria.java` (entidad) — no cambia.
- `AuditoriaController` — no cambia.
- Migración Flyway — la tabla `auditoria` ya existe desde `V10`.
