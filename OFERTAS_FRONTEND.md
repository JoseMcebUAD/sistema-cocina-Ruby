# Módulo de Ofertas — Guía para el Frontend

> Commits: `83b41dd` · `aac5687` · `1195065`  
> Fecha: 2026-10-14

---

## Resumen

Se incorporó un módulo completo de **ofertas de precio especial** (combos, packs, etc.).  
Una oferta define una regla de productos requeridos y un precio fijo al cumplirla.

**Comportamiento por canal:**

| Canal | Detección de ofertas |
|---|---|
| **WEB** | El backend las detecta y aplica automáticamente (greedy). No se envía nada extra. |
| **COCINA** | El operador indica explícitamente qué oferta aplicar y qué productos entran en ella. |

---

## Enums nuevos

### `ModoMatchOferta`
Define cómo el motor empareja productos del pedido con una línea de regla.

| Valor | `idReferencia` | Notas |
|---|---|---|
| `COMIDA_ESPECIFICA` | `idComida` | |
| `COMIDA_POR_TIPO` | `null` | Usar `tipoComidaFiltro` (`FIJA` \| `ESPECIAL`) |
| `PRODUCTO_ESPECIFICO` | `idProductoCocina` | |
| `PRODUCTO_POR_CATEGORIA` | `idCategoria` | |
| `PRODUCTO_POR_SUBCATEGORIA` | `idSubcategoria` | |
| `PRODUCTO_GENERAL` | `null` | Cualquier ProductoCocina |
| `BASICO_ESPECIFICO` | `idBasico` | |
| `BASICO_GENERAL` | `null` | Cualquier Básico |
| `COMBO_ESPECIFICO` | `idCombo` | |
| `COMBO_GENERAL` | `null` | Cualquier Combo |

### `TipoEntidadOferta`
Tipos de productos que pueden participar en una oferta.  
`COMIDA` · `PRODUCTO_COCINA` · `BASICO` · `COMBO`  
_(Desayunos excluidos en v1)_

---

## Endpoints de catálogo de ofertas

Base: `/oferta`  
Autenticación: JWT requerido.

### `GET /oferta`
Lista todas las ofertas registradas.

**Respuesta `200`:**
```json
{
  "codigo": 200,
  "mensaje": "Ofertas obtenidas correctamente",
  "datos": [ /* OfertaResponseDTO[] */ ]
}
```

---

### `GET /oferta/{id}`
Obtiene una oferta por ID.

**Respuesta `200`:**
```json
{
  "codigo": 200,
  "mensaje": "Oferta encontrada",
  "datos": { /* OfertaResponseDTO */ }
}
```

---

### `POST /oferta`
Crea una nueva oferta.

**Body `OfertaRequestDTO`:**
```json
{
  "nombre": "Combo Familiar",
  "descripcion": "2 comidas + 1 bebida",
  "precioOferta": 85.00,
  "prioridad": 10,
  "activo": true,
  "fechaInicio": "2026-11-01",
  "fechaFin": "2026-12-31",
  "tiposPedidoRestriccion": ["MOSTRADOR", "PICK_UP"],
  "lineas": [
    {
      "modoMatch": "COMIDA_GENERAL",
      "idReferencia": null,
      "tipoComidaFiltro": null,
      "cantidad": 2
    },
    {
      "modoMatch": "PRODUCTO_POR_CATEGORIA",
      "idReferencia": 5,
      "tipoComidaFiltro": null,
      "cantidad": 1
    }
  ]
}
```

**Notas de validación:**
- `nombre`: requerido, máx. 150 caracteres.
- `descripcion`: opcional, máx. 400 caracteres.
- `precioOferta`: requerido, > 0.00.
- `prioridad`: entero (default `0`). Mayor valor = se intenta antes en el greedy.
- `activo`: boolean (default `true`).
- `fechaInicio` / `fechaFin`: `"YYYY-MM-DD"` o `null` (sin restricción).
- `tiposPedidoRestriccion`: array de `TipoPedido`. **Vacío = aplica a todos los tipos.**
- `lineas`: requerido, al menos 1 elemento.
  - `modoMatch`: requerido.
  - `idReferencia`: requerido solo para modos `_ESPECIFICO`, `_POR_CATEGORIA`, `_POR_SUBCATEGORIA`.
  - `tipoComidaFiltro`: solo cuando `modoMatch = COMIDA_POR_TIPO`. Valores: `FIJA` | `ESPECIAL`.
  - `cantidad`: entero ≥ 1 (default `1`).

**Respuesta `201`:**
```json
{
  "codigo": 201,
  "mensaje": "Oferta creada correctamente",
  "datos": { /* OfertaResponseDTO */ }
}
```

---

### `PUT /oferta/{id}`
Reemplaza completamente una oferta existente.  
Mismo body que `POST`. Respuesta `200`.

---

### `PATCH /oferta/{id}/toggle-activo`
Activa o desactiva una oferta sin enviar body.

**Respuesta `200`:**
```json
{
  "codigo": 200,
  "mensaje": "Estado de oferta actualizado",
  "datos": { /* OfertaResponseDTO con activo actualizado */ }
}
```

---

### `DELETE /oferta/{id}`
Elimina una oferta.

| Parámetro query | Tipo | Default | Descripción |
|---|---|---|---|
| `saltarConfirmacion` | boolean | `false` | Si `true` omite la validación de uso previo |

**Respuesta `204` (sin cuerpo).**

