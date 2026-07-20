package com.cliente;

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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class ClienteRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;
    private final String uniqueUuid = "test-" + UUID.randomUUID().toString().substring(0, 8);
    private final String uniqueToken = "token-" + UUID.randomUUID().toString().substring(0, 20);

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)
    @DisplayName("GET /cliente - Debe retornar lista de clientes con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/cliente", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | clientes=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /cliente - Debe crear un cliente y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "uuidCliente": "%s",
                  "sessionToken": "%s",
                  "nombre": "Cliente Test",
                  "direccionCliente": "Calle Falsa 123",
                  "telefono": "5551234567"
                }
                """.formatted(uniqueUuid, uniqueToken);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/cliente", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idCliente").asInt();
        assertTrue(createdId > 0);
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /cliente/{id} - Debe retornar el cliente correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/cliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idCliente").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idCliente").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /cliente/{id} - Debe actualizar el cliente y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "uuidCliente": "%s",
                  "sessionToken": "%s",
                  "nombre": "Cliente Test Actualizado",
                  "direccionCliente": "Avenida Siempreviva 742",
                  "telefono": "5559876543"
                }
                """.formatted(uniqueUuid, uniqueToken);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/cliente/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Cliente Test Actualizado", data.get("nombre").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombre").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /cliente/{id} - Debe eliminar el cliente y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/cliente/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/cliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para cliente id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /cliente - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/cliente", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
