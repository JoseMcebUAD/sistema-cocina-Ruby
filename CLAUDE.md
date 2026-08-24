# Sistema Cocina Rubi

POS para una cocina económica. Gestiona pedidos, mesas, clientes, tickets de venta, rutas de reparto y archivos multimedia. Backend REST con autenticación JWT, WebSocket en tiempo real e integración con impresora térmica ESC/POS.

**Stack:** Java 21 · Spring Boot 3.3.5 · MySQL 8 · Flyway · Cloudinary · Docker

---

## Reglas de comportamiento

- **No ejecutes comandos Maven (`mvn`) ni Docker** a menos que el usuario lo pida explícitamente.
- **No corras los tests** a menos que el usuario lo pida explícitamente.
- Si necesitas verificar algo del entorno, pregunta antes de ejecutar.
- **Re-indexa la memoria del codebase** con `mcp__codebase-memory-mcp__index_repository` (mode `full`) al terminar cualquier cambio no trivial: creación/renombre/borrado de clases o rutas, refactor cross-file, migración Flyway nueva, o modificaciones a firmas públicas de servicios/repos/DTOs. Esto mantiene sincronizada la información que consultan las herramientas `search_graph`/`trace_path`/`get_code_snippet`. Cambios triviales (typos, comentarios, valores de constantes locales) no requieren re-indexar.

---

## Convenciones del proyecto

### Rutas REST
- Todas las rutas deben usar **kebab-case** (palabras separadas con `-`).
- Ejemplos correctos: `/horario-atencion`, `/marcar-impreso`

### Nomenclatura de clases
| Tipo | Patrón | Ejemplo |
|---|---|---|
| Entidad JPA | `PascalCase` | `Cliente`, `Pedido` |
| ID de entidad | `id<Entidad>` | `idCliente`, `idPedido` |
| Request DTO | `<Entidad>RequestDTO` | `ClienteRequestDTO` |
| Response DTO | `<Entidad>ResponseDTO` | `ClienteResponseDTO` |
| Servicio | `<Entidad>Service` | `ClienteService` |
| Repositorio | `<Entidad>Repository` | `ClienteRepository` |
| Controlador | `<Entidad>Controller` | `ClienteController` |

### Respuesta estándar de API
Todos los endpoints devuelven `ApiResponse<T>`:
```json
{ "codigo": 200, "mensaje": "...", "datos": { } }
```

### Capas — no saltar entre ellas
`Controller → Service → Repository`  
La lógica de negocio va en `Service`, nunca en `Controller` ni en `Repository`.

### DTOs
Siempre usar `request/` y `response/`. **Nunca exponer entidades JPA directamente** en la respuesta.

### Auditoría
Toda nueva entidad que persista datos debe seguir la guía en `GUIA_AUDITORIA_NUEVA_TABLA.md`.

### Migraciones de base de datos
Usar Flyway en `src/main/resources/db/migration/` con nombre `V{n}__descripcion_en_snake_case.sql`.

### Validaciones de negocio
Las validaciones complejas van en `presentation/strategy/strategyImplementation/`, no en el controlador.

### Eventos asíncronos
Operaciones que no deben bloquear el request van en `event/`.

### Comentarios en código Java

Toda clase o archivo nuevo debe incluir los siguientes comentarios:

**1. Comentario de clase** — Javadoc encima de la declaración de clase que describa su responsabilidad principal y la capa a la que pertenece.

```java
/**
 * Gestiona los pagos asociados a los repartidores.
 * Capa: Service — lógica de negocio de liquidaciones.
 */
@Service
public class PagoRepartidorService { ... }
```

**2. Comentario de método** — Javadoc breve en métodos de más de 10 líneas. Solo el *qué* y el *por qué* si no es obvio; no describir línea por línea.

```java
/**
 * Calcula el total a pagar al repartidor según las entregas del período.
 * Excluye pedidos cancelados después del corte del día.
 */
public BigDecimal calcularLiquidacion(Long idRepartidor, LocalDate fecha) { ... }
```

**3. Comentario in-line de referencia** — Una línea junto a llamadas no obvias a otros métodos o clases externas, indicando qué hace esa dependencia en este contexto.

```java
// PedidoService: recupera solo pedidos en estado ENTREGADO del día
List<Pedido> pedidos = pedidoService.obtenerEntregadosPorFecha(idRepartidor, fecha);

// ObjectMapper: serializa el detalle como JSON para almacenarlo en auditoría
String detalle = objectMapper.writeValueAsString(liquidacionDTO);
```

**Cuándo NO comentar:** métodos cortos (≤ 10 líneas) con nombres auto-explicativos no necesitan Javadoc. Evita comentarios que repitan lo que el nombre ya dice.

---

## Lo que NO hacer

- No tocar tests del paquete `printer` — dependen de hardware físico.
- No modificar `application.properties` con valores del entorno de desarrollo — usar `application-dev.properties`.
- No añadir lógica de negocio en controladores.
- No crear migraciones SQL fuera de Flyway.
- No exponer entidades JPA directamente en respuestas REST.

---

## Estructura de paquetes

```
com.cocinarubi/
├── aop/              # Auditoría AOP (@SkipAudit para omitirla)
├── config/           # Configuración Spring (async, seguridad, WebSocket)
├── dao/              # Repositorios Spring Data JPA
├── domain/
│   ├── entity/       # Entidades JPA
│   ├── interfaces/   # Contratos de servicios
│   ├── mapper/       # Mappers entidad ↔ DTO
│   └── service/      # Lógica de negocio (+ auditoria/, files/, impresion/)
├── event/            # Eventos Spring para operaciones async
├── exception/        # Excepciones de negocio y códigos de error
├── presentation/
│   ├── controller/   # Controladores REST
│   ├── dto/          # request/ y response/
│   ├── filter/       # Filtros HTTP (rate limiting, correlación)
│   ├── security/     # JWT, filtros de auth, WebSocket security
│   └── strategy/     # Validaciones de negocio por Strategy pattern
└── util/             # Utilidades, templates, infraestructura
```

---

## Tests

```bash
mvn test   # excluye **/printer/** automáticamente
```
