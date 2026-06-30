package com.comida;

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
public class ComidaRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

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
    @DisplayName("GET /comida - Debe retornar lista de comidas con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/comida", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | comidas=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /comida - Debe crear una comida y retornar status 201 con el nombre correcto")
    public void save() throws Exception {
        String json = """
                {
                  "uuidComida": "test-uuid-com-rest-001",
                  "nombreComida": "Enchiladas Test",
                  "descripcion": "Con pollo en salsa verde",
                  "precioMedia": 55.00,
                  "precioEntera": 90.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/comida", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idComida").asInt();
        assertEquals("Enchiladas Test", data.get("nombreComida").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " nombre=" + data.get("nombreComida").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /comida/{id} - Debe retornar la comida correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/comida/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idComida").asInt());
        assertEquals("Enchiladas Test", data.get("nombreComida").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idComida").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /comida - Debe actualizar el nombre de la comida y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idComida": %d,
                  "uuidComida": "test-uuid-com-rest-001",
                  "nombreComida": "Enchiladas Verdes Actualizadas",
                  "descripcion": "Con chile verde y queso",
                  "precioMedia": 60.00,
                  "precioEntera": 100.00,
                  "estatus": "DISPONIBLE",
                  "destacado": true
                }
                """.formatted(createdId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/comida", HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Enchiladas Verdes Actualizadas", data.get("nombreComida").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombreComida").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /comida/{id} - Debe eliminar la comida y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/comida/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/comida/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para comida id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /comida - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/comida", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
