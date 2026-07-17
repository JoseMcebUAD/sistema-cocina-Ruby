package com.productococina;

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
public class ProductoCocinaRestTest {

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
    @DisplayName("GET /productoCocina - Debe retornar lista de productos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/productoCocina", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | productos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /productoCocina - Debe crear un producto y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "nombreProducto": "Snack Test REST",
                  "descripcion": "Snack de prueba para test",
                  "precioDomicilio": 35.00,
                  "precioNormal": 25.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "tipoProducto": "SNACK",
                  "saltarConfirmacion": true
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/productoCocina", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idProductoCocina").asInt();
        assertTrue(createdId > 0);
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /productoCocina/{id} - Debe retornar el producto correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/productoCocina/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idProductoCocina").asInt());
        assertEquals("Snack Test REST", data.get("nombreProducto").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idProductoCocina").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /productoCocina/{id} - Debe actualizar el producto y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "nombreProducto": "Snack Test REST Actualizado",
                  "descripcion": "Descripción actualizada",
                  "precioDomicilio": 40.00,
                  "precioNormal": 30.00,
                  "estatus": "DISPONIBLE",
                  "destacado": true,
                  "tipoProducto": "SNACK",
                  "saltarConfirmacion": true
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/productoCocina/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Snack Test REST Actualizado", data.get("nombreProducto").asText());
        assertTrue(data.get("destacado").asBoolean());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombreProducto").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /productoCocina/{id} - Debe eliminar el producto y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/productoCocina/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/productoCocina/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para producto id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /productoCocina - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/productoCocina", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
