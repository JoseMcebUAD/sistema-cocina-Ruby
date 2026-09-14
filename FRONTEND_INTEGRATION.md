# Integración Frontend — API Web Cocina Rubí

Guía de referencia para el equipo de frontend. Todos los endpoints de `/web/**` son públicos en Spring Security; el acceso a recursos protegidos se controla mediante el `sessionToken`.

---

## Breaking Changes (v2.0)

### El `uuidCliente` ya no lo maneja el frontend

El servidor ahora genera y persiste el `uuidCliente` en una cookie HttpOnly llamada
`uuid_cliente` (1 año de vida). El frontend **ya no debe** generarlo ni guardarlo
en localStorage.

**Qué debes hacer:**

1. Agregar `credentials: 'include'` a **todos** los requests hacia `/web/**` y `/menu-web`.
2. Eliminar el código que generaba `uuidCliente` con `crypto.randomUUID()` y lo guardaba en localStorage.
3. El campo `uuidCliente` en el body de `POST /web/sesion` es ahora **opcional** (se acepta por retrocompatibilidad durante la migración).

---

## Cookie `uuid_cliente`

| Atributo | Valor | Motivo |
|----------|-------|--------|
| `HttpOnly` | `true` | JS no puede leer ni modificar el UUID |
| `Secure` | `true` | Solo viaja por HTTPS |
| `SameSite` | `None` | Requerido para requests cross-origin (`carrito.cocinarubi.com` → `api.cocinarubi.com`) |
| `Max-Age` | `31536000` (1 año) | Persiste aunque el usuario cierre el navegador |
| `Path` | `/` | Aplica a todos los endpoints |

El browser envía y recibe esta cookie **automáticamente** en cada request siempre que
uses `credentials: 'include'`. No necesitas leerla ni escribirla desde JS.

---

## `credentials: 'include'` — obligatorio en todos los requests

### Con `fetch`
```javascript
const res = await fetch('https://api.cocinarubi.com/web/sesion', {
  method: 'POST',
  credentials: 'include',           // <-- requerido
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(payload)
});
```

### Con axios
```javascript
// Una vez al inicializar
axios.defaults.withCredentials = true;
```

---

## Flujo de inicio obligatorio

```
1. App abre → ¿localStorage tiene sessionToken vigente?
   ├── Sí → usa ese token directamente
   └── No → llama POST /web/sesion (credentials: 'include')
             → el servidor lee/crea la cookie uuid_cliente automáticamente
             → guarda sessionToken recibido en localStorage
```

---

## 1. Iniciar sesión / obtener token

**`POST /web/sesion`** — No requiere token

### Request
```json
{
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
| `uuidCliente` | `string` | No | Deprecated — el servidor lo gestiona por cookie. Max 45 chars. |
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
  "timestamp": "2026-08-30T10:00:00",
  "status": 200,
  "message": "Sesión iniciada correctamente",
  "data": {
    "idCliente": 42,
    "uuidCliente": "550e8400-e29b-41d4-a716-446655440000",
    "sessionToken": "a3f9b2c1-7d4e-4f2a-b8c3-1234567890ab",
    "tokenExpiracion": "2026-09-06T10:00:00",
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

La respuesta HTTP incluye también:
```
Set-Cookie: uuid_cliente=550e8400-...; Max-Age=31536000; Path=/; Secure; HttpOnly; SameSite=None
```

### Comportamiento del servidor
- **Primera visita / sin cookie:** genera UUID nuevo, crea cliente, emite cookie.
- **Visita con cookie:** recupera el cliente por el UUID del cookie, renueva el token si expiró.
- **Body con `uuidCliente` y sin cookie:** usa el UUID del body (retrocompatibilidad durante migración).

### Cómo guardar y usar el token
```javascript
async function iniciarSesion() {
  const res = await fetch('https://api.cocinarubi.com/web/sesion', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userAgent: navigator.userAgent,
      screenWidth: screen.width,
      screenHeight: screen.height,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      language: navigator.language,
      colorDepth: screen.colorDepth
    })
  });
  const { data } = await res.json();
  localStorage.setItem('sessionToken', data.sessionToken);
  localStorage.setItem('tokenExpiracion', data.tokenExpiracion);
  // Guardar uuidCliente solo si lo necesitas para GET /web/pedidos/{uuid}
  localStorage.setItem('uuidCliente', data.uuidCliente);
}
```

---

## 2. Obtener el menú completo

**`GET /menu-web`** — Requiere token

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
{ "status": 401, "message": "Token de sesión requerido" }
```

### Error token expirado `401`
```json
{ "status": 401, "message": "Token de sesión inválido o expirado" }
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
      "idRuta": 3,
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

**Nota:** Solo se retornan rutas activas (`active: true`). Usa `idRuta` al crear un pedido a domicilio.

---

## 4. Historial de pedidos del cliente

**`GET /web/pedidos/{uuidCliente}`** — Requiere token

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

**`POST /web/pedidos`** — Requiere token

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
    "uuidRuta": "ruta-uuid-1",
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

### Errores posibles

| HTTP | Cuándo |
|------|--------|
| `400` | Falta un campo obligatorio o no pasa validación (`uuidRuta` vacío, `direccion` vacía, etc.) |
| `400` | Producto, complemento, básico o ruta no existe en base de datos |
| `401` | Header `Authorization` ausente o token inválido / expirado |
| `409` | Comida, complemento, desayuno, básico o producto de cocina con estatus `NO_DISPONIBLE` |
| `409` | Pedido con desayunos enviado después de las 11:00 h |

---

## 6. Actualizar un pedido

**`PUT /web/pedidos/{id}`** — Requiere token

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
function tokenEsValido() {
  const expiracion = localStorage.getItem('tokenExpiracion');
  if (!expiracion) return false;
  return new Date(expiracion) > new Date();
}

async function peticionProtegida(url, opciones = {}) {
  if (!tokenEsValido()) {
    await iniciarSesion();
  }
  const res = await fetch(url, {
    ...opciones,
    credentials: 'include',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`,
      'Content-Type': 'application/json',
      ...opciones.headers
    }
  });
  if (res.status === 401) {
    await iniciarSesion();
    return fetch(url, { ...opciones, credentials: 'include' });
  }
  return res;
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

> Todos los requests requieren `credentials: 'include'` para que el browser gestione
> automáticamente la cookie `uuid_cliente`.
