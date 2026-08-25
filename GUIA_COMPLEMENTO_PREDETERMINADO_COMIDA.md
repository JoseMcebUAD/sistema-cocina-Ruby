# Complemento Predeterminado de Comida

Permite asignar complementos que se incluyen **por defecto** al agregar una comida a un pedido. Por ejemplo: la comida "Pollo en salsa" puede tener predeterminados "Arroz (1)" y "Frijoles (1)".

> **Nota para el backend:** El `ComplementoPredeterminadoComidaService` ya está implementado. Falta crear el controlador `ComplementoPredeterminadoComidaController` con las rutas documentadas aquí antes de que el frontend pueda consumir estos endpoints.

---

## Rutas

Base: `/comida/{idComida}/complemento-predeterminado`

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/comida/{idComida}/complemento-predeterminado` | Lista los complementos predeterminados de la comida |
| `POST` | `/comida/{idComida}/complemento-predeterminado` | Asigna un complemento predeterminado a la comida |
| `DELETE` | `/comida/{idComida}/complemento-predeterminado/{id}` | Elimina un complemento predeterminado por su ID |

> Los complementos predeterminados también se incluyen en la respuesta de `GET /comida/{id}` dentro del campo `complementosPredeterminados`. En ese caso la estructura es la de la **entidad** (no el DTO), por lo que el complemento aparece como objeto anidado:
> ```json
> "complementosPredeterminados": [
>   {
>     "idComplementoPredeterminadoComida": 1,
>     "complemento": {
>       "idComplemento": 10,
>       "nombreComplemento": "Arroz",
>       ...
>     },
>     "cantidad": 1
>   }
> ]
> ```

---

## GET — Listar complementos predeterminados

```
GET /comida/3/complemento-predeterminado
Authorization: Bearer <token>
```

### Respuesta exitosa `200`

```json
{
  "timestamp": "2026-08-14T12:00:00",
  "status": 200,
  "message": "Complementos predeterminados obtenidos correctamente",
  "data": [
    {
      "idComplementoPredeterminadoComida": 1,
      "idComplemento": 10,
      "nombreComplemento": "Arroz",
      "cantidad": 1
    },
    {
      "idComplementoPredeterminadoComida": 2,
      "idComplemento": 11,
      "nombreComplemento": "Frijoles",
      "cantidad": 1
    }
  ]
}
```

---

## POST — Asignar complemento predeterminado

```
POST /comida/3/complemento-predeterminado
Authorization: Bearer <token>
Content-Type: application/json
```

### Body

```json
{
  "idComplemento": 10,
  "cantidad": 1
}
```

| Campo | Tipo | Requerido | Reglas |
|-------|------|-----------|--------|
| `idComplemento` | `integer` | Sí | Debe existir; mayor que 0 |
| `cantidad` | `integer` | Sí | Mayor o igual a 0 |

### Respuesta exitosa `201`

```json
{
  "timestamp": "2026-08-14T12:00:00",
  "status": 201,
  "message": "Complemento predeterminado asignado correctamente",
  "data": {
    "idComplementoPredeterminadoComida": 5,
    "idComplemento": 10,
    "nombreComplemento": "Arroz",
    "cantidad": 1
  }
}
```

### Errores posibles

| Código HTTP | Escenario |
|-------------|-----------|
| `404 Not Found` | La comida con `{idComida}` no existe |
| `404 Not Found` | El complemento con `idComplemento` no existe |
| `409 Conflict` | El complemento ya está asignado como predeterminado a esa comida |
| `400 Bad Request` | Campos faltantes o valores inválidos |

---

## DELETE — Eliminar complemento predeterminado

```
DELETE /comida/3/complemento-predeterminado/5
Authorization: Bearer <token>
```

El `5` es el `idComplementoPredeterminadoComida` devuelto por el POST o GET.

### Respuesta exitosa `204`

Sin body.

### Errores posibles

| Código HTTP | Escenario |
|-------------|-----------|
| `404 Not Found` | No existe un predeterminado con ese ID |

---

## Restricciones de negocio

- La combinación `(idComida, idComplemento)` debe ser **única**: no se puede asignar el mismo complemento dos veces a la misma comida.
- Si se elimina la comida o el complemento, el predeterminado se borra en cascada automáticamente (configurado en la base de datos).
- `cantidad` puede ser `0` (p. ej. para indicar "incluido sin porción extra").

---

## Flujo típico del frontend

```
1. Usuario abre el formulario de edición de una comida.
2. GET /comida/{id} → campo `complementosPredeterminados` muestra los actuales.
3. Usuario agrega un complemento:
   POST /comida/{id}/complemento-predeterminado  { idComplemento, cantidad }
4. Usuario elimina un complemento:
   DELETE /comida/{id}/complemento-predeterminado/{idComplementoPredeterminadoComida}
```

Para poblar el selector de complementos disponibles usar:
```
GET /complemento/disponibles
```
