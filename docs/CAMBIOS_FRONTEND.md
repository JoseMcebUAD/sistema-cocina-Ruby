# Cambios en el frontend requeridos por las correcciones de seguridad

Fecha: 2026-09-22
Backend rama: `development`

Este documento lista los cambios que el equipo de frontend debe aplicar tras C1, C2, C3, C4, A2, A3, A4, A5 y A6.

---

## 🔴 CAMBIO OBLIGATORIO 1 — Endpoint de últimos pedidos (C2.2)

**Antes:**
```
GET /web/pedidos/{uuidCliente}
Authorization: Bearer <sessionToken>
```

**Ahora:**
```
GET /web/pedidos
```

- El `uuidCliente` **ya no viaja en la URL**. El backend lo resuelve internamente desde el `session_token` (cookie o Bearer).
- Motivo: el endpoint anterior permitía leer los pedidos de cualquier cliente conociendo su UUID (IDOR).
- **Acción del frontend:** cambiar `fetch('/web/pedidos/' + uuidCliente)` por `fetch('/web/pedidos', { credentials: 'include' })`.

**Impacto:** `GET /web/pedidos/{uuid}` → **404 Not Found**.

---

## 🔴 CAMBIO OBLIGATORIO 2 — `uuidCliente` en body de `POST /web/pedidos` es ignorado (C2.1)

El backend sobrescribe el campo con el UUID del cliente autenticado del `session_token`. Ningún cambio funcional si el frontend ya enviaba el UUID correcto.

---

## 🔴 CAMBIO OBLIGATORIO 3 — Cabecera `X-Fingerprint` en mutaciones `/web/**` (A5)

Todas las peticiones `POST`, `PUT`, `PATCH`, `DELETE` a rutas `/web/**` deben incluir el header custom `X-Fingerprint` (cualquier valor no vacío). Sin el header → **403 Forbidden**.

```js
fetch('/web/pedidos', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-Fingerprint': navigator.userAgent + '|' + screen.width,  // o cualquier hash
  },
  credentials: 'include',
  body: JSON.stringify(pedido),
});
```

- Motivo: bloquea CSRF cross-origin al forzar preflight CORS.
- Endpoints afectados: `POST /web/sesion`, `POST /web/pedidos`, `PUT /web/pedidos/{id}`.

**Impacto:** cualquier `POST/PUT/PATCH/DELETE` a `/web/**` sin `X-Fingerprint` → **403**.

---

## 🔴 CAMBIO OBLIGATORIO 4 — `sessionToken` se envía por cookie, ya no en el body (C4)

**Antes:** `POST /web/sesion` devolvía en el body:
```json
{ "data": { "sessionToken": "abc123...", "uuidCliente": "...", "huella": "...", "userAgent": "...", "ipAddress": "..." } }
```

**Ahora:** el body ya **no incluye** `sessionToken`. El token viaja por cookie:
```
Set-Cookie: session_token=abc123...; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=604800
```

También se eliminaron del response DTO los campos `huella`, `userAgent`, `ipAddress` (PII interno).

### 4.1 — Qué hacía cada campo eliminado y cuál puede romper el frontend

| Campo | ¿Para qué servía? | ¿El frontend lo usa? | Alternativa |
|---|---|---|---|
| **`sessionToken`** | Credencial de autenticación. El frontend lo leía y lo mandaba en `Authorization: Bearer`. | **Sí, casi seguro.** | Ahora viaja por cookie `session_token` HttpOnly → el navegador la envía sola. |
| **`huella`** | Hash SHA-256 calculado en el backend con datos del navegador. Usado internamente para rate limiting. | **Muy improbable.** El backend lo calcula solo. | Recalcular en el frontend si de verdad se necesitaba mostrar: `sha256(userAgent + '\|' + screenWidth + '\|' + screenHeight + '\|' + timezone + '\|' + language + '\|' + colorDepth)`. |
| **`ipAddress`** | La IP pública del cliente. | **No la necesita** (es su propia IP; no le aporta nada). | Ninguna. |
| **`userAgent`** | El User-Agent string del cliente. | **No la necesita** (`navigator.userAgent` ya está en el browser). | Ninguna. |

