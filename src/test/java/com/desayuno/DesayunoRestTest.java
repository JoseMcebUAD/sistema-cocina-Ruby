package com.desayuno;

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
public class DesayunoRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)
    @DisplayName("GET /desayuno - Debe retornar lista de desayunos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/desayuno/todos", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | desayunos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /desayuno - Debe crear un desayuno y retornar status 201 con el nombre correcto")
    public void save() throws Exception {
        String json = """
                {
                  "uuidDesayuno": "test-uuid-des-rest-001",
                  "nombreDesayuno": "Chilaquiles Test",
                  "descripcion": "Con pollo y crema",
                  "precioMedia": 35.00,
                  "precioEntera": 60.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/desayuno", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idDesayuno").asInt();
        assertEquals("Chilaquiles Test", data.get("nombreDesayuno").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " nombre=" + data.get("nombreDesayuno").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /desayuno/{id} - Debe retornar el desayuno correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/desayuno/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idDesayuno").asInt());
        assertEquals("Chilaquiles Test", data.get("nombreDesayuno").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idDesayuno").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /desayuno - Debe actualizar el nombre del desayuno y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idDesayuno": %d,
                  "uuidDesayuno": "test-uuid-des-rest-001",
                  "nombreDesayuno": "Chilaquiles Rojos Actualizados",
                  "descripcion": "Con chile rojo y queso",
                  "precioMedia": 40.00,
                  "precioEntera": 70.00,
                  "estatus": "DISPONIBLE",
                  "destacado": true
                }
                """.formatted(createdId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/desayuno", HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Chilaquiles Rojos Actualizados", data.get("nombreDesayuno").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombreDesayuno").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /desayuno/{id} - Debe eliminar el desayuno y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/desayuno/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/desayuno/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para desayuno id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /desayuno - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/desayuno", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
