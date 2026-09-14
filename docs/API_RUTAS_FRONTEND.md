# API Rutas — Guía Frontend

Base URL: `/ruta`

## Cambios respecto a la versión anterior

| Campo eliminado | Dónde estaba | Motivo |
|---|---|---|
| `orden` | `Ruta` | Reemplazado por agrupación `OrdenRuta` |
| `tiempoEstimadoMin` | `Ruta` | Movido a `OrdenRuta` |

### Nueva entidad: `OrdenRuta`

Agrupa N rutas bajo un mismo grupo de reparto. El sistema tiene **6 grupos fijos** (ids 1–6) creados por seeder. El frontend los asigna, no los crea.

| Campo | Tipo | Descripción |
|---|---|---|
| `idOrdenRuta` | `number` | ID del grupo (1–6) |
| `tiempoEstimadoMin` | `number \| null` | Tiempo estimado de entrega en minutos |

---

## Endpoints

### GET /ruta
Lista simplificada de todas las rutas, ordenadas por grupo (`idOrdenRuta ASC`, sin grupo al final).

**Response 200:**
```json
{
  "timestamp": "2026-08-30T10:00:00",
  "status": 200,
  "message": "Rutas obtenidas correctamente",
  "errorCode": null,
  "data": [
    {
      "idRuta": 1,
      "uuidRuta": "uuid-ruta-1",
      "nombre": "Zona Norte",
      "active": true,
      "tarifaEnvio": 35.00,
      "idOrdenRuta": 1
    },
    {
      "idRuta": 2,
      "uuidRuta": "uuid-ruta-2",
      "nombre": "Zona Sur",
      "active": true,
      "tarifaEnvio": 40.00,
      "idOrdenRuta": null
    }
  ]
}
```

---

### GET /ruta/mapa
Lista completa con coordenadas del polígono. Usar para renderizar en mapa.

**Response 200:**
```json
{
  "status": 200,
  "message": "Rutas con coordenadas obtenidas correctamente",
  "data": [
    {
      "idRuta": 1,
      "uuidRuta": "uuid-ruta-1",
      "nombre": "Zona Norte",
      "coordinates": [
        { "latitude": 20.69, "longitude": -103.35 },
        { "latitude": 20.69, "longitude": -103.34 },
        { "latitude": 20.68, "longitude": -103.34 },
        { "latitude": 20.68, "longitude": -103.35 }
      ],
      "active": true,
      "tarifaEnvio": 35.00,
      "idOrdenRuta": 1
    }
  ]
}
```

---

### GET /ruta/{id}
Detalle de una ruta con coordenadas.

**Response 200:** igual a un elemento de `GET /ruta/mapa`.

**Response 404:**
```json
{
  "status": 404,
  "message": "Ruta no encontrada con id: 99",
  "errorCode": "NOT_FOUND"
}
```

---

### POST /ruta
Crea una nueva ruta. No requiere `orden` ni `tiempoEstimadoMin`.

**Request body:**
```json
{
  "nombre": "Zona Centro",
  "boundaryWkt": "POLYGON ((-103.35 20.69, -103.34 20.69, -103.34 20.68, -103.35 20.68, -103.35 20.69))",
  "isActive": true,
  "tarifaEnvio": 35.00
}
```

| Campo | Tipo | Requerido | Restricciones |
|---|---|---|---|
| `nombre` | `string` | ✅ | máx 45 caracteres |
| `boundaryWkt` | `string` | ✅ | Formato WKT (`POLYGON ((...))`) |
| `isActive` | `boolean` | — | default `false` |
| `tarifaEnvio` | `number` | ✅ | mayor a 0 |

**Response 201:**
```json
{
  "status": 201,
  "message": "Ruta creada correctamente",
  "data": {
    "idRuta": 7,
    "uuidRuta": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Zona Centro",
    "coordinates": [ ... ],
    "active": true,
    "tarifaEnvio": 35.00,
    "idOrdenRuta": null
  }
}
```

---

### PUT /ruta/{id}
Reemplaza todos los campos de una ruta.

**Request body:** mismo esquema que `POST /ruta`.

**Response 200:** mismo esquema que `POST /ruta` con status 200.

---

### PATCH /ruta/{id}
Actualización parcial. Enviar solo los campos a modificar.

**Request body (ejemplo):**
```json
{
  "isActive": false
}
```

Campos aceptados: `nombre`, `boundaryWkt`, `isActive`, `tarifaEnvio`.

> `idRuta` no puede enviarse — el servidor retorna 400.

**Response 200:** mismo esquema que `GET /ruta/mapa` (un elemento).

---

### DELETE /ruta/{id}

**Response 204:** sin body.

**Response 409** si la ruta tiene clientes o pedidos asociados:
```json
{
  "status": 409,
  "message": "No se puede eliminar la ruta porque está asignada a clientes existentes",
  "errorCode": "CONFLICT"
}
```

---

### PATCH /ruta/orden — Asignar rutas a un grupo
Asigna las rutas indicadas al grupo `OrdenRuta`. La asignación es **acumulativa** (merge): las rutas ya en el grupo conservan su asignación; solo se actualizan las enviadas.

**Request body:**
```json
{
  "idOrdenRuta": 1,
  "rutaIds": [1, 2, 3]
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `idOrdenRuta` | `number` | ✅ (1–6) |
| `rutaIds` | `number[]` | ✅ mínimo 1 elemento |

**Response 200:**
```json
{
  "status": 200,
  "message": "Rutas asignadas correctamente",
  "data": {
    "idOrdenRuta": 1,
    "tiempoEstimadoMin": null
  }
}
```

**Errores:**

| Caso | Status | Mensaje |
|---|---|---|
| `idOrdenRuta` no existe | 404 | `OrdenRuta no encontrada con id: X` |
| `rutaIds` vacío | 400 | `La lista de rutas no puede estar vacía` |
| `idOrdenRuta` nulo | 400 | `El id de la orden no puede ser nulo` |
| algún `rutaIds[n]` no existe | 404 | `Ruta no encontrada con id: X` |

---

### PATCH /ruta/orden/{id} — Actualizar tiempo estimado de un grupo
Actualiza el `tiempoEstimadoMin` del grupo `OrdenRuta`.

**Path param:** `id` — ID del grupo (1–6).

**Request body:**
```json
{
  "tiempoEstimadoMin": 35
}
```

**Response 200:**
```json
{
  "status": 200,
  "message": "Tiempo estimado actualizado",
  "data": {
    "idOrdenRuta": 1,
    "tiempoEstimadoMin": 35
  }
}
```

**Response 404** si el grupo no existe:
```json
{
  "status": 404,
  "message": "OrdenRuta no encontrada con id: 99",
  "errorCode": "NOT_FOUND"
}
```

---

## Flujo típico desde el dashboard

```
1. GET /ruta              → cargar lista de rutas y sus grupos actuales
2. PATCH /ruta/orden      → arrastrar rutas al grupo deseado
3. PATCH /ruta/orden/{id} → configurar tiempo estimado de ese grupo
4. GET /ruta              → confirmar estado final
```