**Traducción:** el único campo eliminado que casi seguro el frontend usaba es `sessionToken`. Los otros tres eran datos internos que el backend devolvía "para debug" pero el frontend nunca necesitó.

### 4.2 — Acción del frontend

1. **Dejar de leer** `sessionToken`, `huella`, `userAgent`, `ipAddress` del body — ya no existen.
2. Al hacer requests posteriores, incluir `credentials: 'include'` en fetch/axios para que el navegador envíe la cookie automáticamente.
3. **Ya no es necesario** setear manualmente `Authorization: Bearer <sessionToken>`. El backend acepta ambos (cookie o Bearer), pero la cookie es lo recomendado.
4. Si en la sesión #2 (renovación con token vigente) no llega `Set-Cookie: session_token`, es correcto — el backend no reemite si el token vigente aún sirve; el navegador conserva la cookie previa.

### 4.3 — Antes vs Ahora en código

**Antes (inseguro):**
```js
// El frontend leía sessionToken del body y lo mandaba en cada request
const { data } = await axios.post('/web/sesion', {...});
localStorage.setItem('token', data.data.sessionToken);   // ❌ vulnerable a XSS
// ...
axios.get('/web/pedidos/' + uuidCliente, {
  headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
});
```

**Ahora (seguro):**
```js
axios.defaults.withCredentials = true;   // 👈 envía cookies automáticamente

// Login: la cookie session_token se setea automáticamente
const { data } = await axios.post('/web/sesion',
  { uuidCliente, userAgent, screenWidth, screenHeight, timezone, language, colorDepth },
  { headers: { 'X-Fingerprint': generarFingerprint() } }
);

// data.data trae SOLO el perfil del cliente autenticado:
// { idCliente, uuidCliente, nombre, direccionCliente, telefono,
//   codigoCliente, idRuta, ubicacionLatitud, ubicacionLongitud, tokenExpiracion }

// Guarda ESO en tu store (Vuex/Redux/Pinia), NO el token
setPerfilCliente(data.data);

// Requests posteriores: el navegador manda la cookie session_token sola
await axios.get('/web/pedidos', {
  headers: { 'X-Fingerprint': generarFingerprint() }
});
// ✅ Backend resuelve el cliente desde la cookie
```

---

## 🧭 ¿Cómo sabe el frontend qué usuario está autenticado?

El frontend tiene **dos mecanismos redundantes** que trabajan juntos:

| Cookie | Duración | Propósito |
|---|---|---|
| `uuid_cliente` | 1 año | Identificador estable del navegador/dispositivo. Sigue igual que antes. |
| `session_token` | 7 días | Credencial de autenticación. **Nueva — reemplaza el `sessionToken` del body.** |

En el response de `POST /web/sesion`, el frontend recibe el **perfil del cliente**:
- `idCliente`, `uuidCliente`
- `nombre`, `telefono`, `direccionCliente`, `codigoCliente`
- `idRuta`, `ubicacionLatitud`, `ubicacionLongitud`
- `tokenExpiracion`

Con eso puede pintar el nombre, el teléfono, la dirección predeterminada, la zona de reparto, etc.

### Flujo completo de identificación

```
┌───────────────────────────────────────────────────────────────┐
│  1. Frontend arranca                                           │
│     - Genera uuidCliente en localStorage (si no existe)       │
│     - POST /web/sesion con { uuidCliente, userAgent, ... }    │
│                                                                │
│  2. Backend responde:                                          │
│     Set-Cookie: uuid_cliente=abc123; HttpOnly; 1 año          │
│     Set-Cookie: session_token=xyz789; HttpOnly; 7 días        │
│     Body: { data: { nombre, telefono, direccion, ... } }      │
│                                                                │
│  3. Frontend guarda en su store: { nombre, telefono, ... }    │
│     ❌ NO guarda ningún token — la cookie ya lo tiene         │
│                                                                │
│  4. Peticiones siguientes:                                     │
│     credentials: 'include' → cookie session_token viaja        │
│     Backend → ClienteSessionFilter valida y expone Cliente    │
│     Controllers usan request.getAttribute("clienteAutenticado")│
└───────────────────────────────────────────────────────────────┘
```

