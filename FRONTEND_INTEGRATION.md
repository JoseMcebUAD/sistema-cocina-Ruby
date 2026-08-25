# Integración Frontend — API Web Cocina Rubí

Guía de referencia para el equipo de frontend. Todos los endpoints de `/web/**` son públicos en Spring Security; el acceso a recursos protegidos se controla mediante el `sessionToken`.

---

## Flujo de inicio obligatorio

Cuando el usuario abre la aplicación por primera vez (o si el token expiró), debes llamar a `POST /web/sesion` antes de cualquier otra petición protegida.

```
1. App abre → ¿localStorage tiene sessionToken vigente?
   ├── Sí → usa ese token directamente
   └── No → llama POST /web/sesion → guarda token recibido
```

---

## 1. Iniciar sesión / obtener token

**`POST /web/sesion`** — No requiere token

### Request
```json
{
  "uuidCliente": "550e8400-e29b-41d4-a716-446655440000",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) ...",
  "screenWidth": 1920,
  "screenHeight": 1080,
  "timezone": "America/Merida",
  "language": "es-MX",
  "colorDepth": 24,
  "ipAddress": "192.168.1.1"
}
```

| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `uuidCliente` | `string` | ✅ | UUID generado en localStorage. Max 45 chars. |
| `userAgent` | `string` | No | `navigator.userAgent` |
| `screenWidth` | `integer` | No | `screen.width` |
| `screenHeight` | `integer` | No | `screen.height` |
| `timezone` | `string` | No | `Intl.DateTimeFormat().resolvedOptions().timeZone` |
| `language` | `string` | No | `navigator.language` |
| `colorDepth` | `integer` | No | `screen.colorDepth` |
| `ipAddress` | `string` | No | IP del cliente (si está disponible). Max 45 chars. |

### Response `200 OK`
```json
{
  "timestamp": "2026-08-24T10:00:00",
  "status": 200,
  "message": "Sesión iniciada correctamente",
  "data": {
    "idCliente": 42,
    "uuidCliente": "550e8400-e29b-41d4-a716-446655440000",
    "sessionToken": "a3f9b2c1-7d4e-4f2a-b8c3-1234567890ab",
    "tokenExpiracion": "2026-08-31T10:00:00",
    "huella": "e3b0c44298fc1c149afb...",
    "codigoCliente": null,
    "userAgent": "Mozilla/5.0 ...",
    "ipAddress": "192.168.1.1",
    "ubicacionLatitud": null,
    "ubicacionLongitud": null,
    "nombre": null,
    "direccionCliente": null,
    "telefono": null,
    "idRuta": null
  }
}
```

