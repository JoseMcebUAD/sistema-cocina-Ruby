package com.combo;

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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
                classes = com.cocinarubi.Application.class)
public class ComboRestTest {

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

    @Test @Order(1)
    @DisplayName("POST /combo - Debe crear un combo de prueba con status 201")
    public void save() throws Exception {
        String json = """
                {
                  "precio": 99.00,
                  "descripcion": "Combo REST Test",
                  "destacado": false,
                  "estatus": "DISPONIBLE",
                  "productos": [
                    { "tipoProducto": "COMIDA", "idProducto": 1, "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/combo", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idCombo").asInt();
        assertFalse(data.get("destacado").asBoolean());
        assertEquals("DISPONIBLE", data.get("estatus").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test @Order(2)
    @DisplayName("PUT /combo/{id} - Debe actualizar descripcion y precio del combo")
    public void update() throws Exception {
        String json = """
                {
                  "precio": 149.00,
                  "descripcion": "Combo REST Test Actualizado",
                  "destacado": false,
                  "estatus": "DISPONIBLE",
                  "productos": [
                    { "tipoProducto": "COMIDA", "idProducto": 1, "cantidad": 2 }
                  ]
                }
                """.formatted();
        ResponseEntity<String> response = restTemplate.exchange(
                "/combo/" + createdId, HttpMethod.PUT,
                new HttpEntity<>(json, authHeaders), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Combo REST Test Actualizado", data.get("descripcion").asText());
        assertEquals(149.00, data.get("precio").asDouble(), 0.01);
        assertEquals(1, data.get("productos").size());
        assertEquals(2, data.get("productos").get(0).get("cantidad").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | desc=" + data.get("descripcion").asText());
    }

    @Test @Order(3)
    @DisplayName("PUT /combo/{id} - Debe agregar una segunda comida al combo")
    public void updateAgregaComida() throws Exception {
        String json = """
                {
                  "precio": 149.00,
                  "descripcion": "Combo REST Test Actualizado",
                  "destacado": false,
                  "estatus": "DISPONIBLE",
                  "productos": [
                    { "tipoProducto": "COMIDA", "idProducto": 1, "cantidad": 2 },
                    { "tipoProducto": "COMIDA", "idProducto": 2, "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/combo/" + createdId, HttpMethod.PUT,
                new HttpEntity<>(json, authHeaders), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(2, data.get("productos").size());
        System.out.println("[OK] " + response.getStatusCode() + " | productos=" + data.get("productos").size());
    }

    @Test @Order(4)
    @DisplayName("PUT /combo/destacado/{id} - Debe invertir el campo destacado")
    public void toggleDestacado() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/combo/destacado/" + createdId, HttpMethod.PUT,
                new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.get("destacado").asBoolean());
        System.out.println("[OK] destacado=" + data.get("destacado").asBoolean());
    }

    @Test @Order(5)
    @DisplayName("PUT /combo/estatus/{id}?estatus=NO_DISPONIBLE - Debe actualizar el estatus")
    public void updateEstatus() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/combo/estatus/" + createdId + "?estatus=NO_DISPONIBLE", HttpMethod.PUT,
                new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("NO_DISPONIBLE", data.get("estatus").asText());
        System.out.println("[OK] estatus=" + data.get("estatus").asText());
    }

    @Test @Order(6)
    @DisplayName("DELETE /combo/{id} - Limpia el combo de prueba")
    public void delete() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/combo/" + createdId + "?saltarConfirmacion=true",
                HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("[OK] DELETE 200 | combo id=" + createdId + " eliminado");
    }
}