### ¿Y si el usuario refresca la página?

La cookie `session_token` persiste 7 días. Al arrancar:
- La cookie sigue en el navegador.
- El frontend llama de nuevo `POST /web/sesion` (con el `uuidCliente` de localStorage).
- Recibe el perfil actualizado sin necesidad de "re-login".
- Si el token vigente sigue sirviendo, ni siquiera se emite cookie nueva (evita rotación innecesaria).

### ¿Cómo sé cuál es "mi" carrito/perfil?

Sin `sessionToken` en el body, la identidad del cliente vive **exclusivamente en la cookie** (invisible desde JS por diseño). El frontend NO necesita conocer el token — solo su perfil, que sí llega en el body. Cada request va autenticada sola porque el navegador adjunta la cookie automáticamente cuando usas `credentials: 'include'`.

---

## 🔴 CAMBIO OBLIGATORIO 5 — Nuevo endpoint `POST /auth/logout` (C3)

Para cerrar sesión del dashboard de cocina de forma segura:
```
POST /auth/logout
Authorization: Bearer <JWT>
```

- Revoca el JWT en Redis hasta su expiración natural.
- Response: `200 OK` con `ApiResponse.exito(200, "Sesión cerrada", null)`.
- Idempotente: llamarlo varias veces no da error.
- **Acción del frontend:** al hacer logout, llamar este endpoint ANTES de borrar el JWT local.

---

## 🔴 CAMBIO IMPLÍCITO 6 — Tokens JWT expirados ya no se renuevan automáticamente (C3)

**Antes:** si un JWT expiraba y el usuario seguía dentro de la ventana de 3h de renovación, cualquier request lo renovaba silenciosamente → sesión eterna en la práctica.
**Ahora:** un JWT expirado → **401 Unauthorized**. Cambiar la contraseña de un usuario también invalida todos sus JWT vivos (`token_version` se incrementa).

- **Acción del frontend:** en la interceptación de respuestas 401, redirigir a login (en vez de reintentar). El JWT sigue renovándose en cada request válida via header `Authorization` de respuesta.

---

## 🟡 CAMBIO RECOMENDADO 7 — HSTS (A2)

El backend ahora envía:
```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
```

**Acción del frontend:** todos los recursos (fetch, `<img>`, `<script>`, WebSocket) deben usar `https://`. Ningún mixed content permitido.

---

## 🟡 CAMBIO OPERACIONAL 8 — Variables de entorno en producción (C1 + A6)

- **`TRUSTED_PROXIES`** — CSV con la IP interna del nginx que hace terminación TLS. Ej: `172.18.0.2,127.0.0.1`. Sin esta variable bien configurada, los rate limiters cuentan todo bajo la IP del proxy y bloquean usuarios legítimos.
- **`JWT_SECRET`** — rotar (los operadores harán login una vez).
- **`CLOUDINARY_API_SECRET`** — rotar desde el dashboard.
- **`DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`** — todas por env vars.

Ver [`.env.example`](.env.example) en la raíz del repo.

---

## ✅ SIN CAMBIOS

- URL/método de `POST /web/pedidos`, `PUT /web/pedidos/{id}`, `GET /web/rutas`, `GET /web/rutas/por-ubicacion`, `GET /menu-web/**`.
- Cookie `uuid_cliente` sigue igual.
- DTOs de pedido (`PedidoRequestDTO`, `PedidoResponseDTO`) sin cambios.
- CORS: mismos orígenes permitidos.
- WebSocket: sin cambios (aunque ahora un token expirado sí es rechazado en el CONNECT).

---

## 🔎 Cómo auditar tu código frontend

Antes de desplegar el nuevo backend, corre estos greps en tu repo frontend:

