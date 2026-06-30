package com.pagorepartidor;

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

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class PagoRepartidorRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
    @DisplayName("GET /pago-repartidor - Debe retornar lista de pagos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pago-repartidor", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | pagos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /pago-repartidor - Debe crear un pago y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "pago": 150.00,
                  "fechaPago": "2026-06-15T18:00:00"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pago-repartidor", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idPagoRepartidor").asInt();
        assertEquals("150.0", data.get("pago").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " pago=" + data.get("pago").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /pago-repartidor/{id} - Debe retornar el pago correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pago-repartidor/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idPagoRepartidor").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idPagoRepartidor").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /pago-repartidor - Debe actualizar el pago y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idPagoRepartidor": %d,
                  "pago": 200.00,
                  "fechaPago": "2026-06-20T10:00:00"
                }
                """.formatted(createdId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pago-repartidor", HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("200.0", data.get("pago").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | pago=" + data.get("pago").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /pago-repartidor/{id} - Debe eliminar el pago y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/pago-repartidor/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/pago-repartidor/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para pago id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /pago-repartidor - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pago-repartidor", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
