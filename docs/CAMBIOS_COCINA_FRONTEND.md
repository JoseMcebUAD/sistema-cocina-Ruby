# Cambios: Flujo COCINA — Clientes y Pedidos

## Nuevas tablas agregadas

| Tabla | Descripción |
|---|---|
| `registro_cliente` | Directorio reutilizable de clientes para órdenes desde cocina |
| `pedido_domicilio_cocina` | Extensión 1:1 de pedido — datos de entrega (snapshot) para COCINA+DOMICILIO |
| `pedido_cocina` | Extensión 1:1 de pedido — nombre del cliente para COCINA+PICK_UP o MOSTRADOR |

---

## Nuevos endpoints: `RegistroCliente`

Base URL: `/registro-cliente`

### GET /registro-cliente
Lista paginada del directorio de clientes.

**Query params:** `page` (default 0), `size` (default 10)

**Response 200:**
```json
{
  "timestamp": "2026-07-17T10:00:00",
  "status": 200,
  "message": "Registros de cliente obtenidos correctamente",
  "errorCode": null,
  "data": {
    "content": [
      {
        "idRegistroCliente": 1,
        "nombre": "María López",
        "telefono": "5551234567",
        "idRuta": 2,
        "nombreRuta": "Zona Norte",
        "direccion": "Calle Principal 42"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

### GET /registro-cliente/{id}
Obtiene un cliente por ID.

**Response 200:**
```json
{
  "timestamp": "2026-07-17T10:00:00",
  "status": 200,
  "message": "Registro de cliente encontrado",
  "errorCode": null,
  "data": {
    "idRegistroCliente": 1,
    "nombre": "María López",
    "telefono": "5551234567",
    "idRuta": 2,
    "nombreRuta": "Zona Norte",
    "direccion": "Calle Principal 42"
  }
}
```

**Response 404:**
```json
{
  "timestamp": "2026-07-17T10:00:00",
  "status": 404,
  "message": "Registro de cliente no encontrado con id: 99",
  "errorCode": null,
  "data": null
}
```

---

### GET /registro-cliente/buscar?telefono={texto}
Búsqueda por coincidencia parcial de teléfono, paginada.

**Query params:** `telefono` (requerido), `page` (default 0), `size` (default 10)

**Ejemplo:** `GET /registro-cliente/buscar?telefono=555&page=0&size=5`

**Response 200:**
```json
{
  "timestamp": "2026-07-17T10:00:00",
  "status": 200,
  "message": "Registros de cliente encontrados",
  "errorCode": null,
  "data": {
    "content": [
      {
        "idRegistroCliente": 1,
        "nombre": "María López",
        "telefono": "5551234567",
        "idRuta": null,
        "nombreRuta": null,
        "direccion": null
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 5
  }
}
```

---

### POST /registro-cliente
Crea un nuevo cliente en el directorio.

**Request body:**
```json
{
  "nombre": "María López",
  "telefono": "5551234567",
  "idRuta": 2,
  "direccion": "Calle Principal 42"
}
```

> `idRuta` y `direccion` son **opcionales** (null si el cliente no tiene domicilio frecuente).

**Response 201:**
```json
{
  "timestamp": "2026-07-17T10:00:00",
  "status": 201,
  "message": "Registro de cliente creado correctamente",
  "errorCode": null,
  "data": {
    "idRegistroCliente": 7,
    "nombre": "María López",
    "telefono": "5551234567",
    "idRuta": 2,
    "nombreRuta": "Zona Norte",
    "direccion": "Calle Principal 42"
  }
}
```

**Validaciones (400):**
- `nombre` no puede estar vacío
- `telefono` no puede estar vacío
- `idRuta` debe ser mayor a 0 (si se envía)

---

### PUT /registro-cliente/{id}
Actualiza un cliente existente. Misma estructura de body que el POST.

**Response 200:** igual al POST con los datos actualizados.

---

### DELETE /registro-cliente/{id}
Elimina un cliente del directorio.

**Response 204:** sin body.

**Response 404:** cliente no encontrado.

---

## Endpoints modificados: `Pedido`

### POST /pedido — Variante COCINA+PICK_UP o COCINA+MOSTRADOR

Campo nuevo: **`nombreCliente`** (opcional — si no se envía, nombreCliente queda null).

**Request:**
```json
{
  "metodoPagoPrincipal": "EFECTIVO",
  "tipoPedido": "PICK_UP",
  "pedidoCreadoDesde": "COCINA",
  "pagoCliente": 50.00,
  "comidas": [],
  "desayunos": [],
  "basicos": [],
  "productosCocina": [
    {
      "idProductoCocina": 3,
      "precioUnitario": 25.00,
      "cantidad": 1
    }
  ],
  "nombreCliente": "Juan Pérez",
  "saltarConfirmacion": true
}
```

**Response 201:**
```json
{
  "status": 201,
  "message": "...",
  "data": {
    "idPedido": 42,
    "metodoPagoPrincipal": "EFECTIVO",
    "metodoPagoSecundario": null,
    "tipoPedido": "PICK_UP",
    "pedidoCreadoDesde": "COCINA",
    "precioFinalOrden": 25.00,
    "pagoCliente": 50.00,
    "cambio": 25.00,
    "uuidCliente": null,
    "comidas": [],
    "desayunos": [],
    "basicos": [],
    "productosCocina": [
      {
        "idProductoCocinaPedido": 1,
        "idProductoCocina": 3,
        "nombreProducto": "Agua Mineral",
        "precioUnitario": 25.00,
        "cantidad": 1
      }
    ],
    "domicilio": null,
    "domicilioCocina": null,
    "pedidoCocina": {
      "idPedido": 42,
      "nombreCliente": "Juan Pérez"
    }
  }
}
```

---

### POST /pedido — Variante COCINA+DOMICILIO

Campo nuevo: **`pedidoDomicilioCocina`** (requerido cuando `pedidoCreadoDesde=COCINA` y `tipoPedido=DOMICILIO`).

> **NOTA:** Para COCINA+DOMICILIO usar `pedidoDomicilioCocina`, NO `domicilio`. El campo `tarifa` es snapshot: se guarda al momento del pedido aunque la tarifa de la ruta cambie después.

**Request:**
```json
{
  "metodoPagoPrincipal": "EFECTIVO",
  "metodoPagoSecundario": "TRANSFERENCIA",
  "tipoPedido": "DOMICILIO",
  "pedidoCreadoDesde": "COCINA",
  "pagoCliente": 120.00,
  "comidas": [
    {
      "idComida": 5,
      "precioUnitario": 75.00,
      "tamanoPorcion": "NORMAL",
      "complementos": []
    }
  ],
  "desayunos": [],
  "basicos": [],
  "productosCocina": [],
  "pedidoDomicilioCocina": {
    "idRegistroCliente": 7,
    "idRuta": 2,
    "domicilio": "Calle Principal 42, Int 3",
    "tarifa": 30.00
  },
  "saltarConfirmacion": true
}
```

**Response 201:**
```json
{
  "status": 201,
  "data": {
    "idPedido": 43,
    "metodoPagoPrincipal": "EFECTIVO",
    "metodoPagoSecundario": "TRANSFERENCIA",
    "tipoPedido": "DOMICILIO",
    "pedidoCreadoDesde": "COCINA",
    "precioFinalOrden": 105.00,
    "pagoCliente": 120.00,
    "cambio": 15.00,
    "uuidCliente": null,
    "comidas": [ ... ],
    "desayunos": [],
    "basicos": [],
    "productosCocina": [],
    "domicilio": null,
    "domicilioCocina": {
      "idPedido": 43,
      "idRegistroCliente": 7,
      "nombreCliente": "María López",
      "telefono": "5551234567",
      "idRuta": 2,
      "nombreRuta": "Zona Norte",
      "domicilio": "Calle Principal 42, Int 3",
      "precioTarifa": 30.00
    },
    "pedidoCocina": null
  }
}
```

---

### POST /pedido — Variante WEB+DOMICILIO (sin cambios)

Sigue usando `domicilio` (no `domicilioCocina`).

**Request:**
```json
{
  "metodoPagoPrincipal": "TARJETA",
  "tipoPedido": "DOMICILIO",
  "pedidoCreadoDesde": "WEB",
  "uuidCliente": "uuid-del-cliente",
  "comidas": [ ... ],
  "desayunos": [],
  "basicos": [],
  "productosCocina": [],
  "domicilio": {
    "idRuta": 2,
    "direccion": "Av. Reforma 100",
    "codigo": "COD-ABC"
  }
}
```

---

## Reglas de validación (errores 400)

| Situación | Error |
|---|---|
| `pedidoCreadoDesde=COCINA` + `tipoPedido=DOMICILIO` sin `pedidoDomicilioCocina` | `"Un pedido COCINA a domicilio requiere los datos de entrega (campo 'pedidoDomicilioCocina')"` |
| `pedidoCreadoDesde=COCINA` + `tipoPedido=DOMICILIO` con `domicilio` | `"Un pedido COCINA a domicilio no debe incluir el campo 'domicilio' (use 'pedidoDomicilioCocina')"` |
| `pedidoCreadoDesde=WEB` + `tipoPedido=DOMICILIO` sin `domicilio` | `"Un pedido WEB a domicilio requiere los datos de entrega (campo 'domicilio')"` |
| `pedidoCreadoDesde=WEB` + `tipoPedido=DOMICILIO` con `pedidoDomicilioCocina` | `"Un pedido WEB a domicilio no debe incluir el campo 'pedidoDomicilioCocina'"` |
| Pedido sin ningún producto | `"El pedido debe incluir al menos un producto"` |

**Ejemplo de error 400:**
```json
{
  "timestamp": "2026-07-17T10:00:00",
  "status": 400,
  "message": "Un pedido COCINA a domicilio requiere los datos de entrega (campo 'pedidoDomicilioCocina')",
  "errorCode": "VALIDACION",
  "data": null
}
```

---

## Campos del modelo `Pedido` — respuesta completa

```json
{
  "idPedido": 0,
  "metodoPagoPrincipal": "EFECTIVO | TARJETA | TRANSFERENCIA",
  "metodoPagoSecundario": "EFECTIVO | TARJETA | TRANSFERENCIA | null",
  "tipoPedido": "MOSTRADOR | PICK_UP | DOMICILIO",
  "fechaExpedicionPedido": "2026-07-17T10:30:00",
  "pedidoCreadoDesde": "COCINA | WEB",
  "precioFinalOrden": 0.00,
  "pagoCliente": 0.00,
  "cambio": 0.00,
  "uuidCliente": "null | uuid-string",
  "comidas": [],
  "desayunos": [],
  "basicos": [],
  "productosCocina": [],
  "domicilio": null,
  "domicilioCocina": null,
  "pedidoCocina": null
}
```

> `domicilio`, `domicilioCocina` y `pedidoCocina` son mutuamente excluyentes y dependen de `pedidoCreadoDesde` + `tipoPedido`.
