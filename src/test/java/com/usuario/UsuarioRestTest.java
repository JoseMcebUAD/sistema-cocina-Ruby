package com.usuario;

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
public class UsuarioRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private HttpHeaders authHeadersCocina;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
    @DisplayName("GET /usuario - Debe retornar lista de usuarios con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/usuario", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | usuarios=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /usuario - Debe crear un usuario y retornar status 201 con el nombre correcto")
    public void save() throws Exception {
        String json = """
                {
                  "idRol": 2,
                  "nombreUsuario": "test_tmp_rest",
                  "contrasena": "tmp12"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/usuario", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idUsuario").asInt();
        assertEquals("test_tmp_rest", data.get("nombreUsuario").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " usuario=" + data.get("nombreUsuario").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /usuario/{id} - Debe retornar el usuario correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/usuario/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idUsuario").asInt());
        assertEquals("test_tmp_rest", data.get("nombreUsuario").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idUsuario").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /usuario/{id} - Debe actualizar el usuario y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idRol": 2,
                  "nombreUsuario": "test_tmp_upd",
                  "contrasena": "upd12"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/usuario/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("test_tmp_upd", data.get("nombreUsuario").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | usuario=" + data.get("nombreUsuario").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /usuario/{id} - Debe eliminar el usuario y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/usuario/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/usuario/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para usuario id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /usuario - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/usuario", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }

    @Test
    @Order(7)
    @DisplayName("GET /usuario - con token COCINA debe responder 403")
    public void seguridad_rolCocina() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/usuario", HttpMethod.GET, new HttpEntity<>(authHeadersCocina), String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        System.out.println("[OK] rol COCINA → 403");
    }
}
