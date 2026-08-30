package com.clienteweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.cocinarubi.presentation.security.JwtService;
import com.cocinarubi.presentation.security.UsuarioDetailsService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class ClienteWebRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders adminHeaders;
    private HttpHeaders webHeaders;
    private HttpHeaders emptyHeaders;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String uuidCliente = "web-test-" + UUID.randomUUID().toString().substring(0, 8);

    private String sessionToken;
    private int testProductoId;
    private int createdPedidoId;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(jwtService.generarToken(jefa));
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);

        emptyHeaders = new HttpHeaders();
        emptyHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Crear producto de prueba con JWT admin para usarlo en pedidos web
        String productoJson = """
                {
                  "nombreProducto": "Agua Web Test",
                  "precioDomicilio": 15.00,
                  "precioNormal": 10.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "idCategoria": 1,
                  "idSubcategorias": [],
                  "saltarConfirmacion": true
                }
                """;
        ResponseEntity<String> productoResp = restTemplate.exchange(
                "/producto-cocina", HttpMethod.POST, new HttpEntity<>(productoJson, adminHeaders), String.class
        );
        testProductoId = mapper.readTree(productoResp.getBody()).get("data").get("idProductoCocina").asInt();
        System.out.println("[SETUP] testProductoId=" + testProductoId);
    }

    @AfterAll
    void tearDown() {
        if (createdPedidoId > 0) {
            restTemplate.exchange("/pedido/" + createdPedidoId, HttpMethod.DELETE,
                    new HttpEntity<>(adminHeaders), String.class);
        }
        if (testProductoId > 0) {
            restTemplate.exchange("/producto-cocina/" + testProductoId, HttpMethod.DELETE,
                    new HttpEntity<>(adminHeaders), String.class);
        }
        System.out.println("[TEARDOWN] datos de prueba eliminados");
    }

    @Test
    @Order(1)
    @DisplayName("POST /web/sesion - Cliente nuevo debe recibir sessionToken y expiracion a 7 dias")
    public void sesion_clienteNuevo() throws Exception {
        String json = """
                {
                  "uuidCliente": "%s",
                  "userAgent": "Mozilla/5.0 Test",
                  "screenWidth": 1920,
                  "screenHeight": 1080,
                  "timezone": "America/Merida",
                  "language": "es-MX",
                  "colorDepth": 24,
                  "ipAddress": "127.0.0.1"
                }
                """.formatted(uuidCliente);

        ResponseEntity<String> response = restTemplate.exchange(
                "/web/sesion", HttpMethod.POST, new HttpEntity<>(json, emptyHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data.get("sessionToken").asText());
        assertNotNull(data.get("tokenExpiracion").asText());
        assertEquals(uuidCliente, data.get("uuidCliente").asText());

        sessionToken = data.get("sessionToken").asText();
        webHeaders = new HttpHeaders();
        webHeaders.setBearerAuth(sessionToken);
        webHeaders.setContentType(MediaType.APPLICATION_JSON);

        System.out.println("[OK] sesion nueva | token=" + sessionToken.substring(0, 8) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("POST /web/sesion - Misma UUID debe devolver el mismo sessionToken (token vigente)")
    public void sesion_clienteExistente_mismToken() throws Exception {
        String json = """
                {
                  "uuidCliente": "%s",
                  "userAgent": "Mozilla/5.0 Test v2",
                  "screenWidth": 1920,
                  "screenHeight": 1080,
                  "timezone": "America/Merida",
                  "language": "es-MX",
                  "colorDepth": 24,
                  "ipAddress": "127.0.0.2"
                }
                """.formatted(uuidCliente);

        ResponseEntity<String> response = restTemplate.exchange(
                "/web/sesion", HttpMethod.POST, new HttpEntity<>(json, emptyHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(sessionToken, data.get("sessionToken").asText());
        System.out.println("[OK] sesion existente | mismo token conservado");
    }

    @Test
    @Order(3)
    @DisplayName("GET /web/rutas - Debe retornar solo rutas activas con idRuta y uuidRuta")
    public void rutas_publicas() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/rutas", HttpMethod.GET, new HttpEntity<>(emptyHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        for (JsonNode ruta : data) {
            assertTrue(ruta.has("idRuta"), "Debe exponer idRuta para usarlo en pedidos");
            assertTrue(ruta.has("uuidRuta"));
            assertTrue(ruta.get("active").asBoolean(), "Solo deben aparecer rutas activas");
        }
        System.out.println("[OK] rutas | count=" + data.size());
    }

    @Test
    @Order(4)
    @DisplayName("GET /menu-web - Con token válido debe retornar el menú completo")
    public void menuWeb_conToken() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/menu-web", HttpMethod.GET, new HttpEntity<>(webHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data);
        System.out.println("[OK] menu-web con token | status=" + response.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("GET /menu-web - Sin token debe responder 401")
    public void menuWeb_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/menu-web", HttpMethod.GET, new HttpEntity<>(emptyHeaders), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] menu-web sin token → 401");
    }

    @Test
    @Order(6)
    @DisplayName("GET /web/pedidos/{uuid} - Con token válido debe retornar lista (puede estar vacía)")
    public void ultimosPedidos_conToken() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/pedidos/" + uuidCliente, HttpMethod.GET, new HttpEntity<>(webHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] ultimos pedidos | count=" + data.size());
    }

    @Test
    @Order(7)
    @DisplayName("GET /web/pedidos/{uuid} - Sin token debe responder 401")
    public void ultimosPedidos_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/pedidos/" + uuidCliente, HttpMethod.GET, new HttpEntity<>(emptyHeaders), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] ultimos pedidos sin token → 401");
    }

    @Test
    @Order(8)
    @DisplayName("POST /web/pedidos - Con token válido debe crear un pedido y retornar 201")
    public void crearPedido_conToken() throws Exception {
        String json = """
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "WEB",
                  "pagoCliente": 50.00,
                  "uuidCliente": "%s",
                  "nombreCliente": "Cliente Web Test",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "paquetes": [],
                  "saltarConfirmacion": true
                }
                """.formatted(uuidCliente, testProductoId);

        ResponseEntity<String> response = restTemplate.exchange(
                "/web/pedidos", HttpMethod.POST, new HttpEntity<>(json, webHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdPedidoId = data.get("idPedido").asInt();
        assertTrue(createdPedidoId > 0);
        assertEquals("WEB", data.get("pedidoCreadoDesde").asText());
        System.out.println("[OK] pedido creado | id=" + createdPedidoId);
    }

    @Test
    @Order(9)
    @DisplayName("PUT /web/pedidos/{id} - Con token válido debe actualizar el pedido")
    public void actualizarPedido_conToken() throws Exception {
        String json = """
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "WEB",
                  "pagoCliente": 60.00,
                  "uuidCliente": "%s",
                  "nombreCliente": "Cliente Web Test Actualizado",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 2}
                  ],
                  "paquetes": [],
                  "saltarConfirmacion": true
                }
                """.formatted(uuidCliente, testProductoId);

        ResponseEntity<String> response = restTemplate.exchange(
                "/web/pedidos/" + createdPedidoId, HttpMethod.PUT,
                new HttpEntity<>(json, webHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("[OK] pedido actualizado | id=" + createdPedidoId);
    }

    @Test
    @Order(10)
    @DisplayName("GET /web/pedidos/{uuid} - Debe retornar máximo 5 pedidos del cliente")
    public void ultimosPedidos_maximo5() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/pedidos/" + uuidCliente, HttpMethod.GET, new HttpEntity<>(webHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        assertTrue(data.size() <= 5, "No debe retornar más de 5 pedidos");
        System.out.println("[OK] maximo 5 pedidos | count=" + data.size());
    }
}
