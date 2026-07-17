package com.registrocliente;

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
public class RegistroClienteRestTest {

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
    @DisplayName("GET /registroCliente - Debe retornar lista paginada con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/registroCliente", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.has("content"));
        assertTrue(data.has("totalElements"));
        System.out.println("[OK] " + response.getStatusCode() + " | total=" + data.get("totalElements").asInt());
    }

    @Test
    @Order(2)
    @DisplayName("POST /registroCliente - Debe crear un registro y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "nombre": "María Test REST",
                  "telefono": "5551112222",
                  "direccion": "Calle Prueba 77"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/registroCliente", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idRegistroCliente").asInt();
        assertTrue(createdId > 0);
        assertEquals("María Test REST", data.get("nombre").asText());
        assertEquals("5551112222", data.get("telefono").asText());
        assertTrue(data.get("idRuta").isNull());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " nombre=" + data.get("nombre").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /registroCliente/{id} - Debe retornar el registro con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/registroCliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idRegistroCliente").asInt());
        assertEquals("María Test REST", data.get("nombre").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idRegistroCliente").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("GET /registroCliente/buscar?telefono= - Debe retornar coincidencias paginadas con 200")
    public void buscarPorTelefono() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/registroCliente/buscar?telefono=5551112222", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.get("totalElements").asInt() >= 1);
        System.out.println("[OK] buscar por teléfono → " + data.get("totalElements").asInt() + " resultado(s)");
    }

    @Test
    @Order(5)
    @DisplayName("PUT /registroCliente/{id} - Debe actualizar el registro y retornar 200")
    public void update() throws Exception {
        String json = """
                {
                  "nombre": "María Test Actualizada",
                  "telefono": "5553334444"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/registroCliente/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("María Test Actualizada", data.get("nombre").asText());
        assertEquals("5553334444", data.get("telefono").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombre").asText());
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /registroCliente/{id} - Debe eliminar y retornar 404 al buscarlo de nuevo")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/registroCliente/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/registroCliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para registroCliente id=" + createdId);
    }

    @Test
    @Order(7)
    @DisplayName("GET /registroCliente - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/registroCliente", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
