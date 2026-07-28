# WebSocket — Pedidos WEB sin imprimir

Documenta los endpoints STOMP del módulo de pedidos WEB en tiempo real. Permite que la cocina vea los pedidos online apenas llegan, sin necesidad de recargar la página.

---

## Endpoint de conexión HTTP

| Campo | Valor |
|---|---|
| URL | `ws://<host>/todos` |
| Protocolo | STOMP sobre WebSocket |
| Autenticación | Header `Authorization: Bearer <jwt>` en el frame STOMP **CONNECT** (no en el handshake HTTP) |

El handshake HTTP en `/todos` está **permitido sin token** en Spring Security. La autenticación real ocurre en el frame STOMP CONNECT dentro de `JwtChannelInterceptor`.

---

## Roles autorizados

| Rol | Puede conectarse | Puede suscribirse |
|---|---|---|
| `ROLE_JEFA_COCINA` | Sí | Sí |
| `ROLE_COCINA` | Sí | Sí |
| Sin token / rol incorrecto | No (rechazado en CONNECT) | No |

---

## Destinos STOMP

### `/pedido-web/lista`

**Para qué sirve:** Mantiene al cliente sincronizado con la lista completa de pedidos WEB que aún no han sido impresos.

| Acción | Descripción |
|---|---|
| **Snapshot al suscribirse** | En el momento en que el cliente hace `SUBSCRIBE`, recibe inmediatamente la lista actual (sin esperar el próximo evento). |
| **Broadcast automático** | Cada vez que llega un pedido WEB nuevo, se imprime uno, o se elimina uno sin imprimir, todos los suscriptores reciben la lista actualizada. |

**Tipo de payload:** `PedidoResponseDTO[]` (array JSON con el detalle completo de cada pedido)

---

### `/pedido-web/contador`

**Para qué sirve:** Muestra cuántos pedidos WEB están pendientes de impresión. Útil para badges o indicadores numéricos en la UI sin parsear la lista completa.

| Acción | Descripción |
|---|---|
| **Snapshot al suscribirse** | El cliente recibe el conteo actual en el instante de la suscripción. |
| **Broadcast automático** | Se actualiza en los mismos eventos que `/pedido-web/lista` (siempre se envían juntos). |

**Tipo de payload:** `long` (número entero)

---

## Prefijos de destino

| Prefijo | Uso |
|---|---|
| `/app/...` | Mensajes del cliente hacia el servidor (no se usa en este módulo) |
| `/pedido-web/...` | Canal del broker → cliente (broadcasts y snapshots) |

---

## Triggers de broadcast

Un broadcast a ambos destinos se dispara automáticamente tras confirmar cualquiera de estas operaciones:

| Operación REST | Condición para disparar |
|---|---|
| `POST /pedido` | `pedidoCreadoDesde = WEB` |
| `PATCH /pedido/{id}/marcar-impreso` | El pedido es de tipo WEB |
| `DELETE /pedido/{id}` | El pedido era WEB **y** no estaba impreso |

> El broadcast ocurre **después del commit** de la transacción (`@TransactionalEventListener(AFTER_COMMIT)`), garantizando que el cliente nunca ve datos de una transacción que pueda revertirse.

---

## Flujo desde el cliente

```
1. Conectar al WebSocket
   CONNECT
   Authorization: Bearer eyJ...

2. Suscribirse a la lista (recibe snapshot inmediato)
   SUBSCRIBE /pedido-web/lista

3. Suscribirse al contador (recibe snapshot inmediato)
   SUBSCRIBE /pedido-web/contador

4. Esperar broadcasts automáticos
   ← MESSAGE /pedido-web/lista    [{ id: 1, ... }, { id: 2, ... }]
   ← MESSAGE /pedido-web/contador 2
```

---

## Archivos involucrados

| Archivo | Responsabilidad |
|---|---|
| [config/ws/WebSocketConfig.java](../src/main/java/com/cocinarubi/config/ws/WebSocketConfig.java) | Configura broker, endpoint `/todos` y registra el interceptor JWT |
| [config/ws/TomcatWebSocketConfig.java](../src/main/java/com/cocinarubi/config/ws/TomcatWebSocketConfig.java) | Registra el contenedor WebSocket de Tomcat para compatibilidad con OkHttp |
| [security/ws/JwtChannelInterceptor.java](../src/main/java/com/cocinarubi/presentation/security/ws/JwtChannelInterceptor.java) | Valida JWT en CONNECT y verifica rol en SUBSCRIBE/SEND |
| [event/ws/PedidoWebActualizadoEvent.java](../src/main/java/com/cocinarubi/event/ws/PedidoWebActualizadoEvent.java) | Evento de dominio que dispara el broadcast |
| [event/ws/PedidoWebSocketListener.java](../src/main/java/com/cocinarubi/event/ws/PedidoWebSocketListener.java) | Escucha el evento post-commit y envía el broadcast |
| [controller/sockets/WsPedidoWebController.java](../src/main/java/com/cocinarubi/presentation/controller/sockets/WsPedidoWebController.java) | Entrega el snapshot inicial al suscribirse |
| [domain/service/PedidoService.java](../src/main/java/com/cocinarubi/domain/service/PedidoService.java) | Publica el evento tras save, marcarImpreso y delete |
| [dao/PedidoRepository.java](../src/main/java/com/cocinarubi/dao/PedidoRepository.java) | Queries derivadas para filtrar pedidos WEB sin imprimir |