---

## Shapes de respuesta

### `OfertaResponseDTO`
```json
{
  "idOferta": 1,
  "nombre": "Combo Familiar",
  "descripcion": "2 comidas + 1 bebida",
  "precioOferta": 85.00,
  "prioridad": 10,
  "activo": true,
  "fechaInicio": "2026-11-01",
  "fechaFin": "2026-12-31",
  "tiposPedidoRestriccion": ["MOSTRADOR"],
  "lineas": [
    {
      "idOfertaLinea": 3,
      "modoMatch": "COMIDA_ESPECIFICA",
      "idReferencia": 7,
      "tipoComidaFiltro": null,
      "cantidad": 2
    }
  ]
}
```

---

## Cambios en el endpoint de Menú Web

`GET /menu-web` ahora incluye el campo **`ofertas`** en su respuesta:

```json
{
  "comidas": [...],
  "basicos": [...],
  "desayunos": [...],
  "combos": [...],
  "categorias": [...],
  "complementos": [...],
  "anuncios": [...],
  "ofertas": [
    {
      "idOferta": 1,
      "nombre": "Combo Familiar",
      "descripcion": "2 comidas + 1 bebida",
      "precioOferta": 85.00,
      "tiposPedidoRestriccion": ["MOSTRADOR"],
      "lineas": [
        {
          "cantidad": 2,
          "descripcion": "Cualquier comida del menú"
        },
        {
          "cantidad": 1,
          "descripcion": "Refresco (categoría Bebidas)"
        }
      ]
    }
  ]
}
```

> Las `lineas` de `OfertaMenuDTO` ya vienen con `descripcion` resuelta en texto legible.  
> **No expone `modoMatch` ni `idReferencia`** — eso queda en el admin.  
> Usar esta lista para mostrar la tarjeta de oferta en el carrito del cliente.

---

## Cambios en Pedidos

### Request — canal WEB
No cambia nada. El backend detecta y aplica las ofertas automáticamente.

### Request — canal COCINA
`PedidoRequestDTO` incluye el campo opcional **`ofertas`**:

```json
{
  "tipoPedido": "MOSTRADOR",
  "comidas": [...],
  "ofertas": [
    {
      "idOferta": 1,
      "lineas": [
        { "tipoEntidad": "COMIDA", "idProducto": 7 },
        { "tipoEntidad": "COMIDA", "idProducto": 12 },
        { "tipoEntidad": "PRODUCTO_COCINA", "idProducto": 3 }
      ]
    }
  ]
}
```

- `idOferta`: ID de la oferta a aplicar.
- `lineas`: productos del pedido que entran en la oferta.
  - `tipoEntidad`: `COMIDA` | `PRODUCTO_COCINA` | `BASICO` | `COMBO`
  - `idProducto`: ID del producto en el catálogo (idComida, idProductoCocina, etc.)

---

### Response — todos los canales
`PedidoResponseDTO` ahora incluye **`ofertasAplicadas`**:

```json
{
  "idPedido": 99,
  "total": 85.00,
  "ofertasAplicadas": [
    {
      "idOfertaPedido": 1,
      "idOferta": 1,
      "nombreOferta": "Combo Familiar",
      "precioAplicado": 85.00,
      "ahorro": 18.00,
      "productos": [
        {
          "tipoEntidad": "COMIDA",
          "idProductoCatalogo": 7,
          "nombreProducto": "Pechuga a la plancha",
          "cantidadConsumida": 1,
          "precioNormalConsumido": 55.00
        },
        {
          "tipoEntidad": "PRODUCTO_COCINA",
          "idProductoCatalogo": 3,
          "nombreProducto": "Refresco 600ml",
          "cantidadConsumida": 1,
          "precioNormalConsumido": 48.00
        }
      ]
    }
  ]
}
```

| Campo | Descripción |
|---|---|
| `idOfertaPedido` | ID del registro de aplicación |
| `idOferta` | ID de la oferta del catálogo |
| `nombreOferta` | Nombre snapshot al momento del pedido |
| `precioAplicado` | Precio cobrado por la oferta |
| `ahorro` | Diferencia vs. precios normales individuales |
| `productos` | Detalle de qué productos se consumieron en la oferta |
| `precioNormalConsumido` | Precio normal total de esa línea (unitario × cantidad) |

---

## Regla importante: descuento por volumen

Las comidas que entran en una oferta **no cuentan** para el descuento automático por volumen (≥ 12 unidades). El backend lo maneja internamente; el frontend no necesita hacer ningún cálculo extra.

---

## Flujo sugerido para el carrito WEB

```
1. GET /menu-web
   └─ guardar ofertas[] en estado global

2. Usuario agrega productos al carrito
   └─ mostrar tarjetas de oferta disponibles según tiposPedidoRestriccion

3. POST /pedido  (sin campo ofertas)
   └─ backend aplica greedy automáticamente

4. Mostrar resumen con ofertasAplicadas[] en la respuesta
   └─ mostrar "ahorraste $X" por cada oferta aplicada
```

---

## Flujo sugerido para el panel COCINA

```
1. Operador selecciona productos del pedido
2. Sistema sugiere ofertas aplicables (consultar GET /oferta?activo=true)
3. Operador confirma o ajusta manualmente las ofertas
4. POST /pedido con campo ofertas[] indicando asignación explícita
5. Mostrar ofertasAplicadas[] en el ticket
```
