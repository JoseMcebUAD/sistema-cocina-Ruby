package com.inventariocomida;

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
public class InventarioComidaRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;
    private int comidaId = 1;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Obtener un idComida válido de la BD
        ResponseEntity<String> comidaRes = restTemplate.exchange(
                "/comida", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        if (comidaRes.getStatusCode() == HttpStatus.OK) {
            JsonNode comidas = mapper.readTree(comidaRes.getBody()).get("data");
            if (comidas.isArray() && comidas.size() > 0) {
                comidaId = comidas.get(0).get("idComida").asInt();
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("GET /inventarioComida - Debe retornar lista de registros con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/inventarioComida", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | registros=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /inventarioComida - Debe crear un registro y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "idComida": %d,
                  "cantidad": 5,
                  "tipoContadorComida": "UNIDAD"
                }
                """.formatted(comidaId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/inventarioComida", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idInventarioComida").asInt();
        assertTrue(createdId > 0);
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /inventarioComida/{id} - Debe retornar el registro correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/inventarioComida/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idInventarioComida").asInt());
        assertEquals(5, data.get("cantidad").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idInventarioComida").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("GET /inventarioComida/comida/{idComida} - Debe retornar los registros de inventario de la comida")
    public void findByComida() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/inventarioComida/comida/" + comidaId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        assertTrue(data.size() >= 1);
        System.out.println("[OK] " + response.getStatusCode() + " | registros comida id=" + comidaId + " → " + data.size());
    }

    @Test
    @Order(5)
    @DisplayName("PUT /inventarioComida/{id} - Debe actualizar el registro y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idComida": %d,
                  "cantidad": 12,
                  "tipoContadorComida": "KILOGRAMO"
                }
                """.formatted(comidaId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/inventarioComida/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(12, data.get("cantidad").asInt());
        assertEquals("KILOGRAMO", data.get("tipoContadorComida").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | cantidad=" + data.get("cantidad").asInt());
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /inventarioComida/{id} - Debe eliminar el registro y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/inventarioComida/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/inventarioComida/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para inventario id=" + createdId);
    }

    @Test
    @Order(7)
    @DisplayName("GET /inventarioComida - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/inventarioComida", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
