package com.subcategoria;

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
public class SubcategoriaRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    // Categoría dedicada del test (nombre único por corrida) para no colisionar con seeder.
    private final String CATEGORIA_TEST = "CAT_SUB_" + System.currentTimeMillis();
    private int idCategoriaTest;
    private int idSubcategoriaCreada;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // POST /categoria: crea la categoría contenedora que usará este test.
        String json = "{\"nombre\":\"" + CATEGORIA_TEST + "\"}";
        ResponseEntity<String> resp = restTemplate.exchange(
                "/categoria", HttpMethod.POST,
                new HttpEntity<>(json, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        idCategoriaTest = mapper.readTree(resp.getBody()).get("data").get("idCategoria").asInt();
        System.out.println("[SETUP] categoría de prueba id=" + idCategoriaTest);
    }

    @AfterAll
    void tearDown() {
        // Limpieza defensiva: por si algún test falló antes de eliminar la subcategoría.
        if (idSubcategoriaCreada > 0) {
            restTemplate.exchange("/subcategoria/" + idSubcategoriaCreada,
                    HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        restTemplate.exchange("/categoria/" + idCategoriaTest,
                HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        System.out.println("[TEARDOWN] limpieza completa");
    }

    @Test
    @Order(1)
    @DisplayName("GET /subcategoria - lista inicial (puede estar vacía o traer datos de otros tests)")
    public void findAll() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(mapper.readTree(response.getBody()).get("data").isArray());
        System.out.println("[OK] GET /subcategoria — 200");
    }

    @Test
    @Order(2)
    @DisplayName("POST /subcategoria - crea vinculada a la categoría de prueba, devuelve 201 con nombreCategoria")
    public void save() throws Exception {
        String json = "{\"nombre\":\"Fría\",\"idCategoria\":" + idCategoriaTest + "}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria", HttpMethod.POST,
                new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        idSubcategoriaCreada = data.get("idSubcategoria").asInt();
        assertTrue(idSubcategoriaCreada > 0);
        assertEquals("Fría", data.get("nombre").asText());
        assertEquals(idCategoriaTest, data.get("idCategoria").asInt());
        assertEquals(CATEGORIA_TEST, data.get("nombreCategoria").asText());
        System.out.println("[OK] POST /subcategoria — id=" + idSubcategoriaCreada);
    }

    @Test
    @Order(3)
    @DisplayName("POST /subcategoria - categoría inexistente responde 404")
    public void save_categoriaInexistente() {
        String json = "{\"nombre\":\"Cualquiera\",\"idCategoria\":999999}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria", HttpMethod.POST,
                new HttpEntity<>(json, authHeaders), String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        System.out.println("[OK] POST /subcategoria con categoría inexistente → 404");
    }

    @Test
    @Order(4)
    @DisplayName("POST /subcategoria - nombre duplicado en la misma categoría responde 409")
    public void save_duplicado() {
        String json = "{\"nombre\":\"fría\",\"idCategoria\":" + idCategoriaTest + "}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria", HttpMethod.POST,
                new HttpEntity<>(json, authHeaders), String.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        System.out.println("[OK] POST /subcategoria con nombre duplicado (case-insensitive) → 409");
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /categoria/{id} - bloqueado con 409 mientras la categoría tenga subcategorías")
    public void delete_categoriaConSubcategorias_bloqueado() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/categoria/" + idCategoriaTest, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        System.out.println("[OK] DELETE /categoria bloqueado por subcategorías → 409");
    }

    @Test
    @Order(6)
    @DisplayName("GET /subcategoria/{id} - retorna la subcategoría creada")
    public void findById() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria/" + idSubcategoriaCreada, HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Fría", data.get("nombre").asText());
        assertEquals(idCategoriaTest, data.get("idCategoria").asInt());
        System.out.println("[OK] GET /subcategoria/" + idSubcategoriaCreada);
    }

    @Test
    @Order(7)
    @DisplayName("PUT /subcategoria/{id} - actualiza nombre respetando la unicidad")
    public void update() throws Exception {
        String json = "{\"nombre\":\"Refrigerada\",\"idCategoria\":" + idCategoriaTest + "}";
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria/" + idSubcategoriaCreada, HttpMethod.PUT,
                new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Refrigerada", data.get("nombre").asText());
        System.out.println("[OK] PUT /subcategoria/" + idSubcategoriaCreada + " → Refrigerada");
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /subcategoria/{id} - elimina y luego GET responde 404")
    public void delete() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria/" + idSubcategoriaCreada, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<String> getResponse = restTemplate.exchange(
                "/subcategoria/" + idSubcategoriaCreada, HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());

        // Evita doble delete en el tearDown.
        idSubcategoriaCreada = 0;
        System.out.println("[OK] DELETE /subcategoria → 204 y luego 404");
    }

    @Test
    @Order(10)
    @DisplayName("GET /subcategoria - sin token responde 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/subcategoria", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] GET /subcategoria sin token → 401");
    }
}
