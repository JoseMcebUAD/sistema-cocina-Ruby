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

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("ruby");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
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
    @DisplayName("POST /pedido - Debe crear un pedido MOSTRADOR y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "metodoPago": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [],
                  "saltarConfirmacion": true
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idPedido").asInt();
        assertTrue(createdId > 0);
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
        String json = """
                {
                  "metodoPago": "TARJETA",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [],
                  "saltarConfirmacion": true
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("TARJETA", data.get("metodoPago").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | metodoPago=" + data.get("metodoPago").asText());
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
}
