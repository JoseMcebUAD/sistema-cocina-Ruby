# API de Imágenes — Guía de integración Frontend

Base URL: `http://localhost:8080`

Tipos de entidad soportados: `COMIDA` `COMPLEMENTO` `DESAYUNO` `SNACK` `CHAROLA` `BEBIDA` `POSTRE`

---

## Estructura de respuesta

Todos los endpoints (excepto DELETE) devuelven un wrapper `ApiResponse`:

```json
{
  "status": 200,
  "message": "Archivos obtenidos correctamente",
  "data": [ ...archivos ]
}
```

Cada objeto archivo tiene esta forma:

```json
{
  "idArchivo": 3,
  "entityType": "COMIDA",
  "idEntidad": 1,
  "pathArchivo": "https://res.cloudinary.com/xxx/image/upload/v.../cocina_rubi/comida/abc.jpg",
  "publicId": "cocina_rubi/comida/abc",
  "mimeType": "image/jpeg",
  "nombreArchivo": "foto_arroz.jpg",
  "orden": 1,
  "creadoEn": "2026-07-20T14:30:00"
}
```

> `pathArchivo` es la URL lista para usar en `<img src="">`.  
> `idArchivo` es lo único que necesitas para eliminar.

---

## Endpoints

### 1. Subir imagen(es) — `POST /files`

Content-Type: `multipart/form-data`  
La metadata va como JSON en el campo `meta`, los archivos en el campo `files`.

**Restricciones del servidor:**
- MIME aceptados: `image/jpeg`, `image/png`, `image/webp`
- Tamaño máximo por archivo: **5 MB**
- Tamaño máximo de la petición: **25 MB**

```js
async function subirImagenes(entityType, idEntidad, archivos) {
  const formData = new FormData();

  // El campo "meta" debe ser un JSON string con Content-Type application/json
  formData.append(
    'meta',
    new Blob([JSON.stringify({ entityType, idEntidad })], { type: 'application/json' })
  );

  // "files" puede ser uno o varios archivos
  for (const archivo of archivos) {
    formData.append('files', archivo);
  }

  const res = await fetch('/files', {
    method: 'POST',
    body: formData,
    // NO pongas Content-Type manualmente — el browser lo agrega con el boundary
  });

  if (!res.ok) throw await res.json();
  return (await res.json()).data; // ArchivoResponseDTO[]
}

// Ejemplo desde un <input type="file" multiple>
const input = document.getElementById('input-imagenes');
subirImagenes('COMIDA', 1, input.files);
```

**Con Axios:**
```js
async function subirImagenesAxios(entityType, idEntidad, archivos) {
  const formData = new FormData();
  formData.append(
    'meta',
    new Blob([JSON.stringify({ entityType, idEntidad })], { type: 'application/json' })
  );
  for (const archivo of archivos) {
    formData.append('files', archivo);
  }

  const { data } = await axios.post('/files', formData);
  return data.data;
}
```

**Respuesta exitosa — 201:**
```json
{
  "status": 201,
  "message": "Archivos subidos correctamente",
  "data": [
    {
      "idArchivo": 5,
      "entityType": "COMIDA",
      "idEntidad": 1,
      "pathArchivo": "https://res.cloudinary.com/.../cocina_rubi/comida/imagen.jpg",
      "orden": 1
    }
  ]
}
```

---

### 2. Obtener todas las imágenes de una entidad — `GET /files`

```js
async function getImagenes(entityType, idEntidad) {
  const res = await fetch(`/files?entityType=${entityType}&idEntidad=${idEntidad}`);
  if (!res.ok) throw await res.json();
  return (await res.json()).data; // ArchivoResponseDTO[]
}

// Ejemplo: mostrar imágenes de la comida con id 3
const imagenes = await getImagenes('COMIDA', 3);
imagenes.forEach(img => {
  const el = document.createElement('img');
  el.src = img.pathArchivo;
  document.body.appendChild(el);
});
```

Las imágenes vienen ordenadas por el campo `orden` (ascendente).

---

### 3. Portada de varios productos (listados) — `GET /files/portada`

Para listados de comidas/complementos/etc. donde solo necesitas **una imagen por producto**. Devuelve la imagen de menor `orden` por entidad.

