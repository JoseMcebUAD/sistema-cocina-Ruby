package com.horarioatencion;

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
public class HorarioAtencionRestTest {

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
    @DisplayName("GET /horario-atencion - Debe retornar lista de horarios con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/horario-atencion/todos", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | horarios=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /horario-atencion - Debe crear un horario y retornar status 201 con el día correcto")
    public void save() throws Exception {
        String json = """
                {
                  "horaInicioAtencionComidas": "08:30:00",
                  "horaCierreAtencionComidas": "15:30:00",
                  "diaSemana": "X",
                  "tipoHorario": "COMIDAS",
                  "atendiendo": true
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/horario-atencion", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idHorarioAtencionComidas").asInt();
        assertEquals("X", data.get("diaSemana").asText());
        assertTrue(data.get("atendiendo").asBoolean());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " día=" + data.get("diaSemana").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /horario-atencion/{id} - Debe retornar el horario correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/horario-atencion/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idHorarioAtencionComidas").asInt());
        assertEquals("X", data.get("diaSemana").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idHorarioAtencionComidas").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /horario-atencion - Debe actualizar el horario y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idHorarioAtencionComidas": %d,
                  "horaInicioAtencionComidas": "09:00:00",
                  "horaCierreAtencionComidas": "14:00:00",
                  "diaSemana": "X",
                  "tipoHorario": "COMIDAS",
                  "atendiendo": false
                }
                """.formatted(createdId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/horario-atencion", HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertFalse(data.get("atendiendo").asBoolean());
        System.out.println("[OK] " + response.getStatusCode() + " | atendiendo=" + data.get("atendiendo").asBoolean());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /horario-atencion/{id} - Debe eliminar el horario y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/horario-atencion/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/horario-atencion/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para horario id=" + createdId);
    }
}