```bash
# ¿Alguien lee los campos eliminados del response?
grep -rn "\.sessionToken" src/
grep -rn "\.huella" src/
grep -rn "\.ipAddress" src/
grep -rn "\.userAgent" src/    # solo del response, no navigator.userAgent

# ¿Alguien manda el token manualmente?
grep -rn "Authorization.*Bearer" src/

# ¿Alguien concatena el uuidCliente al path de /web/pedidos?
grep -rn "web/pedidos/" src/
```

Si hay hits, hay que cambiar:
- **`sessionToken`** → eliminar código, usar `credentials: 'include'` en su lugar.
- **`huella`, `ipAddress`, `userAgent`** → probablemente puedes eliminarlos sin más.
- **`web/pedidos/` con UUID** → reemplazar por `web/pedidos` sin sufijo.

---

## Checklist de verificación en frontend

- [ ] Buscar `pedidos/${uuidCliente}` en GET → reemplazar por `pedidos`.
- [ ] Añadir `X-Fingerprint` a **todo** POST/PUT/PATCH/DELETE de `/web/**`.
- [ ] Dejar de leer `sessionToken`, `huella`, `ipAddress`, `userAgent` del body de `POST /web/sesion`.
- [ ] Guardar solo el **perfil del cliente** (nombre, telefono, direccion, idRuta, etc.) en el store — no el token.
- [ ] Activar `credentials: 'include'` (axios: `withCredentials = true`) en todas las requests a `/web/**` y `/menu-web/**`.
- [ ] Implementar llamada a `POST /auth/logout` en el botón "Cerrar sesión" del dashboard.
- [ ] Interceptor 401 → redirigir a login (no reintentar).
- [ ] Verificar que todos los recursos cargan por HTTPS.
- [ ] Probar: login → hacer un pedido → consultar historial → editar dentro de 5 min → logout → intentar usar token viejo (debe fallar).

---

## Preguntas frecuentes

**¿Se rompe algo si el frontend sigue enviando `Authorization: Bearer <sessionToken>`?**
No. El backend prioriza la cookie `session_token`, pero acepta el Bearer como fallback. Con la cookie es más seguro (HttpOnly = no accesible desde JS = inmune a XSS).

**¿Cómo genero el valor de `X-Fingerprint`?**
Cualquier string no vacío basta. Recomendación: hash SHA-256 de `userAgent + screen.width + screen.height + language`. No es un secreto — su función es solo forzar preflight CORS.

**¿Puedo seguir usando SessionStorage/LocalStorage para guardar el token?**
Con la cookie HttpOnly ya no es necesario ni recomendable — la cookie viaja sola. Si lo mantienes por back-compat, asegúrate de que el interceptor 401 lo limpie.

**¿La cookie `session_token` funciona en Safari iOS con `SameSite=None`?**
Sí, siempre y cuando `Secure=true` (HTTPS obligatorio).

**¿Qué pasa si el usuario está en múltiples pestañas y hace logout en una?**
Todas las pestañas fallarán con 401 en la próxima request (el JWT está en la denylist Redis).

**¿El backend puede devolverme la IP del cliente si de verdad la necesito para mostrarla?**
No en el response de `/web/sesion`. Si tienes un caso de uso claro (panel de admin, historial de accesos), pídelo como endpoint separado (`GET /web/mi-sesion/metadata` o similar). Para uso "el usuario está en tal ciudad" es preferible un servicio externo de geolocalización en el frontend.

**¿Cómo restauro `huella` si la necesitaba para X?**
Recalcúlala en el frontend antes de mandar la request de sesión — el backend usa exactamente el mismo algoritmo:
```js
async function calcularHuella() {
  const raw = [navigator.userAgent, screen.width, screen.height,
               Intl.DateTimeFormat().resolvedOptions().timeZone,
               navigator.language, screen.colorDepth].join('|');
  const buf = new TextEncoder().encode(raw);
  const hash = await crypto.subtle.digest('SHA-256', buf);
  return Array.from(new Uint8Array(hash))
    .map(b => b.toString(16).padStart(2, '0')).join('');
}
```
