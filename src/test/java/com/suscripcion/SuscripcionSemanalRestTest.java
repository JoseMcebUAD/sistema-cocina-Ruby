package com.suscripcion;

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
public class SuscripcionSemanalRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;
    private String codigoRecuperacionCreado;

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)
    @DisplayName("GET /suscripcion-semanal/todos - Debe retornar lista con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/suscripcion-semanal/todos", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | suscripciones=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /suscripcion-semanal - Debe crear suscripción y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "identificadorCliente": "Susana García REST",
                  "semanaInicio": "2025-10-27",
                  "comidasPorDia": 1,
                  "precioSuscripcion": 150.00,
                  "metodoPago": "EFECTIVO",
                  "origen": "COCINA",
                  "activo": true
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/suscripcion-semanal", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idSuscripcionSemanal").asInt();
        codigoRecuperacionCreado = data.get("codigoRecuperacion").asText();
        assertTrue(createdId > 0);
        assertFalse(codigoRecuperacionCreado.isBlank());
        assertEquals(8, codigoRecuperacionCreado.length());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId
                + " | codigo=" + codigoRecuperacionCreado);
    }

    @Test
    @Order(3)
    @DisplayName("GET /suscripcion-semanal/{id} - Debe retornar la suscripción con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/suscripcion-semanal/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idSuscripcionSemanal").asInt());
        assertEquals("Susana García REST", data.get("identificadorCliente").asText());
        assertEquals(1, data.get("comidasPorDia").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idSuscripcionSemanal").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /suscripcion-semanal/{id} - Debe actualizar campos editables y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "identificadorCliente": "Susana García REST Actualizada",
                  "semanaInicio": "2025-10-27",
                  "comidasPorDia": 2,
                  "precioSuscripcion": 200.00,
                  "metodoPago": "TRANSFERENCIA",
                  "origen": "COCINA",
                  "activo": true
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/suscripcion-semanal/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Susana García REST Actualizada", data.get("identificadorCliente").asText());
        assertEquals(2, data.get("comidasPorDia").asInt());
        // codigoRecuperacion debe mantenerse igual al creado
        assertEquals(codigoRecuperacionCreado, data.get("codigoRecuperacion").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | identificadorCliente="
                + data.get("identificadorCliente").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /suscripcion-semanal/{id} - Debe eliminar y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/suscripcion-semanal/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/suscripcion-semanal/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para suscripción id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("POST /suscripcion-semanal - Debe retornar 400 cuando faltan campos requeridos")
    public void save_validacionFalla() throws Exception {
        String json = """
                {
                  "comidasPorDia": 1
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/suscripcion-semanal", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("[OK] POST sin campos requeridos → 400");
    }

    @Test
    @Order(7)
    @DisplayName("GET /suscripcion-semanal - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/suscripcion-semanal", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
