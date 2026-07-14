package com.codigocliente;

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
public class CodigoClienteRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    private int createdId;

    @BeforeAll
    void setUp() {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("ruby");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(1)
    @DisplayName("GET /codigoCliente - Debe retornar lista de códigos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/codigoCliente", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | códigos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /codigoCliente - Debe crear un código y retornar status 201")
    public void save() throws Exception {
        String json = """
                {
                  "identificador": "Código Test REST",
                  "codigoCliente": "TREST001",
                  "tarifaEspecial": 25.00,
                  "estatus": "DISPONIBLE"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/codigoCliente", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idCodigoCliente").asInt();
        assertTrue(createdId > 0);
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /codigoCliente/{id} - Debe retornar el código correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/codigoCliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idCodigoCliente").asInt());
        assertEquals("TREST001", data.get("codigoCliente").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idCodigoCliente").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /codigoCliente/{id} - Debe actualizar el identificador y tarifa y retornar status 200")
    public void update() throws Exception {
        String json = """
                {
                  "identificador": "Código Test REST Actualizado",
                  "codigoCliente": "TREST001",
                  "tarifaEspecial": 18.50,
                  "estatus": "DISPONIBLE"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/codigoCliente/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Código Test REST Actualizado", data.get("identificador").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | identificador=" + data.get("identificador").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /codigoCliente/{id} - Debe eliminar el código y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/codigoCliente/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/codigoCliente/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para código id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /codigoCliente - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/codigoCliente", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }
}
