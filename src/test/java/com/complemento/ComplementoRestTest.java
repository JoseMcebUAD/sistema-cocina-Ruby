package com.complemento;

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
public class ComplementoRestTest {

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
    @DisplayName("GET /complemento - Debe retornar lista de complementos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/complemento", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | complementos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /complemento - Debe crear un complemento y retornar status 201 con el nombre correcto")
    public void save() throws Exception {
        String json = """
                {
                  "uuidComplemento": "test-uuid-comp-rest-001",
                  "nombreComplemento": "Arroz Test",
                  "descripcion": "Arroz blanco de acompañamiento",
                  "precioExtra": 0.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "cobrarSiempre": false
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/complemento", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idComplemento").asInt();
        assertEquals("Arroz Test", data.get("nombreComplemento").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " nombre=" + data.get("nombreComplemento").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /complemento/{id} - Debe retornar el complemento correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/complemento/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idComplemento").asInt());
        assertEquals("Arroz Test", data.get("nombreComplemento").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idComplemento").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /complemento - Debe actualizar el nombre del complemento y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idComplemento": %d,
                  "uuidComplemento": "test-uuid-comp-rest-001",
                  "nombreComplemento": "Arroz Integral Actualizado",
                  "descripcion": "Versión integral",
                  "precioExtra": 5.00,
                  "estatus": "DISPONIBLE",
                  "destacado": true,
                  "cobrarSiempre": false
                }
                """.formatted(createdId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/complemento", HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Arroz Integral Actualizado", data.get("nombreComplemento").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombreComplemento").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /complemento/{id} - Debe eliminar el complemento y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/complemento/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/complemento/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para complemento id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /complemento - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/complemento", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
