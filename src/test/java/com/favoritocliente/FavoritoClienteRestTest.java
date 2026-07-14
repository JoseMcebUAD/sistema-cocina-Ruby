package com.favoritocliente;

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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class FavoritoClienteRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;
    private int clienteId;
    private int comidaId = 1;
    private String sessionToken;
    private final String uniqueUuid = "test-" + UUID.randomUUID().toString().substring(0, 8);
    private final String uniqueToken = "fav-token-" + UUID.randomUUID().toString().substring(0, 16);

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("ruby");
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

        // Crear un cliente de soporte para los tests de favoritos
        sessionToken = uniqueToken;
        String clienteJson = """
                {
                  "uuidCliente": "%s",
                  "sessionToken": "%s",
                  "nombre": "Cliente Test Favoritos"
                }
                """.formatted(uniqueUuid, sessionToken);
        ResponseEntity<String> clienteRes = restTemplate.exchange(
                "/cliente", HttpMethod.POST, new HttpEntity<>(clienteJson, authHeaders), String.class);
        if (clienteRes.getStatusCode() == HttpStatus.CREATED) {
            clienteId = mapper.readTree(clienteRes.getBody()).get("data").get("idCliente").asInt();
        }
    }

    @AfterAll
    void tearDown() {
        if (clienteId > 0) {
            restTemplate.exchange(
                    "/cliente/" + clienteId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
    }

    @Test
    @Order(1)
    @DisplayName("GET /favoritoCliente - Debe retornar lista de favoritos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/favoritoCliente", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | favoritos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /favoritoCliente - Debe crear un favorito y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "sessionToken": "%s",
                  "idProducto": %d,
                  "tipoCatalogoProducto": "COMIDA",
                  "saltarConfirmacion": true
                }
                """.formatted(sessionToken, comidaId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/favoritoCliente", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idFavoritoCliente").asInt();
        assertTrue(createdId > 0);
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /favoritoCliente/{id} - Debe retornar el favorito correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/favoritoCliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idFavoritoCliente").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idFavoritoCliente").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("GET /favoritoCliente/cliente/{sessionToken} - Debe retornar los favoritos del cliente")
    public void findBySessionToken() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/favoritoCliente/cliente/" + sessionToken, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        assertTrue(data.size() >= 1);
        System.out.println("[OK] " + response.getStatusCode() + " | favoritos del cliente=" + data.size());
    }

    @Test
    @Order(5)
    @DisplayName("PUT /favoritoCliente/{id} - Debe actualizar el favorito y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "sessionToken": "%s",
                  "idProducto": %d,
                  "tipoCatalogoProducto": "COMIDA",
                  "saltarConfirmacion": true
                }
                """.formatted(sessionToken, comidaId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/favoritoCliente/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idFavoritoCliente").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idFavoritoCliente").asInt());
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /favoritoCliente/{id} - Debe eliminar el favorito y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/favoritoCliente/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/favoritoCliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para favorito id=" + createdId);
    }

    @Test
    @Order(7)
    @DisplayName("GET /favoritoCliente - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/favoritoCliente", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
