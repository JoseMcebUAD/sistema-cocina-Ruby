# Vista/Resumen de Pedidos — Guía para Frontend

Documentación del controlador [VistaResumenPedidoController.java](src/main/java/com/cocinarubi/presentation/controller/VistaResumenPedidoController.java), pensada para que el equipo de frontend pueda conectarse, consumir y renderizar los datos de la vista consolidada de pedidos.

## Base URL

```
{HOST}/vista-resumen-pedido
```

Todas las respuestas se envuelven en el wrapper estándar `ApiResponse<T>`:

```json
{
  "timestamp": "2026-07-20T12:34:56",
  "status": 200,
  "message": "Vista de pedidos obtenida correctamente",
  "errorCode": null,
  "data": { ... }
}
```

> `timestamp` está en zona horaria `America/Merida`. Los campos `null` se omiten en la respuesta.

---

## Endpoints

### 1. `GET /vista-resumen-pedido`

Devuelve la vista consolidada de pedidos (todos los orígenes y tipos) **paginada** y con filtros opcionales.

#### Query params

| Parámetro            | Tipo             | Requerido | Default | Descripción                                                                                     |
|----------------------|------------------|-----------|---------|-------------------------------------------------------------------------------------------------|
| `desde`              | `LocalDateTime`  | No        | —       | Fecha/hora inicial del rango (ISO 8601, ej: `2026-07-01T00:00:00`).                              |
| `hasta`              | `LocalDateTime`  | No        | —       | Fecha/hora final del rango (ISO 8601, ej: `2026-07-20T23:59:59`).                                |
| `tipoPedido`         | `TipoPedido`     | No        | —       | Filtra por tipo. Valores: `PICK_UP`, `DOMICILIO`, `MOSTRADOR`.                                   |
| `pedidoCreadoDesde`  | `PedidoCreadoDesde` | No     | —       | Filtra por origen. Valores: `COCINA`, `WEB`.                                                     |
| `page`               | `int`            | No        | `0`     | Índice de página (base 0).                                                                       |
| `size`               | `int`            | No        | `20`    | Tamaño de página.                                                                                |

#### Ejemplo de request

```
GET /vista-resumen-pedido?desde=2026-07-01T00:00:00&hasta=2026-07-20T23:59:59&tipoPedido=DOMICILIO&page=0&size=20
```

#### Respuesta `200 OK`

`data` es un `Page<VistaResumenPedidoResponseDTO>` de Spring:

```json
{
  "timestamp": "2026-07-20T12:34:56",
  "status": 200,
  "message": "Vista de pedidos obtenida correctamente",
  "data": {
    "content": [
      {
        "idPedido": 101,
        "impreso": true,
        "nombreCliente": "Juan Pérez",
        "metodoPagoPrincipal": "EFECTIVO",
        "metodoPagoSecundario": null,
        "tipoPedido": "DOMICILIO",
        "pedidoCreadoDesde": "COCINA",
        "fechaExpedicionPedido": "2026-07-20T13:15:00",
        "precioFinalOrden": 250.00,
        "pagoClientePrincipal": 300.00,
        "ruta": "Ruta Centro",
        "domicilio": "Calle 60 #123, Mérida",
        "precioTarifa": 30.00
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": { "sorted": false, "unsorted": true, "empty": true },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 57,
    "totalPages": 3,
    "last": false,
    "first": true,
    "number": 0,
    "numberOfElements": 20,
    "size": 20,
    "sort": { "sorted": false, "unsorted": true, "empty": true },
    "empty": false
  }
}
```

---

### 2. `GET /vista-resumen-pedido/metricas`

Devuelve **la misma vista paginada** más **métricas agregadas** calculadas sobre el rango/filtros aplicados (no solo sobre la página).

#### Query params

Idénticos a `GET /vista-resumen-pedido`.

#### Ejemplo de request

```
GET /vista-resumen-pedido/metricas?desde=2026-07-01T00:00:00&hasta=2026-07-20T23:59:59&page=0&size=20
```

#### Respuesta `200 OK`

`data` es un `VistaResumenPedidoConMetricasResponseDTO`:

```json
{
  "timestamp": "2026-07-20T12:34:56",
  "status": 200,
  "message": "Vista y métricas de pedidos obtenidas correctamente",
  "data": {
    "pedidos": {
      "content": [ /* array de VistaResumenPedidoResponseDTO (mismo shape que el endpoint anterior) */ ],
      "totalElements": 57,
      "totalPages": 3,
      "number": 0,
      "size": 20,
      "first": true,
      "last": false,
      "empty": false
    },
    "cantidadTotal": 57,
    "cantidadImpresos": 40,
    "cantidadNoImpresos": 17,
    "ingresoTotal": 14250.00,
    "ingresoEfectivo": 8000.00,
    "ingresoTransferencia": 3250.00,
    "ingresoTarjeta": 3000.00
  }
}
```

---

## Modelos de datos

### `VistaResumenPedidoResponseDTO`

