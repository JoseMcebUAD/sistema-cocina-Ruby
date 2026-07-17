package com.pedido;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cocinarubi.presentation.security.JwtService;
import com.cocinarubi.presentation.security.UsuarioDetailsService;
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
public class PedidoRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private int createdId;
    private int testProductoId;
    private int testRegistroClienteId;
    private int testRutaId;
    private int createdCocinaPickUpId;
    private int createdCocinaDomicilioId;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // La ruta se crea primero para que el cliente pueda referenciarla
        String rutaJson = """
                {
                  "nombre": "Ruta Test Pedido",
                  "boundaryWkt": "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))",
                  "isActive": true,
                  "tarifaEnvio": 40.00,
                  "tiempoEstimadoMin": 20
                }
                """;
        ResponseEntity<String> rutaResp = restTemplate.exchange(
                "/ruta", HttpMethod.POST, new HttpEntity<>(rutaJson, authHeaders), String.class
        );
        testRutaId = mapper.readTree(rutaResp.getBody()).get("data").get("idRuta").asInt();

        String clienteJson = String.format("""
                {
                  "nombre": "Cliente Test Pedido",
                  "telefono": "5550009999",
                  "idRuta": %d,
                  "direccion": "Calle Principal 42 Int 3"
                }
                """, testRutaId);
        ResponseEntity<String> clienteResp = restTemplate.exchange(
                "/registroCliente", HttpMethod.POST, new HttpEntity<>(clienteJson, authHeaders), String.class
        );
        testRegistroClienteId = mapper.readTree(clienteResp.getBody()).get("data").get("idRegistroCliente").asInt();

        String productoJson = """
                {
                  "nombreProducto": "Agua Test Pedido",
                  "precioDomicilio": 15.00,
                  "precioNormal": 10.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "tipoProducto": "BEBIDA",
                  "saltarConfirmacion": true
                }
                """;
        ResponseEntity<String> productoResp = restTemplate.exchange(
                "/productoCocina", HttpMethod.POST, new HttpEntity<>(productoJson, authHeaders), String.class
        );
        testProductoId = mapper.readTree(productoResp.getBody()).get("data").get("idProductoCocina").asInt();

        System.out.println("[SETUP] productoId=" + testProductoId
                + " clienteId=" + testRegistroClienteId
                + " rutaId=" + testRutaId);
    }

    @AfterAll
    void tearDown() {
        if (createdCocinaPickUpId > 0) {
            restTemplate.exchange("/pedido/" + createdCocinaPickUpId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (createdCocinaDomicilioId > 0) {
            restTemplate.exchange("/pedido/" + createdCocinaDomicilioId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
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
    @DisplayName("GET /pedido - Debe retornar lista de pedidos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | pedidos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /pedido - Debe crear un pedido COCINA MOSTRADOR y retornar status 201")
    public void save() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 50.00,
                  "nombreCliente": "Test REST",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idPedido").asInt();
        assertTrue(createdId > 0);
        assertEquals("MOSTRADOR", data.get("tipoPedido").asText());
        assertNotNull(data.get("pedidoCocina"));
        assertEquals("Test REST", data.get("pedidoCocina").get("nombreCliente").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /pedido/{id} - Debe retornar el pedido correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idPedido").asInt());
        assertEquals("MOSTRADOR", data.get("tipoPedido").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idPedido").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /pedido/{id} - Debe actualizar el pedido y retornar status 200")
    public void update() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "TARJETA",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "nombreCliente": "Test REST",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("TARJETA", data.get("metodoPagoPrincipal").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | metodoPagoPrincipal=" + data.get("metodoPagoPrincipal").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /pedido/{id} - Debe eliminar el pedido y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para pedido id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /pedido - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }

    @Test
    @Order(7)
    @DisplayName("POST /pedido - COCINA PICK_UP con nombreCliente debe retornar 201")
    public void saveCocina_pickUp() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "PICK_UP",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 30.00,
                  "nombreCliente": "Ana García",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdCocinaPickUpId = data.get("idPedido").asInt();
        assertTrue(createdCocinaPickUpId > 0);
        assertEquals("PICK_UP", data.get("tipoPedido").asText());
        assertFalse(data.get("pedidoCocina").isNull());
        assertEquals("Ana García", data.get("pedidoCocina").get("nombreCliente").asText());
        assertTrue(data.get("domicilioCocina").isNull());
        System.out.println("[OK] COCINA+PICK_UP id=" + createdCocinaPickUpId
                + " nombreCliente=" + data.get("pedidoCocina").get("nombreCliente").asText());
    }

    @Test
    @Order(8)
    @DisplayName("POST /pedido - COCINA DOMICILIO con idRegistroCliente debe retornar 201 con datos del cliente")
    public void saveCocina_domicilio() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "DOMICILIO",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 80.00,
                  "idRegistroCliente": %d,
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testRegistroClienteId, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdCocinaDomicilioId = data.get("idPedido").asInt();
        assertTrue(createdCocinaDomicilioId > 0);
        assertEquals("DOMICILIO", data.get("tipoPedido").asText());

        JsonNode domicilioCocina = data.get("domicilioCocina");
        assertFalse(domicilioCocina.isNull());
        assertEquals(testRegistroClienteId, domicilioCocina.get("idRegistroCliente").asInt());
        assertEquals("Cliente Test Pedido", domicilioCocina.get("nombreCliente").asText());
        assertEquals(testRutaId, domicilioCocina.get("idRuta").asInt());
        assertEquals("Calle Principal 42 Int 3", domicilioCocina.get("domicilio").asText());
        assertEquals(40.00, domicilioCocina.get("precioTarifa").asDouble());

        assertTrue(data.get("domicilio").isNull());
        assertTrue(data.get("pedidoCocina").isNull());

        assertEquals(50.00, data.get("precioFinalOrden").asDouble());
        System.out.println("[OK] COCINA+DOMICILIO id=" + createdCocinaDomicilioId
                + " cliente=" + domicilioCocina.get("nombreCliente").asText()
                + " precioFinal=" + data.get("precioFinalOrden").asDouble());
    }

    @Test
    @Order(9)
    @DisplayName("POST /pedido - COCINA DOMICILIO sin idRegistroCliente debe retornar 400")
    public void save_cocinaDomicilio_sinRegistroCliente_retorna400() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "DOMICILIO",
                  "pedidoCreadoDesde": "COCINA",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = mapper.readTree(response.getBody());
        assertTrue(body.get("message").asText().contains("idRegistroCliente"));
        System.out.println("[OK] COCINA+DOMICILIO sin idRegistroCliente → 400: " + body.get("message").asText());
    }

    @Test
    @Order(10)
    @DisplayName("POST /pedido - WEB DOMICILIO sin campo domicilio debe retornar 400")
    public void save_webDomicilio_sinDomicilio_retorna400() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "TARJETA",
                  "tipoPedido": "DOMICILIO",
                  "pedidoCreadoDesde": "WEB",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = mapper.readTree(response.getBody());
        assertTrue(body.get("message").asText().contains("domicilio"));
        System.out.println("[OK] WEB+DOMICILIO sin domicilio → 400: " + body.get("message").asText());
    }
}