### Cómo guardar y usar el token
```javascript
// Al recibir la respuesta de /web/sesion
localStorage.setItem('sessionToken', data.sessionToken);
localStorage.setItem('tokenExpiracion', data.tokenExpiracion);
localStorage.setItem('uuidCliente', data.uuidCliente);

// Función helper para verificar si el token expiró
function tokenEsValido() {
  const expiracion = localStorage.getItem('tokenExpiracion');
  if (!expiracion) return false;
  return new Date(expiracion) > new Date();
}

// Header a incluir en peticiones protegidas
const headers = {
  'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`,
  'Content-Type': 'application/json'
};
```

### Cómo generar `uuidCliente`
```javascript
// Generar UUID solo si no existe
function obtenerUuidCliente() {
  let uuid = localStorage.getItem('uuidCliente');
  if (!uuid) {
    uuid = crypto.randomUUID(); // o usar una librería uuid
    localStorage.setItem('uuidCliente', uuid);
  }
  return uuid;
}
```

### Comportamiento del servidor
- **Cliente nuevo**: crea registro, genera token con 7 días de vida.
- **Cliente existente + token vigente**: devuelve el mismo token (no renueva).
- **Cliente existente + token expirado**: genera nuevo token, resetea expiración a 7 días.

---

## 2. Obtener el menú completo

**`GET /menu-web`** — ⚠️ Requiere token

### Request
```
GET /menu-web
Authorization: Bearer <sessionToken>
```

### Response `200 OK`
```json
{
  "data": {
    "comidas": [...],
    "basicos": [...],
    "desayunos": [...],
    "paquetes": [...],
    "categorias": [...]
  }
}
```

### Error sin token `401`
```json
{
  "status": 401,
  "message": "Token de sesión requerido"
}
```

### Error token expirado `401`
```json
{
  "status": 401,
  "message": "Token de sesión inválido o expirado"
}
```

---

## 3. Obtener rutas de entrega

**`GET /web/rutas`** — No requiere token

### Request
```
GET /web/rutas
```

### Response `200 OK`
```json
{
  "data": [
    {
      "uuidRuta": "ruta-uuid-1",
      "nombre": "Centro",
      "active": true,
      "tarifaEnvio": 40.00,
      "tiempoEstimadoMin": 25,
      "orden": 1
    }
  ]
}
```

**Nota:** Solo se retornan rutas activas (`active: true`). No se expone el `idRuta` interno.

---

## 4. Historial de pedidos del cliente

**`GET /web/pedidos/{uuidCliente}`** — ⚠️ Requiere token

### Request
```
GET /web/pedidos/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <sessionToken>
```

### Response `200 OK`
```json
{
  "data": [
    {
      "idPedido": 101,
      "tipoPedido": "DOMICILIO",
      "pedidoCreadoDesde": "WEB",
      "fechaExpedicionPedido": "2026-08-24T12:30:00",
      "precioFinalOrden": 150.00,
      "pagado": false,
      "comidas": [...],
      "pedidoDomicilio": { ... }
    }
  ]
}
```

Retorna máximo los **últimos 5 pedidos** ordenados por fecha descendente.

---

## 5. Crear un pedido

**`POST /web/pedidos`** — ⚠️ Requiere token

### Request
```
POST /web/pedidos
Authorization: Bearer <sessionToken>
Content-Type: application/json
```

```json
{
  "metodoPagoPrincipal": "EFECTIVO",
  "tipoPedido": "DOMICILIO",
  "pedidoCreadoDesde": "WEB",
  "pagoCliente": 200.00,
  "uuidCliente": "550e8400-e29b-41d4-a716-446655440000",
  "comentario": "Sin cebolla por favor",
  "comidas": [
    {
      "idComida": 5,
      "tamanoPorcion": "ENTERA",
      "precioUnitario": 120.00,
      "complementos": []
    }
  ],
  "desayunos": [],
  "basicos": [],
  "productosCocina": [],
  "paquetes": [],
  "domicilio": {
    "idRuta": 2,
    "direccion": "Calle 60 No. 123 x 45 y 47",
    "latitud": 20.9674,
    "longitud": -89.5926
  }
}
```

### Response `201 Created`
```json
{
  "status": 201,
  "message": "Pedido creado correctamente",
  "data": {
    "idPedido": 205,
    "tipoPedido": "DOMICILIO",
    "pedidoCreadoDesde": "WEB",
    ...
  }
}
```

---

## 6. Actualizar un pedido

**`PUT /web/pedidos/{id}`** — ⚠️ Requiere token

### Request
```
PUT /web/pedidos/205
Authorization: Bearer <sessionToken>
Content-Type: application/json
```
Body: misma estructura que `POST /web/pedidos`.

### Response `200 OK`
```json
{
  "status": 200,
  "message": "Pedido actualizado correctamente",
  "data": { ... }
}
```

---

## Manejo de expiración (flujo recomendado)

```javascript
async function peticionProtegida(url, opciones = {}) {
  if (!tokenEsValido()) {
    await renovarSesion(); // llama POST /web/sesion
  }
  const res = await fetch(url, {
    ...opciones,
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`,
      'Content-Type': 'application/json',
      ...opciones.headers
    }
  });
  if (res.status === 401) {
    await renovarSesion();
    // reintentar una vez
    return fetch(url, opciones);
  }
  return res;
}

async function renovarSesion() {
  const body = {
    uuidCliente: obtenerUuidCliente(),
    userAgent: navigator.userAgent,
    screenWidth: screen.width,
    screenHeight: screen.height,
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    language: navigator.language,
    colorDepth: screen.colorDepth
  };
  const res = await fetch('/web/sesion', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  const { data } = await res.json();
  localStorage.setItem('sessionToken', data.sessionToken);
  localStorage.setItem('tokenExpiracion', data.tokenExpiracion);
}
```

---

## Resumen de endpoints

| Método | Endpoint | Token requerido | Descripción |
|--------|----------|----------------|-------------|
| `POST` | `/web/sesion` | No | Crear o refrescar sesión |
| `GET` | `/web/rutas` | No | Rutas de entrega activas |
| `GET` | `/menu-web` | ✅ | Menú completo |
| `GET` | `/web/pedidos/{uuid}` | ✅ | Últimos 5 pedidos del cliente |
| `POST` | `/web/pedidos` | ✅ | Crear pedido |
| `PUT` | `/web/pedidos/{id}` | ✅ | Actualizar pedido |