| Campo                    | Tipo               | Notas                                                                 |
|--------------------------|--------------------|-----------------------------------------------------------------------|
| `idPedido`               | `Integer`          | ID del pedido.                                                        |
| `impreso`                | `Boolean`          | `true` si el ticket ya fue impreso.                                   |
| `nombreCliente`          | `String`           | Puede ser `null` para pedidos de mostrador.                           |
| `metodoPagoPrincipal`    | `MetodoPago`       | `TARJETA`, `EFECTIVO`, `TRANSFERENCIA`.                               |
| `metodoPagoSecundario`   | `MetodoPago`       | Opcional (pagos mixtos).                                              |
| `tipoPedido`             | `TipoPedido`       | `PICK_UP`, `DOMICILIO`, `MOSTRADOR`.                                  |
| `pedidoCreadoDesde`      | `PedidoCreadoDesde`| `COCINA`, `WEB`.                                                      |
| `fechaExpedicionPedido`  | `LocalDateTime`    | Formato: `yyyy-MM-dd'T'HH:mm:ss`.                                      |
| `precioFinalOrden`       | `BigDecimal`       | Total cobrado al cliente.                                             |
| `pagoClientePrincipal`   | `BigDecimal`       | Monto entregado por el cliente con el método principal.               |
| `ruta`                   | `String`           | Solo aplica a `DOMICILIO`; puede ser `null` en otros tipos.           |
| `domicilio`              | `String`           | Dirección de entrega; puede ser `null` fuera de `DOMICILIO`.          |
| `precioTarifa`           | `BigDecimal`       | Tarifa de envío del pedido a domicilio; puede ser `null`.             |

### `VistaResumenPedidoConMetricasResponseDTO`

| Campo                  | Tipo                                    | Notas                                                        |
|------------------------|-----------------------------------------|--------------------------------------------------------------|
| `pedidos`              | `Page<VistaResumenPedidoResponseDTO>`   | Página de resultados (misma estructura que el endpoint base).|
| `cantidadTotal`        | `long`                                  | Total de pedidos en el rango filtrado.                       |
| `cantidadImpresos`     | `long`                                  | Pedidos con `impreso = true`.                                |
| `cantidadNoImpresos`   | `long`                                  | Pedidos con `impreso = false`.                               |
| `ingresoTotal`         | `BigDecimal`                            | Suma de `precioFinalOrden`.                                  |
| `ingresoEfectivo`      | `BigDecimal`                            | Ingresos cobrados en efectivo.                               |
| `ingresoTransferencia` | `BigDecimal`                            | Ingresos cobrados por transferencia.                         |
| `ingresoTarjeta`       | `BigDecimal`                            | Ingresos cobrados con tarjeta.                               |

### Enums

```ts
type TipoPedido = "PICK_UP" | "DOMICILIO" | "MOSTRADOR";
type PedidoCreadoDesde = "COCINA" | "WEB";
type MetodoPago = "TARJETA" | "EFECTIVO" | "TRANSFERENCIA";
```

---

## Tipos TypeScript sugeridos

```ts
export interface ApiResponse<T> {
  timestamp: string;          // "yyyy-MM-dd'T'HH:mm:ss"
  status: number;
  message: string;
  errorCode?: string | null;
  data: T;
}

export interface VistaResumenPedidoResponseDTO {
  idPedido: number;
  impreso: boolean;
  nombreCliente: string | null;
  metodoPagoPrincipal: MetodoPago;
  metodoPagoSecundario: MetodoPago | null;
  tipoPedido: TipoPedido;
  pedidoCreadoDesde: PedidoCreadoDesde;
  fechaExpedicionPedido: string; // ISO local sin zona
  precioFinalOrden: number;
  pagoClientePrincipal: number;
  ruta: string | null;
  domicilio: string | null;
  precioTarifa: number | null;
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;      // página actual (0-based)
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  numberOfElements: number;
}

export interface VistaResumenPedidoConMetricasResponseDTO {
  pedidos: SpringPage<VistaResumenPedidoResponseDTO>;
  cantidadTotal: number;
  cantidadImpresos: number;
  cantidadNoImpresos: number;
  ingresoTotal: number;
  ingresoEfectivo: number;
  ingresoTransferencia: number;
  ingresoTarjeta: number;
}
```

---

## Ejemplos de consumo (fetch)

### Listado paginado con filtros

```ts
const params = new URLSearchParams({
  desde: "2026-07-01T00:00:00",
  hasta: "2026-07-20T23:59:59",
  tipoPedido: "DOMICILIO",
  page: "0",
  size: "20",
});

const res = await fetch(`${API}/vista-resumen-pedido?${params}`);
const json: ApiResponse<SpringPage<VistaResumenPedidoResponseDTO>> = await res.json();

const pedidos = json.data.content;
const totalPaginas = json.data.totalPages;
```

### Vista + métricas agregadas

```ts
const params = new URLSearchParams({
  desde: "2026-07-01T00:00:00",
  hasta: "2026-07-20T23:59:59",
  page: "0",
  size: "20",
});

const res = await fetch(`${API}/vista-resumen-pedido/metricas?${params}`);
const json: ApiResponse<VistaResumenPedidoConMetricasResponseDTO> = await res.json();

const { pedidos, ingresoTotal, cantidadImpresos } = json.data;
```

---

## Notas y consideraciones

- **Formato de fechas:** los `LocalDateTime` deben enviarse **sin** zona horaria y con segundos (`yyyy-MM-dd'T'HH:mm:ss`). Enviar `Z` o un offset (`-05:00`) puede fallar la deserialización.
- **Filtros combinables:** todos los filtros son opcionales y se aplican con `AND`. Sin parámetros, devuelve todo el histórico paginado.
- **Paginación:** `page` es **base 0** (típico Spring). Si en el frontend se muestra base 1, restar 1 al enviar.
- **Métricas vs página:** en `/metricas` los agregados (`cantidadTotal`, `ingresoTotal`, etc.) se calculan sobre **todos** los pedidos que cumplen el filtro, no solo sobre la página actual.
- **Campos nulos:** `ruta`, `domicilio` y `precioTarifa` solo tienen sentido cuando `tipoPedido = DOMICILIO`; el frontend debe manejarlos como opcionales.
- **Wrapper `ApiResponse`:** siempre acceder a los datos vía `response.data`. En caso de error, revisar `status`, `message` y `errorCode`.