**Query params:**

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `entityType` | `TipoCatalogoProducto` | Sí | Tipo de entidad |
| `ids` | `integer[]` | Sí | Lista de ids separados por coma |

```js
async function getPortadas(entityType, ids) {
  const params = new URLSearchParams({ entityType });
  ids.forEach(id => params.append('ids', id));

  const res = await fetch(`/files/portada?${params}`);
  if (!res.ok) throw await res.json();
  return (await res.json()).data; // Record<number, ArchivoResponseDTO>
}

// Ejemplo: listado de comidas
const comidas = [{ id: 1, nombre: 'Arroz' }, { id: 2, nombre: 'Pollo' }];
const portadas = await getPortadas('COMIDA', comidas.map(p => p.id));

comidas.forEach(comida => {
  const portada = portadas[comida.id]; // undefined si no tiene imagen
  if (portada) {
    document.getElementById(`img-${comida.id}`).src = portada.pathArchivo;
  }
});
```

**Respuesta exitosa — 200:**
```json
{
  "status": 200,
  "message": "Portadas obtenidas correctamente",
  "data": {
    "1": { "idArchivo": 3, "entityType": "COMIDA", "idEntidad": 1, "pathArchivo": "https://...", "orden": 1 },
    "2": { "idArchivo": 7, "entityType": "COMIDA", "idEntidad": 2, "pathArchivo": "https://...", "orden": 1 }
  }
}
```

> Si un producto no tiene imagen, su key no aparece en el mapa — maneja el caso `undefined` en el frontend.

**Tipo TypeScript:**
```ts
async function getPortadas(
  entityType: TipoCatalogoProducto,
  ids: number[]
): Promise<Record<number, ArchivoResponseDTO>> {
  const params = new URLSearchParams({ entityType });
  ids.forEach(id => params.append('ids', String(id)));

  const res = await fetch(`/files/portada?${params}`);
  if (!res.ok) throw await res.json();
  const json: ApiResponse<Record<string, ArchivoResponseDTO>> = await res.json();
  return json.data as unknown as Record<number, ArchivoResponseDTO>;
}
```

---

### 4. Obtener todas las imágenes de varios productos — `GET /files/batch`

Para la vista de detalle de un producto o galería completa. Devuelve **todas** las imágenes agrupadas por `idEntidad`.

**Query params:**

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `entityType` | `TipoCatalogoProducto` | Sí | Tipo de entidad |
| `ids` | `integer[]` | Sí | Lista de ids separados por coma |

```js
async function getImagenesBatch(entityType, ids) {
  const params = new URLSearchParams({ entityType });
  ids.forEach(id => params.append('ids', id));

  const res = await fetch(`/files/batch?${params}`);
  if (!res.ok) throw await res.json();
  return (await res.json()).data; // Record<number, ArchivoResponseDTO[]>
}

// Ejemplo: listado de comidas
const productos = [{ id: 1, nombre: 'Arroz' }, { id: 2, nombre: 'Pollo' }];
const imagenesPorId = await getImagenesBatch('COMIDA', productos.map(p => p.id));
// imagenesPorId[1] → ArchivoResponseDTO[] de "Arroz"
// imagenesPorId[2] → ArchivoResponseDTO[] de "Pollo"
// Si una entidad no tiene archivos, su key simplemente no aparece en el mapa
```

**Respuesta exitosa — 200:**
```json
{
  "status": 200,
  "message": "Archivos obtenidos correctamente",
  "data": {
    "1": [
      { "idArchivo": 3, "entityType": "COMIDA", "idEntidad": 1, "pathArchivo": "https://...", "orden": 1 }
    ],
    "2": [
      { "idArchivo": 7, "entityType": "COMIDA", "idEntidad": 2, "pathArchivo": "https://...", "orden": 1 },
      { "idArchivo": 8, "entityType": "COMIDA", "idEntidad": 2, "pathArchivo": "https://...", "orden": 2 }
    ]
  }
}
```

> Las keys del mapa son strings aunque representen números (comportamiento estándar de JSON).  
> Los archivos de cada entidad vienen ordenados por `orden` ascendente.

**Con Axios:**
```js
async function getImagenesBatchAxios(entityType, ids) {
  const { data } = await axios.get('/files/batch', {
    params: { entityType, ids },
    // Axios serializa arrays como ids=1&ids=2&ids=3 automáticamente
  });
  return data.data;
}
```

