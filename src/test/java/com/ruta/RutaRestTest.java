package com.ruta;

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
public class RutaRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private HttpHeaders authHeadersCocina;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("ruby");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        UserDetails cocina = usuarioDetailsService.loadUserByUsername("ana");
        authHeadersCocina = new HttpHeaders();
        authHeadersCocina.setBearerAuth(jwtService.generarToken(cocina));
        authHeadersCocina.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)
    @DisplayName("GET /ruta - Debe retornar lista de rutas con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/ruta", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | rutas=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /ruta - Debe crear una ruta y retornar status 201 con el nombre correcto")
    public void save() throws Exception {
        String json = """
                {
                  "nombre": "Zona Test Norte",
                  "boundaryWkt": "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))",
                  "isActive": true,
                  "tarifaEnvio": 25.00,
                  "tiempoEstimadoMin": 30
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/ruta", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idRuta").asInt();
        assertEquals("Zona Test Norte", data.get("nombre").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " nombre=" + data.get("nombre").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /ruta/{id} - Debe retornar la ruta correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/ruta/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idRuta").asInt());
        assertEquals("Zona Test Norte", data.get("nombre").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idRuta").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /ruta/{id} - Debe actualizar la ruta y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "nombre": "Zona Test Norte Actualizada",
                  "boundaryWkt": "POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))",
                  "isActive": false,
                  "tarifaEnvio": 30.00,
                  "tiempoEstimadoMin": 45
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/ruta/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Zona Test Norte Actualizada", data.get("nombre").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombre").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /ruta/{id} - Debe eliminar la ruta y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/ruta/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/ruta/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para ruta id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /ruta - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/ruta", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }

    @Test
    @Order(7)
    @DisplayName("GET /ruta - con token COCINA debe responder 403")
    public void seguridad_rolCocina() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/ruta", HttpMethod.GET, new HttpEntity<>(authHeadersCocina), String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        System.out.println("[OK] rol COCINA → 403");
    }
}
