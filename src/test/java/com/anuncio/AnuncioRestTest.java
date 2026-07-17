package com.anuncio;

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
public class AnuncioRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private HttpHeaders authHeadersCocina;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
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
    @DisplayName("GET /anuncio - Debe retornar lista de anuncios con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/anuncio", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | anuncios=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /anuncio - Debe crear un anuncio y retornar status 201 con la descripción correcta")
    public void save() throws Exception {
        String json = """
                {
                  "descripcionAnuncio": "Cerrado el lunes por festivo",
                  "color": "#FF5733",
                  "fechaExpiracionAnuncio": "2026-12-31T23:59:59"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/anuncio", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idAnuncio").asInt();
        assertEquals("Cerrado el lunes por festivo", data.get("descripcionAnuncio").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " descripcion=" + data.get("descripcionAnuncio").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /anuncio/{id} - Debe retornar el anuncio correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/anuncio/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idAnuncio").asInt());
        assertEquals("Cerrado el lunes por festivo", data.get("descripcionAnuncio").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idAnuncio").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /anuncio - Debe actualizar la descripción del anuncio y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idAnuncio": %d,
                  "descripcionAnuncio": "Reabrimos el martes",
                  "color": "#00FF00",
                  "fechaExpiracionAnuncio": "2027-01-02T23:59:59"
                }
                """.formatted(createdId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/anuncio", HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Reabrimos el martes", data.get("descripcionAnuncio").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | descripcion=" + data.get("descripcionAnuncio").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /anuncio/{id} - Debe eliminar el anuncio y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/anuncio/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/anuncio/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para anuncio id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /anuncio - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/anuncio", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }

    @Test
    @Order(7)
    @DisplayName("GET /anuncio - con token COCINA debe responder 403")
    public void seguridad_rolCocina() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/anuncio", HttpMethod.GET, new HttpEntity<>(authHeadersCocina), String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        System.out.println("[OK] rol COCINA → 403");
    }
}