**Tipo TypeScript:**
```ts
async function getImagenesBatch(
  entityType: TipoCatalogoProducto,
  ids: number[]
): Promise<Record<number, ArchivoResponseDTO[]>> {
  const params = new URLSearchParams({ entityType });
  ids.forEach(id => params.append('ids', String(id)));

  const res = await fetch(`/files/batch?${params}`);
  if (!res.ok) throw await res.json();
  const json: ApiResponse<Record<string, ArchivoResponseDTO[]>> = await res.json();
  return json.data as unknown as Record<number, ArchivoResponseDTO[]>;
}
```

---

### 5. Obtener una imagen por ID — `GET /files/{idArchivo}`

```js
async function getImagen(idArchivo) {
  const res = await fetch(`/files/${idArchivo}`);
  if (!res.ok) throw await res.json();
  return (await res.json()).data; // ArchivoResponseDTO
}
```

---

### 6. Eliminar una imagen — `DELETE /files/{idArchivo}`

Elimina el archivo de Cloudinary **y** el registro en base de datos.  
Devuelve `204 No Content` sin cuerpo.

```js
async function eliminarImagen(idArchivo) {
  const res = await fetch(`/files/${idArchivo}`, { method: 'DELETE' });
  if (res.status !== 204) throw new Error('Error al eliminar');
}
```

---

### 7. Cambiar el orden de una imagen — `PATCH /files/orden`

Mueve un archivo a una posición concreta dentro de la galería de su entidad. El servidor rota automáticamente el resto de las imágenes para mantener la secuencia sin huecos.

**Body:** `application/json`

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `entityType` | `TipoCatalogoProducto` | Sí | Tipo de entidad a la que pertenece el archivo |
| `idArchivo` | `integer` | Sí | ID del archivo cuyo orden se quiere cambiar |
| `nuevoOrden` | `integer` | Sí | Posición destino (empieza en 1) |

**Ejemplos de rotación:**

| Caso | Antes | Después |
|------|-------|---------|
| Mover orden 2 → 1 | `[A₁, B₂, C₃]` | `[B₁, A₂, C₃]` |
| Mover orden 3 → 1 | `[A₁, B₂, C₃]` | `[C₁, A₂, B₃]` |
| Mover orden 1 → 3 | `[A₁, B₂, C₃]` | `[B₁, C₂, A₃]` |

> La imagen de la posición `nuevoOrden` **no desaparece**, solo cede su lugar y se desplaza un puesto.

```js
async function cambiarOrden(entityType, idArchivo, nuevoOrden) {
  const res = await fetch('/files/orden', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ entityType, idArchivo, nuevoOrden }),
  });
  if (!res.ok) throw await res.json();
  return (await res.json()).data; // ArchivoResponseDTO con el orden actualizado
}

// Ejemplo: subir la imagen con id 9 a la primera posición en la galería de COMIDA id 3
await cambiarOrden('COMIDA', 9, 1);
```

**Con Axios:**
```js
async function cambiarOrdenAxios(entityType, idArchivo, nuevoOrden) {
  const { data } = await axios.patch('/files/orden', { entityType, idArchivo, nuevoOrden });
  return data.data;
}
```

**Respuesta exitosa — 200:**
```json
{
  "status": 200,
  "message": "Orden actualizado correctamente",
  "data": {
    "idArchivo": 9,
    "entityType": "COMIDA",
    "idEntidad": 3,
    "pathArchivo": "https://res.cloudinary.com/.../imagen.jpg",
    "orden": 1,
    "creadoEn": "2026-07-20T14:30:00"
  }
}
```

**Tipo TypeScript:**
```ts
async function cambiarOrden(
  entityType: TipoCatalogoProducto,
  idArchivo: number,
  nuevoOrden: number
): Promise<ArchivoResponseDTO> {
  const res = await fetch('/files/orden', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ entityType, idArchivo, nuevoOrden }),
  });
  if (!res.ok) throw await res.json();
  const json: ApiResponse<ArchivoResponseDTO> = await res.json();
  return json.data;
}
```

**Errores posibles:**

| Status | Causa |
|--------|-------|
| 400 | `nuevoOrden` mayor al total de imágenes de la entidad |
| 400 | `entityType` no coincide con el tipo real del archivo |
| 404 | `idArchivo` no existe |

