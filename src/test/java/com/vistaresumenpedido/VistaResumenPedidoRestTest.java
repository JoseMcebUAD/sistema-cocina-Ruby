package com.vistaresumenpedido;

import com.cocinarubi.presentation.security.JwtService;
import com.cocinarubi.presentation.security.UsuarioDetailsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class VistaResumenPedidoRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private int testRutaId;
    private int testRegistroClienteId;
    private int testProductoId;
    private int pedidoCocinaMostradorId;
    private int pedidoCocinaDomicilioId;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        String rutaJson = """
                {
                  "nombre": "Ruta Test VistaResumen",
                  "boundaryWkt": "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))",
                  "isActive": true,
                  "tarifaEnvio": 40.00,
                  "tiempoEstimadoMin": 20
                }
                """;
        ResponseEntity<String> rutaResp = restTemplate.exchange(
                "/ruta", HttpMethod.POST, new HttpEntity<>(rutaJson, authHeaders), String.class);
        testRutaId = mapper.readTree(rutaResp.getBody()).get("data").get("idRuta").asInt();

        String clienteJson = String.format("""
                {
                  "nombre": "Cliente Test VistaResumen",
                  "telefono": "5550008888",
                  "idRuta": %d,
                  "direccion": "Calle Vista 42"
                }
                """, testRutaId);
        ResponseEntity<String> clienteResp = restTemplate.exchange(
                "/registroCliente", HttpMethod.POST, new HttpEntity<>(clienteJson, authHeaders), String.class);
        testRegistroClienteId = mapper.readTree(clienteResp.getBody()).get("data").get("idRegistroCliente").asInt();

        String productoJson = """
                {
                  "nombreProducto": "Producto Test VistaResumen",
                  "precioDomicilio": 60.00,
                  "precioNormal": 50.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "tipoProducto": "BEBIDA",
                  "saltarConfirmacion": true
                }
                """;
        ResponseEntity<String> productoResp = restTemplate.exchange(
                "/productoCocina", HttpMethod.POST, new HttpEntity<>(productoJson, authHeaders), String.class);
        testProductoId = mapper.readTree(productoResp.getBody()).get("data").get("idProductoCocina").asInt();

        // Pedido 1: COCINA MOSTRADOR, EFECTIVO puro (sin secundario) — 50.00
        String mostradorJson = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 50.00,
                  "nombreCliente": "Ana Mostrador",
                  "comidas": [], "desayunos": [], "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 50.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);
        ResponseEntity<String> mostradorResp = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(mostradorJson, authHeaders), String.class);
        pedidoCocinaMostradorId = mapper.readTree(mostradorResp.getBody()).get("data").get("idPedido").asInt();

        // Pedido 2: COCINA DOMICILIO, pago mixto EFECTIVO(60) + TARJETA(40) — total 100
        String domicilioJson = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "metodoPagoSecundario": "TARJETA",
                  "tipoPedido": "DOMICILIO",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 60.00,
                  "pedidoDomicilioCocina": {
                    "idRegistroCliente": %d,
                    "tarifa": 40.00,
                    "domicilio": "Calle Vista 42",
                    "idRuta": %d
                  },
                  "comidas": [], "desayunos": [], "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 60.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testRegistroClienteId, testRutaId, testProductoId);
        ResponseEntity<String> domicilioResp = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(domicilioJson, authHeaders), String.class);
        pedidoCocinaDomicilioId = mapper.readTree(domicilioResp.getBody()).get("data").get("idPedido").asInt();

        System.out.println("[SETUP] mostrador=" + pedidoCocinaMostradorId
                + " domicilio=" + pedidoCocinaDomicilioId
                + " ruta=" + testRutaId
                + " cliente=" + testRegistroClienteId
                + " producto=" + testProductoId);
    }

    @AfterAll
    void tearDown() {
        if (pedidoCocinaMostradorId > 0) {
            restTemplate.exchange("/pedido/" + pedidoCocinaMostradorId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (pedidoCocinaDomicilioId > 0) {
            restTemplate.exchange("/pedido/" + pedidoCocinaDomicilioId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testProductoId > 0) {
            restTemplate.exchange("/productoCocina/" + testProductoId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testRegistroClienteId > 0) {
            restTemplate.exchange("/registroCliente/" + testRegistroClienteId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testRutaId > 0) {
            restTemplate.exchange("/ruta/" + testRutaId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        System.out.println("[TEARDOWN] datos de prueba eliminados");
    }

    @Test
    @Order(1)
    @DisplayName("GET /vista-resumen-pedido - Retorna página con estructura correcta")
    public void findVista_paginacion() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido?page=0&size=50", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.has("content"));
        assertTrue(data.has("totalElements"));
        assertTrue(data.get("content").isArray());
        System.out.println("[OK] paginación total=" + data.get("totalElements").asInt());
    }

    @Test
    @Order(2)
    @DisplayName("GET /vista-resumen-pedido - Filtro tipoPedido=MOSTRADOR solo trae mostrador")
    public void findVista_filtroTipoPedido() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido?tipoPedido=MOSTRADOR&size=100", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode content = mapper.readTree(response.getBody()).get("data").get("content");
        assertTrue(content.size() > 0);
        content.forEach(row -> assertEquals("MOSTRADOR", row.get("tipoPedido").asText()));
        System.out.println("[OK] filtro tipoPedido=MOSTRADOR devolvió " + content.size() + " filas");
    }

    @Test
    @Order(3)
    @DisplayName("GET /vista-resumen-pedido - Filtro pedidoCreadoDesde=COCINA solo trae cocina")
    public void findVista_filtroCreadoDesde() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido?pedidoCreadoDesde=COCINA&size=100", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode content = mapper.readTree(response.getBody()).get("data").get("content");
        assertTrue(content.size() > 0);
        content.forEach(row -> assertEquals("COCINA", row.get("pedidoCreadoDesde").asText()));
        System.out.println("[OK] filtro pedidoCreadoDesde=COCINA devolvió " + content.size() + " filas");
    }

    @Test
    @Order(4)
    @DisplayName("GET /vista-resumen-pedido - Fila COCINA MOSTRADOR trae nombreCliente y campos de domicilio nulos")
    public void findVista_filaMostrador() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido?tipoPedido=MOSTRADOR&pedidoCreadoDesde=COCINA&size=100",
                HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode content = mapper.readTree(response.getBody()).get("data").get("content");
        JsonNode fila = null;
        for (JsonNode row : content) {
            if (row.get("idPedido").asInt() == pedidoCocinaMostradorId) { fila = row; break; }
        }
        assertNotNull(fila, "No se encontró el pedido MOSTRADOR creado en el setup");
        assertEquals("Ana Mostrador", fila.get("nombreCliente").asText());
        assertTrue(fila.get("ruta") == null || fila.get("ruta").isNull());
        assertTrue(fila.get("domicilio") == null || fila.get("domicilio").isNull());
        assertTrue(fila.get("precioTarifa") == null || fila.get("precioTarifa").isNull());
        System.out.println("[OK] fila MOSTRADOR: nombreCliente=" + fila.get("nombreCliente").asText()
                + " ruta/domicilio/precioTarifa null");
    }

    @Test
    @Order(5)
    @DisplayName("GET /vista-resumen-pedido - Fila COCINA DOMICILIO trae ruta, domicilio y precioTarifa")
    public void findVista_filaCocinaDomicilio() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido?tipoPedido=DOMICILIO&pedidoCreadoDesde=COCINA&size=100",
                HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode content = mapper.readTree(response.getBody()).get("data").get("content");
        JsonNode fila = null;
        for (JsonNode row : content) {
            if (row.get("idPedido").asInt() == pedidoCocinaDomicilioId) { fila = row; break; }
        }
        assertNotNull(fila, "No se encontró el pedido DOMICILIO creado en el setup");
        assertEquals("Cliente Test VistaResumen", fila.get("nombreCliente").asText());
        assertEquals("Ruta Test VistaResumen", fila.get("ruta").asText());
        assertEquals("Calle Vista 42", fila.get("domicilio").asText());
        assertEquals(40.00, fila.get("precioTarifa").asDouble());
        System.out.println("[OK] fila COCINA DOMICILIO: ruta=" + fila.get("ruta").asText()
                + " domicilio=" + fila.get("domicilio").asText()
                + " precioTarifa=" + fila.get("precioTarifa").asDouble());
    }

    @Test
    @Order(6)
    @DisplayName("GET /vista-resumen-pedido/metricas - Estructura completa (pedidos + 7 métricas)")
    public void findVistaConMetricas_estructura() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido/metricas?size=100", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.has("pedidos"));
        assertTrue(data.get("pedidos").has("content"));
        assertTrue(data.has("cantidadTotal"));
        assertTrue(data.has("cantidadImpresos"));
        assertTrue(data.has("cantidadNoImpresos"));
        assertTrue(data.has("ingresoTotal"));
        assertTrue(data.has("ingresoEfectivo"));
        assertTrue(data.has("ingresoTransferencia"));
        assertTrue(data.has("ingresoTarjeta"));
        System.out.println("[OK] métricas estructura: cantidadTotal=" + data.get("cantidadTotal").asLong()
                + " ingresoTotal=" + data.get("ingresoTotal").asDouble());
    }

    @Test
    @Order(7)
    @DisplayName("GET /vista-resumen-pedido/metricas - cantidadTotal coincide con page.totalElements y sumas cuadran")
    public void findVistaConMetricas_invariantes() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido/metricas?pedidoCreadoDesde=COCINA&size=100", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");

        long cantidadTotal = data.get("cantidadTotal").asLong();
        long pageTotal = data.get("pedidos").get("totalElements").asLong();
        assertEquals(cantidadTotal, pageTotal, "cantidadTotal debe coincidir con page.totalElements");

        long impresos = data.get("cantidadImpresos").asLong();
        long noImpresos = data.get("cantidadNoImpresos").asLong();
        assertEquals(cantidadTotal, impresos + noImpresos,
                "cantidadImpresos + cantidadNoImpresos debe igualar cantidadTotal");

        double total = data.get("ingresoTotal").asDouble();
        double efectivo = data.get("ingresoEfectivo").asDouble();
        double transferencia = data.get("ingresoTransferencia").asDouble();
        double tarjeta = data.get("ingresoTarjeta").asDouble();
        assertEquals(total, efectivo + transferencia + tarjeta, 0.01,
                "ingreso por método debe sumar exactamente el ingresoTotal");
        System.out.println("[OK] invariantes: total=" + total
                + " efectivo=" + efectivo + " transferencia=" + transferencia + " tarjeta=" + tarjeta);
    }

    @Test
    @Order(8)
    @DisplayName("GET /vista-resumen-pedido/metricas - Pago mixto reparte según pagoClientePrincipal")
    public void findVistaConMetricas_pagoMixto() throws Exception {
        // El pedido COCINA DOMICILIO creado en setUp es EFECTIVO(60) + TARJETA(40) = 100 total.
        // Filtramos solo ese universo para poder verificar exactamente los agregados.
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido/metricas?tipoPedido=DOMICILIO&pedidoCreadoDesde=COCINA&size=100",
                HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");

        // Estos aggregates incluyen SOLO pedidos COCINA DOMICILIO; el del setup contribuye:
        //   efectivo += 60 (pagoClientePrincipal)
        //   tarjeta  += 40 (precioFinalOrden - pagoClientePrincipal)
        double efectivo = data.get("ingresoEfectivo").asDouble();
        double tarjeta = data.get("ingresoTarjeta").asDouble();
        double total = data.get("ingresoTotal").asDouble();

        assertTrue(efectivo >= 60.00, "Efectivo debe incluir al menos los 60 del pago mixto");
        assertTrue(tarjeta >= 40.00, "Tarjeta debe incluir al menos los 40 del pago mixto");
        assertEquals(total, efectivo + data.get("ingresoTransferencia").asDouble() + tarjeta, 0.01);
        System.out.println("[OK] pago mixto: efectivo>=60 (actual=" + efectivo + ") tarjeta>=40 (actual=" + tarjeta + ")");
    }

    @Test
    @Order(9)
    @DisplayName("GET /vista-resumen-pedido - Sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido", HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }

    @Test
    @Order(10)
    @DisplayName("GET /vista-resumen-pedido/metricas - Sin token debe responder 401")
    public void seguridadMetricas_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vista-resumen-pedido/metricas", HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] métricas sin token → 401");
    }
}
