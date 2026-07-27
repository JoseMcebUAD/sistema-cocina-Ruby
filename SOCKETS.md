# Modulo WebSocket — Arquitectura y Guia de Replicacion

Documenta como esta construido el modulo WebSocket en este proyecto Spring Boot 3 para que pueda replicarse en otro sistema con las mismas garantias de seguridad, aislamiento multi-tenant y consistencia transaccional.

---

## Indice

1. [Vision general](#1-vision-general)
2. [Estructura de paquetes](#2-estructura-de-paquetes)
3. [Capas y clases](#3-capas-y-clases)
   - 3.1 [Configuracion del contenedor](#31-configuracion-del-contenedor--tomcatwebsocketconfigjava)
   - 3.2 [Configuracion STOMP](#32-configuracion-stomp--websocketconfigjava)
   - 3.3 [Interceptor de autenticacion inbound](#33-interceptor-de-autenticacion-inbound--jwtchannelinterceptorjava)
   - 3.4 [Interceptor de reescritura outbound](#34-interceptor-de-reescritura-outbound--sucursaloutboundinterceptorjava)
   - 3.5 [Eventos de dominio](#35-eventos-de-dominio)
   - 3.6 [Listener transaccional](#36-listener-transaccional--comandawebsocketlistenerjava)
   - 3.7 [Controller de snapshot](#37-controller-de-snapshot--wscomandacontrollerjava)
4. [Flujo completo de mensajes](#4-flujo-completo-de-mensajes)
5. [Destinos STOMP](#5-destinos-stomp)
6. [Decisiones de diseno clave](#6-decisiones-de-diseno-clave)
7. [Guia paso a paso para replicar el modulo](#7-guia-paso-a-paso-para-replicar-el-modulo)

---

## 1. Vision general

El modulo implementa comunicacion en tiempo real con **STOMP sobre WebSocket**. Sus responsabilidades son:

| Responsabilidad | Mecanismo |
|-----------------|-----------|
| Autenticacion del cliente | JWT extraido del header STOMP `Authorization` en el frame `CONNECT` |
| Aislamiento multi-tenant | `id_sucursal` inyectado en el destino por interceptor; el cliente nunca lo conoce |
| Snapshot inicial | `@SubscribeMapping` en el controller; responde al primer `SUBSCRIBE` |
| Broadcast ante cambios | `ApplicationEvent` publicado en servicios; listener envia con `SimpMessagingTemplate` |
| Consistencia transaccional | `@TransactionalEventListener(AFTER_COMMIT)` — el broadcast solo ocurre si la TX confirmo |
| Compatibilidad Android OkHttp | Rechazo de `permessage-deflate` en el handshake |

---

## 2. Estructura de paquetes

```
com/
├── config/ws/
│   ├── TomcatWebSocketConfig.java        # Tamanos de buffer del contenedor Tomcat
│   └── WebSocketConfig.java              # STOMP broker, endpoint, interceptores
│
├── security/ws/
│   ├── JwtChannelInterceptor.java        # Autenticacion + autorizacion + reescritura inbound
│   └── SucursalOutboundInterceptor.java  # Reescritura de destinos en frames salientes
│
├── event/ws/
│   ├── ComandaActualizadaEvent.java      # Evento publicado al mutar pedidos
│   ├── InventarioActualizadoEvent.java   # Evento publicado al mutar inventario
│   └── ComandaWebSocketListener.java     # Escucha eventos y hace broadcast via WS
│
└── Controller/sockets/
    └── WsComandaController.java          # Snapshot inicial al suscribirse
```

---

## 3. Capas y clases

### 3.1 Configuracion del contenedor — `TomcatWebSocketConfig.java`

**Proposito:** registrar explicitamente el `ServletServerContainerFactoryBean` de Tomcat para que Spring tome control del handshake completo antes de que Tomcat lo procese.

```java
@Configuration
public class TomcatWebSocketConfig {

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}
```

**Por que es necesaria:**
Sin este bean, Tomcat negocia la extension `permessage-deflate` antes de que el `DefaultHandshakeHandler` de Spring pueda rechazarla, lo que rompe la conexion con OkHttp en Android.

**Para adaptar:** ajustar los tamanos de buffer segun el tamano maximo esperado del payload JSON.

---

### 3.2 Configuracion STOMP — `WebSocketConfig.java`

**Proposito:** punto central de configuracion del protocolo STOMP.

#### Broker de mensajes

```java
registry.enableSimpleBroker("/pendientes", "/topic");
registry.setApplicationDestinationPrefixes("/app");
```

- `/pendientes` y `/topic` — prefijos que el broker distribuye a los suscriptores.
- `/app` — prefijo para mensajes que van a un `@MessageMapping` o `@SubscribeMapping`.

#### Endpoint STOMP

```java
registry.addEndpoint("/todos")
    .setAllowedOriginPatterns(...)
    .setHandshakeHandler(new DefaultHandshakeHandler() {
        @Override
        protected List<WebSocketExtension> filterRequestedExtensions(...) {
            return Collections.emptyList(); // rechaza permessage-deflate
        }
    })
    .addInterceptors(new HandshakeInterceptor() { ... }); // log de handshake
```

#### Interceptores de canal

```java
// inbound: valida JWT y reescribe destinos
registration.interceptors(jwtChannelInterceptor);

// outbound: oculta id_sucursal en frames salientes
registration.interceptors(sucursalOutboundInterceptor);
```

#### Decorator de ciclo de vida

```java
registration.addDecoratorFactory(factory -> new WebSocketHandlerDecorator(handler) {
    @Override public void afterConnectionEstablished(session) { log.info(...); }
    @Override public void afterConnectionClosed(session, status) { log.info(...); }
});
```

Registra `session.getId()` y `remoteAddress` al conectar y desconectar sin logica de negocio.

---

### 3.3 Interceptor de autenticacion inbound — `JwtChannelInterceptor.java`

**Proposito:** validar el JWT y autorizar los destinos en cada frame STOMP entrante.

#### Constantes clave

```java
private static final String TOPIC_PROTEGIDO = "/pendientes";
private static final Pattern ALIAS_SIN_SUCURSAL =
    Pattern.compile("^(/app)?/pendientes/(total|por-ruta)$");
private static final Set<String> ROLES_REQUERIDOS =
    Set.of("ROLE_COCINA", "ROLE_ADMINISTRADOR", "ROLE_GERENTE_SUCURSAL");
```

#### Logica por comando STOMP

| Comando STOMP | Accion |
|---------------|--------|
| `CONNECT` | Extrae JWT del header `Authorization`, valida, carga `UserDetails`, guarda `id_sucursal` en `sessionAttributes` |
| `SUBSCRIBE` | Verifica que el usuario tenga un rol requerido para destinos protegidos; reescribe el destino inyectando `id_sucursal` |
| `SEND` | Igual que `SUBSCRIBE` |

#### Reescritura de destinos (inyeccion de sucursal)

```
Cliente envia:    /pendientes/total
Interceptor lee:  id_sucursal = 3  (de sessionAttributes)
Interceptor escribe: /pendientes/3/total
```

Esto permite que el broker enrute a un topico especifico por sucursal sin que el cliente conozca su id.

#### Patron de implementacion

```java
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command))     { autenticarConnect(accessor); }
        else if (SUBSCRIBE o SEND)                    { autorizarDestino(accessor); inyectarSucursal(accessor); }

        return message;
    }
}
```

---

### 3.4 Interceptor de reescritura outbound — `SucursalOutboundInterceptor.java`

**Proposito:** eliminar el `id_sucursal` del destino en los frames MESSAGE salientes para que el cliente reciba el mismo alias al que se suscribio.

```
Broker envia:    /pendientes/3/total
Interceptor:     /pendientes/3/total  →  /pendientes/total
Cliente recibe:  /pendientes/total    (el alias que uso al suscribirse)
```

#### Patron de implementacion

```java
@Component
public class SucursalOutboundInterceptor implements ChannelInterceptor {

    private static final Pattern DESTINO_INTERNO =
        Pattern.compile("^/pendientes/\\d+/(total|por-ruta)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = /* wrap o getAccessor */;
        if (!StompCommand.MESSAGE.equals(accessor.getCommand())) return message;

        Matcher m = DESTINO_INTERNO.matcher(accessor.getDestination());
        if (!m.matches()) return message;

        accessor.setDestination("/pendientes/" + m.group(1));
        return mutable ? message : MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }
}
```

**Detalle de mutabilidad:** si el accessor ya es mutable se reutiliza el mismo mensaje; si no, se reconstruye con `MessageBuilder` para evitar `UnsupportedOperationException`.

---

### 3.5 Eventos de dominio

Son POJOs que extienden `ApplicationEvent`. Se publican desde los servicios con `ApplicationEventPublisher`.

#### `ComandaActualizadaEvent`

```java
public class ComandaActualizadaEvent extends ApplicationEvent {
    private final int idSucursal;
    public ComandaActualizadaEvent(Object source, int idSucursal) {
        super(source);
        this.idSucursal = idSucursal;
    }
}
```

Publicado en `PedidoService` al crear, editar, eliminar o marcar como impreso un pedido:

```java
eventPublisher.publishEvent(new ComandaActualizadaEvent(this, pedido.getIdSucursal()));
```

#### `InventarioActualizadoEvent`

```java
public class InventarioActualizadoEvent extends ApplicationEvent {
    public InventarioActualizadoEvent(Object source) { super(source); }
}
```

Publicado en `InventarioComidaService` al mutar el inventario.

---

### 3.6 Listener transaccional — `ComandaWebSocketListener.java`

**Proposito:** escuchar los eventos de dominio y hacer broadcast via WebSocket solo despues de que la transaccion confirmo.

```java
@Component
public class ComandaWebSocketListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onComandaActualizada(ComandaActualizadaEvent event) {
        String prefijo = "/pendientes/" + event.getIdSucursal();
        messagingTemplate.convertAndSend(prefijo + "/total",  comandaService.contarPendientes(idSucursal));
        messagingTemplate.convertAndSend(prefijo + "/por-ruta", comandaService.contarPendientesPorRuta(idSucursal));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInventarioActualizado(InventarioActualizadoEvent event) {
        messagingTemplate.convertAndSend("/topic/inventario/stock", inventarioComidaService.findAll());
    }
}
```

**Por que `AFTER_COMMIT`:** garantiza que el cliente nunca recibe datos de una transaccion que despues hizo rollback. Si se usara `BEFORE_COMMIT` y el commit fallara, el cliente tendria un estado inconsistente.

---

### 3.7 Controller de snapshot — `WsComandaController.java`

**Proposito:** entregar el estado actual al cliente en el momento exacto en que se suscribe, antes de que llegue cualquier broadcast.

```java
@Controller
public class WsComandaController {

    @SubscribeMapping("/pendientes/*/total")
    public PendienteCountDTO snapshotTotal(SimpMessageHeaderAccessor headerAccessor) {
        return comandaService.contarPendientes(extraerSucursal(headerAccessor));
    }

    @SubscribeMapping("/pendientes/*/por-ruta")
    public Map<String, Integer> snapshotPorRuta(SimpMessageHeaderAccessor headerAccessor) {
        return comandaService.contarPendientesPorRuta(extraerSucursal(headerAccessor));
    }

    @SubscribeMapping("/inventario/stock")
    public List<InventarioComida> snapshotInventario() {
        return inventarioComidaService.findAll();
    }

    private int extraerSucursal(SimpMessageHeaderAccessor h) {
        Integer id = (Integer) h.getSessionAttributes().get("id_sucursal");
        if (id == null) throw new AccessDeniedException("Sesion sin sucursal");
        return id;
    }
}
```

**Como funciona `@SubscribeMapping`:**
Cuando el cliente hace `SUBSCRIBE /pendientes/total`, `JwtChannelInterceptor` reescribe el destino a `/pendientes/3/total`. El router de Spring mapea el pattern `"/pendientes/*/total"` y llama al metodo, cuyo valor de retorno se envia directamente al suscriptor (no al broker).

---

## 4. Flujo completo de mensajes

```
CLIENTE
  │
  │  1. CONNECT  {Authorization: Bearer <jwt>}
  ▼
JwtChannelInterceptor.autenticarConnect()
  ├── valida JWT
  ├── carga UserDetails
  ├── accessor.setUser(auth)
  └── sessionAttributes["id_sucursal"] = 3
  │
  │  2. SUBSCRIBE  /pendientes/total
  ▼
JwtChannelInterceptor.autorizarDestino()  → verifica rol
JwtChannelInterceptor.inyectarSucursalEnDestino()
  └── reescribe → /pendientes/3/total
  │
  │  3. Router llama @SubscribeMapping("/pendientes/*/total")
  ▼
WsComandaController.snapshotTotal()
  └── retorna PendienteCountDTO
  │
  │  4. Spring envia MESSAGE a /pendientes/3/total
  ▼
SucursalOutboundInterceptor
  └── reescribe → /pendientes/total
  │
CLIENTE recibe snapshot en /pendientes/total
  │
  │  (mas tarde) PedidoService guarda un pedido
  │    └── eventPublisher.publishEvent(ComandaActualizadaEvent(this, 3))
  ▼
TX confirma → ComandaWebSocketListener.onComandaActualizada()
  ├── messagingTemplate.convertAndSend("/pendientes/3/total", count)
  └── messagingTemplate.convertAndSend("/pendientes/3/por-ruta", map)
  │
SucursalOutboundInterceptor reescribe → /pendientes/total y /pendientes/por-ruta
  │
CLIENTE recibe broadcast en /pendientes/total
```

---

## 5. Destinos STOMP

| Tipo | Destino del cliente | Destino interno | Descripcion |
|------|--------------------|-----------------|-|
| Subscribe + snapshot | `/pendientes/total` | `/pendientes/{id}/total` | Contador de pedidos pendientes |
| Subscribe + snapshot | `/pendientes/por-ruta` | `/pendientes/{id}/por-ruta` | Pendientes agrupados por ruta |
| Subscribe + snapshot | `/inventario/stock` | `/inventario/stock` | Stock actual de inventario |
| Broadcast | `/topic/bloqueo-impresion` | mismo | Estado de locks de impresion |

---

## 6. Decisiones de diseno clave

### Aislamiento multi-tenant sin exponer el id

El cliente no necesita conocer su `id_sucursal`. El JWT lo contiene como claim; el interceptor lo extrae una vez en el `CONNECT` y lo guarda en `sessionAttributes`. A partir de ahi, todos los destinos se reescriben automaticamente. El cliente siempre usa destinos genericos (`/pendientes/total`).

### `@SubscribeMapping` vs subscribe al broker

`@SubscribeMapping` responde directamente al suscriptor, lo que elimina la necesidad de un poll inicial separado (no hay endpoint REST adicional). La respuesta viaja como un frame MESSAGE privado, no como broadcast.

### Separacion de eventos de dominio y broadcast WebSocket

Los servicios solo publican `ApplicationEvent`; no inyectan `SimpMessagingTemplate`. Esto desacopla la logica de negocio del transporte WebSocket. Si manana se reemplaza WebSocket por SSE, solo cambia el listener.

### Consistencia con `AFTER_COMMIT`

Usar `TransactionPhase.BEFORE_COMMIT` haria que el broadcast llegara antes de que el cambio sea visible para otras conexiones a la DB. `AFTER_COMMIT` garantiza que los datos ya estan duros en disco cuando el cliente los solicita.

---

## 7. Guia paso a paso para replicar el modulo

### Prerequisitos

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

---

### Paso 1 — Configurar el contenedor Tomcat

```java
// com/config/ws/TomcatWebSocketConfig.java
@Configuration
public class TomcatWebSocketConfig {
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean c = new ServletServerContainerFactoryBean();
        c.setMaxTextMessageBufferSize(8192);
        c.setMaxBinaryMessageBufferSize(8192);
        return c;
    }
}
```

Ajustar los tamanos si los payloads JSON son grandes.

---

### Paso 2 — Configurar STOMP (`WebSocketConfig`)

```java
// com/config/ws/WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // inyectar tus interceptores aqui
    private final MiChannelInterceptor miInterceptor;
    private final MiOutboundInterceptor miOutboundInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // prefijos que el broker distribuye a suscriptores
        registry.enableSimpleBroker("/mi-topico", "/topic");
        // prefijo para mensajes dirigidos a @MessageMapping / @SubscribeMapping
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                          // URL de conexion WebSocket
                .setAllowedOriginPatterns("http://localhost:*", "https://mi-dominio.com")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected List<WebSocketExtension> filterRequestedExtensions(
                            ServerHttpRequest req, List<WebSocketExtension> requested,
                            List<WebSocketExtension> supported) {
                        return Collections.emptyList(); // quitar si no usas Android OkHttp
                    }
                });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration r) {
        r.interceptors(miInterceptor);      // autenticacion + reescritura inbound
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration r) {
        r.interceptors(miOutboundInterceptor); // ocultar ids internos en salida
    }
}
```

---

### Paso 3 — Crear el interceptor de autenticacion inbound

Implementa `ChannelInterceptor`. Solo necesitas `preSend`.

```java
// com/security/ws/MiChannelInterceptor.java
@Component
public class MiChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;

        switch (accessor.getCommand()) {
            case CONNECT  -> autenticar(accessor);
            case SUBSCRIBE, SEND -> {
                autorizar(accessor);
                reescribirDestino(accessor); // si aplica multi-tenant
            }
        }
        return message;
    }

    private void autenticar(StompHeaderAccessor accessor) {
        // 1. Leer header "Authorization: Bearer <token>"
        String token = extraerToken(accessor);
        // 2. Validar token con tu JwtService
        // 3. Cargar UserDetails
        // 4. accessor.setUser(new UsernamePasswordAuthenticationToken(...))
        // 5. Guardar datos de sesion: accessor.getSessionAttributes().put("clave", valor)
    }

    private void autorizar(StompHeaderAccessor accessor) {
        // Verificar accessor.getUser() y sus roles para destinos protegidos
    }

    private void reescribirDestino(StompHeaderAccessor accessor) {
        // Si el cliente usa alias, inyectar el id interno en el destino
        // accessor.setDestination(nuevoDestino);
    }
}
```

**Puntos criticos:**
- Lanzar `BadCredentialsException` o `AccessDeniedException` para rechazar la conexion/suscripcion.
- Guardar en `sessionAttributes` cualquier dato que los controllers y el outbound interceptor necesiten.
- El accessor devuelto por `MessageHeaderAccessor.getAccessor` es mutable; no crear uno nuevo.

---

### Paso 4 — Crear el interceptor outbound (si hay reescritura)

Solo si necesitas ocultar informacion interna al cliente.

```java
// com/security/ws/MiOutboundInterceptor.java
@Component
public class MiOutboundInterceptor implements ChannelInterceptor {

    private static final Pattern INTERNO = Pattern.compile("^/mi-topico/\\d+/(evento-a|evento-b)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        boolean mutable = accessor != null;
        if (!mutable) accessor = StompHeaderAccessor.wrap(message);

        if (!StompCommand.MESSAGE.equals(accessor.getCommand())) return message;

        String destino = accessor.getDestination();
        Matcher m = INTERNO.matcher(destino != null ? destino : "");
        if (!m.matches()) return message;

        accessor.setDestination("/mi-topico/" + m.group(1)); // alias sin id interno
        return mutable ? message
                       : MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }
}
```

---

### Paso 5 — Crear los eventos de dominio

Un evento por cada tipo de cambio que deba provocar un broadcast.

```java
// com/event/ws/MiRecursoActualizadoEvent.java
public class MiRecursoActualizadoEvent extends ApplicationEvent {
    private final int idTenant; // o cualquier dato que necesite el listener
    public MiRecursoActualizadoEvent(Object source, int idTenant) {
        super(source);
        this.idTenant = idTenant;
    }
    public int getIdTenant() { return idTenant; }
}
```

---

### Paso 6 — Publicar eventos desde los servicios

```java
@Service
public class MiServicio {

    private final ApplicationEventPublisher eventPublisher;

    public void crearRecurso(...) {
        // ... logica de negocio y persistencia ...
        eventPublisher.publishEvent(new MiRecursoActualizadoEvent(this, idTenant));
    }
}
```

El servicio no sabe nada de WebSocket; solo publica el evento.

---

### Paso 7 — Crear el listener transaccional

```java
// com/event/ws/MiWebSocketListener.java
@Component
public class MiWebSocketListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final MiServicio miServicio;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecursoActualizado(MiRecursoActualizadoEvent event) {
        String destino = "/mi-topico/" + event.getIdTenant() + "/evento-a";
        Object payload = miServicio.obtenerEstadoActual(event.getIdTenant());
        messagingTemplate.convertAndSend(destino, payload);
    }
}
```

Siempre usar `AFTER_COMMIT`. Si la operacion no esta dentro de una transaccion, el evento se ejecuta de forma sincrona inmediatamente al publicarse — igual es correcto.

---

### Paso 8 — Crear el controller de snapshot

```java
// com/Controller/sockets/MiWsController.java
@Controller
public class MiWsController {

    private final MiServicio miServicio;

    @SubscribeMapping("/mi-topico/*/evento-a")
    public MiPayloadDTO snapshot(SimpMessageHeaderAccessor h) {
        int idTenant = (Integer) h.getSessionAttributes().get("clave");
        return miServicio.obtenerEstadoActual(idTenant);
    }
}
```

El `*` en el pattern matchea el segmento que el interceptor inyecto. La respuesta se envia directamente al cliente que se suscribio, no al broker.

---

### Lista de verificacion para replicar

```
[ ] Dependencia spring-boot-starter-websocket en pom.xml
[ ] TomcatWebSocketConfig con el bean ServletServerContainerFactoryBean
[ ] WebSocketConfig con @EnableWebSocketMessageBroker:
    [ ] enableSimpleBroker con los prefijos correctos
    [ ] setApplicationDestinationPrefixes("/app")
    [ ] addEndpoint con CORS configurado
    [ ] filterRequestedExtensions vacio (si hay clientes Android OkHttp)
    [ ] configureClientInboundChannel → interceptor de autenticacion
    [ ] configureClientOutboundChannel → interceptor de reescritura (si aplica)
[ ] ChannelInterceptor inbound:
    [ ] CONNECT: valida token, setUser, guarda datos en sessionAttributes
    [ ] SUBSCRIBE/SEND: autoriza rol, reescribe destino si es necesario
[ ] ChannelInterceptor outbound (si aplica):
    [ ] Solo actua sobre StompCommand.MESSAGE
    [ ] Maneja correctamente la mutabilidad del accessor
[ ] ApplicationEvent por cada tipo de cambio que dispara broadcast
[ ] @TransactionalEventListener(AFTER_COMMIT) en el listener
[ ] @SubscribeMapping en el controller para snapshot inicial
[ ] Ningun servicio inyecta SimpMessagingTemplate directamente
```