---

## Manejo de errores

El servidor devuelve errores con esta estructura:

```json
{
  "status": 400,
  "message": "MIME type no permitido: image/gif",
  "data": null
}
```

| Status | Causa |
|--------|-------|
| 400 | MIME no permitido, metadata inválida, tipo de entidad no soportado |
| 404 | `idEntidad` no existe en la BD, o `idArchivo` no existe |
| 500 | Fallo al eliminar en Cloudinary |

```js
try {
  const imagenes = await subirImagenes('COMIDA', 99, archivos);
} catch (err) {
  // err.message => "Producto no encontrado con id: 99"
  console.error(err.status, err.message);
}
```

---

## Patrón completo: galería con subida, eliminación y reordenamiento

```js
class GaleriaImagenes {
  constructor(entityType, idEntidad) {
    this.entityType = entityType;
    this.idEntidad = idEntidad;
  }

  async cargar() {
    return getImagenes(this.entityType, this.idEntidad);
  }

  async subir(archivos) {
    return subirImagenes(this.entityType, this.idEntidad, archivos);
  }

  async eliminar(idArchivo) {
    await eliminarImagen(idArchivo);
  }

  async reordenar(idArchivo, nuevoOrden) {
    return cambiarOrden(this.entityType, idArchivo, nuevoOrden);
  }
}

// Uso:
const galeria = new GaleriaImagenes('COMIDA', 1);
const imgs = await galeria.cargar();
// imgs[0].pathArchivo → URL lista para <img src>

// Subir imagen al primer lugar (ej. drag & drop en galería)
await galeria.reordenar(imgs[2].idArchivo, 1);
```

---

## Carpetas en Cloudinary por tipo

| entityType | Carpeta destino |
|------------|----------------|
| COMIDA | `cocina_rubi/comida` |
| COMPLEMENTO | `cocina_rubi/complemento` |
| DESAYUNO | `cocina_rubi/desayuno` |
| SNACK | `cocina_rubi/snack` |
| CHAROLA | `cocina_rubi/charola` |
| BEBIDA | `cocina_rubi/bebida` |
| POSTRE | `cocina_rubi/postre` |

---

## Notas

- El campo `meta` en el multipart **debe tener** `Content-Type: application/json` (usa `new Blob([...], { type: 'application/json' })`). Si lo mandas como string plano el servidor lo rechaza.
- Puedes subir varios archivos en una sola petición agregando múltiples entradas `files` al FormData.
- El `orden` se asigna automáticamente de forma incremental; la primera imagen de una entidad tendrá `orden: 1`.
- `PATCH /files/orden` aplica una **rotación**: no hay huecos ni colisiones, el servidor ajusta todos los demás archivos de la entidad en la misma transacción.
- Para mostrar la imagen optimizada puedes manipular la URL `pathArchivo` agregando transformaciones de Cloudinary, por ejemplo reemplazando `/upload/` por `/upload/w_400,q_auto/`.

---

## Optimización de ancho de banda (recomendado)

Las URLs que devuelve el backend apuntan a la imagen original (~150 KB promedio). Cloudinary permite transformarla en la URL antes de pasarla al `<img src>`, reduciendo el peso a ~40 KB sin perder calidad visual.

```js
function optimizarUrl(pathArchivo, ancho = 600) {
  return pathArchivo.replace('/upload/', `/upload/w_${ancho},q_auto,f_auto/`);
}
```

| Parámetro | Efecto |
|-----------|--------|
| `w_600` | Redimensiona al ancho indicado (en px) |
| `q_auto` | Cloudinary elige la calidad óptima automáticamente |
| `f_auto` | Sirve WebP en browsers que lo soportan, JPEG en el resto |

**Uso en el frontend:**

```js
const imagenes = await getImagenes('COMIDA', 1);

imagenes.forEach(img => {
  const el = document.createElement('img');
  el.src = optimizarUrl(img.pathArchivo, 600); // menú de escritorio
  // el.src = optimizarUrl(img.pathArchivo, 300); // miniaturas o móvil
  document.body.appendChild(el);
});
```

Con esta optimización el plan gratuito de Cloudinary (25 GB/mes) alcanza para aproximadamente **2,200 visitas únicas diarias** en vez de 550 sin optimizar.
