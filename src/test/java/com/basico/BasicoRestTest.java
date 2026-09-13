package com.basico;

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
public class BasicoRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;
    private int comidaId;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        String testUuid = "test-basico-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String comidaJson = """
                {
                  "uuidComida": "%s",
                  "nombreComida": "Arroz Test Basico",
                  "descripcion": "Comida de prueba para BasicoRestTest",
                  "precioMedia": 45.00,
                  "precioEntera": 80.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false
                }
                """.formatted(testUuid);
        ResponseEntity<String> res = restTemplate.exchange(
                "/comida", HttpMethod.POST, new HttpEntity<>(comidaJson, authHeaders), String.class);
        JsonNode dataNode = mapper.readTree(res.getBody()).get("data");
        assertNotNull(dataNode, "POST /comida falló. Respuesta: " + res.getBody());
        comidaId = dataNode.get("idComida").asInt();
    }

    @AfterAll
    void tearDown() {
        if (comidaId > 0) {
            restTemplate.exchange(
                    "/comida/" + comidaId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
    }

    @Test
    @Order(1)
    @DisplayName("GET /basico - Debe retornar lista de básicos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/basico/todos", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "GET /basico/todos falló. Body: " + response.getBody());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data, "El campo 'data' no está en la respuesta: " + response.getBody());
        assertTrue(data.isArray(), "Se esperaba un array en 'data', tipo recibido: " + data.getNodeType());
        System.out.println("[OK] " + response.getStatusCode() + " | basicos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /basico - Debe crear un básico y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "idComida": %d,
                  "descripcion": "Paquete con arroz y agua",
                  "destacado": false,
                  "precioBasico": 65.00,
                  "estatus": "DISPONIBLE",
                  "idComplementos": [],
                  "saltarConfirmacion": false
                }
                """.formatted(comidaId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/basico", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "POST /basico falló. Body: " + response.getBody());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data, "El campo 'data' no está en la respuesta: " + response.getBody());
        createdId = data.get("idBasico").asInt();
        assertTrue(createdId > 0, "idBasico inválido tras crear básico. Body: " + response.getBody());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId + " comida=" + data.get("nombreComida").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /basico/{id} - Debe retornar el básico correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/basico/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "GET /basico/" + createdId + " falló. Body: " + response.getBody());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data, "El campo 'data' no está en la respuesta: " + response.getBody());
        assertEquals(createdId, data.get("idBasico").asInt(),
                "El idBasico retornado no coincide con el solicitado. Body: " + response.getBody());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idBasico").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /basico/{id} - Debe actualizar el básico y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "idComida": %d,
                  "descripcion": "Paquete actualizado con frijoles",
                  "destacado": true,
                  "precioBasico": 70.00,
                  "estatus": "DISPONIBLE",
                  "idComplementos": [],
                  "saltarConfirmacion": false
                }
                """.formatted(comidaId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/basico/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "PUT /basico/" + createdId + " falló. Body: " + response.getBody());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data, "El campo 'data' no está en la respuesta: " + response.getBody());
        assertEquals("70.0", data.get("precioBasico").asText(),
                "El precioBasico no fue actualizado correctamente. Body: " + response.getBody());
        System.out.println("[OK] " + response.getStatusCode() + " | precioBasico=" + data.get("precioBasico").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /basico/{id} - Debe eliminar el básico y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/basico/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode(),
                "DELETE /basico/" + createdId + " falló. Body: " + deleteResponse.getBody());
        JsonNode deleteData = mapper.readTree(deleteResponse.getBody());
        assertEquals(200, deleteData.get("status").asInt(),
                "Se esperaba status=200 en body de DELETE. Body: " + deleteResponse.getBody());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/basico/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode(),
                "Se esperaba 404 tras eliminar básico id=" + createdId + ". Body: " + getResponse.getBody());
        System.out.println("[OK] DELETE 200 → GET 404 para basico id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /basico - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/basico", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Se esperaba 401 sin token en GET /basico. Body: " + response.getBody());
        System.out.println("[OK] sin token → 401");
    }
}
