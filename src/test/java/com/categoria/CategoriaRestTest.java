package com.categoria;

import com.cocinarubi.presentation.security.JwtService;
import com.cocinarubi.presentation.security.UsuarioDetailsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class CategoriaRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;
    // Nombre único por corrida para no colisionar con seeder ni ejecuciones previas.
    private final String NOMBRE_TEST = "CAT_TEST_" + System.currentTimeMillis();

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)
    @DisplayName("GET /categoria - retorna las 4 categorías seed (BEBIDA, CHAROLA, SNACK, POSTRE)")
    public void findAll() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        assertTrue(data.size() >= 4, "Se esperaban al menos 4 categorías seed");
        System.out.println("[OK] GET /categoria — " + data.size() + " categorías");
    }

    @Test
    @Order(2)
    @DisplayName("POST /categoria - crea una categoría nueva y devuelve 201")
    public void save() throws Exception {
        String json = "{\"nombre\":\"" + NOMBRE_TEST + "\"}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria", HttpMethod.POST,
                new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idCategoria").asInt();
        assertTrue(createdId > 0);
        assertEquals(NOMBRE_TEST, data.get("nombre").asText());
        System.out.println("[OK] POST /categoria — id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("POST /categoria - nombre vacío responde 400")
    public void save_nombreVacio() {
        String json = "{\"nombre\":\"\"}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria", HttpMethod.POST,
                new HttpEntity<>(json, authHeaders), String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("[OK] POST /categoria con nombre vacío → 400");
    }

    @Test
    @Order(4)
    @DisplayName("POST /categoria - nombre duplicado case-insensitive responde 409")
    public void save_duplicadoCaseInsensitive() {
        String json = "{\"nombre\":\"" + NOMBRE_TEST.toLowerCase() + "\"}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria", HttpMethod.POST,
                new HttpEntity<>(json, authHeaders), String.class
        );
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        System.out.println("[OK] POST /categoria con duplicado → 409");
    }

    @Test
    @Order(5)
    @DisplayName("GET /categoria/{id} - retorna la categoría creada")
    public void findById() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria/" + createdId, HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idCategoria").asInt());
        assertEquals(NOMBRE_TEST, data.get("nombre").asText());
        System.out.println("[OK] GET /categoria/" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("PUT /categoria/{id} - actualiza el nombre")
    public void update() throws Exception {
        String nuevoNombre = NOMBRE_TEST + "_UPD";
        String json = "{\"nombre\":\"" + nuevoNombre + "\"}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria/" + createdId, HttpMethod.PUT,
                new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(nuevoNombre, data.get("nombre").asText());
        System.out.println("[OK] PUT /categoria/" + createdId + " → " + nuevoNombre);
    }

    @Test
    @Order(8)
    @DisplayName("GET /categoria/con-subcategorias - retorna árbol con lista de subcategorias por cada categoría")
    public void findAllConSubcategorias() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria/con-subcategorias", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        assertTrue(data.size() >= 4);
        // Cada elemento debe tener la propiedad "subcategorias" (posiblemente vacía).
        for (JsonNode nodo : data) {
            assertTrue(nodo.has("subcategorias"), "Falta campo subcategorias en " + nodo);
            assertTrue(nodo.get("subcategorias").isArray());
        }
        System.out.println("[OK] GET /categoria/con-subcategorias — " + data.size() + " categorías con árbol");
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /categoria/{id} - elimina la categoría creada y devuelve 204")
    public void delete() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria/" + createdId, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<String> getResponse = restTemplate.exchange(
                "/categoria/" + createdId, HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE /categoria/" + createdId + " → 204 y luego 404");
    }

    @Test
    @Order(10)
    @DisplayName("GET /categoria - sin token responde 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] GET /categoria sin token → 401");
    }
}
