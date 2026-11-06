package com.oferta;

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
public class OfertaRestTest {

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
    @DisplayName("GET /oferta - Debe retornar lista con status 200")
    void findAll() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | ofertas=" + data.size());
    }

    @Test @Order(2)
    @DisplayName("POST /oferta - Debe crear una oferta con 2 líneas y retornar 201")
    void save() throws Exception {
        String json = """
                {
                  "nombre": "Oferta REST Test",
                  "descripcion": "Comida FIJA + cualquier producto de cocina",
                  "precioOferta": 65.00,
                  "prioridad": 5,
                  "activo": true,
                  "lineas": [
                    { "modoMatch": "COMIDA_POR_TIPO", "tipoComidaFiltro": "FIJA", "cantidad": 1 },
                    { "modoMatch": "PRODUCTO_GENERAL", "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idOferta").asInt();
        assertTrue(createdId > 0);
        assertEquals("Oferta REST Test", data.get("nombre").asText());
        assertEquals(65.00, data.get("precioOferta").asDouble(), 0.01);
        assertTrue(data.get("activo").asBoolean());
        assertEquals(2, data.get("lineas").size());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId
                + " nombre=" + data.get("nombre").asText());
    }

    @Test @Order(3)
    @DisplayName("GET /oferta/{id} - Debe retornar la oferta correspondiente con status 200")
    void findById() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idOferta").asInt());
        assertEquals("Oferta REST Test", data.get("nombre").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idOferta").asInt());
    }

    @Test @Order(4)
    @DisplayName("PUT /oferta/{id} - Debe actualizar nombre, precio y líneas")
    void update() throws Exception {
        String json = """
                {
                  "nombre": "Oferta REST Test Actualizada",
                  "precioOferta": 70.00,
                  "prioridad": 10,
                  "activo": true,
                  "lineas": [
                    { "modoMatch": "COMIDA_POR_TIPO", "tipoComidaFiltro": "FIJA", "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("Oferta REST Test Actualizada", data.get("nombre").asText());
        assertEquals(70.00, data.get("precioOferta").asDouble(), 0.01);
        assertEquals(1, data.get("lineas").size());
        System.out.println("[OK] " + response.getStatusCode() + " | nombre=" + data.get("nombre").asText());
    }

    @Test @Order(5)
    @DisplayName("PATCH /oferta/{id}/toggle-activo - Debe desactivar la oferta")
    void toggleActivo() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta/" + createdId + "/toggle-activo", HttpMethod.PATCH,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertFalse(data.get("activo").asBoolean());
        System.out.println("[OK] " + response.getStatusCode() + " | activo=" + data.get("activo").asBoolean());
    }

    @Test @Order(6)
    @DisplayName("DELETE /oferta/{id} - Sin pedidos asociados debe retornar 204 y 404 al buscarlo")
    void delete() throws Exception {
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/oferta/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = restTemplate.exchange(
                "/oferta/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para oferta id=" + createdId);
    }

    @Test @Order(7)
    @DisplayName("GET /oferta - Sin token debe responder 401")
    void seguridad_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }

    @Test @Order(8)
    @DisplayName("POST /oferta - COMIDA_POR_TIPO sin tipoComidaFiltro debe retornar 400")
    void save_lineaInvalida_retorna400() throws Exception {
        String json = """
                {
                  "nombre": "Oferta Inválida",
                  "precioOferta": 50.00,
                  "prioridad": 0,
                  "activo": true,
                  "lineas": [
                    { "modoMatch": "COMIDA_POR_TIPO", "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("[OK] POST línea inválida → 400");
    }

    @Test @Order(9)
    @DisplayName("POST /oferta - con tiposPedidoRestriccion → aparece en GET /oferta/{id}")
    void save_conRestriccion_aparece_en_respuesta() throws Exception {
        String json = """
                {
                  "nombre": "Oferta Restringida Domicilio",
                  "precioOferta": 60.00,
                  "prioridad": 1,
                  "activo": true,
                  "tiposPedidoRestriccion": ["DOMICILIO"],
                  "lineas": [
                    { "modoMatch": "PRODUCTO_GENERAL", "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> create = restTemplate.exchange(
                "/oferta", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, create.getStatusCode());
        int id = mapper.readTree(create.getBody()).get("data").get("idOferta").asInt();

        ResponseEntity<String> get = restTemplate.exchange(
                "/oferta/" + id, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertEquals(HttpStatus.OK, get.getStatusCode());
        JsonNode data = mapper.readTree(get.getBody()).get("data");
        JsonNode restriccion = data.get("tiposPedidoRestriccion");
        assertNotNull(restriccion, "tiposPedidoRestriccion no debe ser null");
        assertTrue(restriccion.isArray() && restriccion.size() == 1);
        assertEquals("DOMICILIO", restriccion.get(0).asText());

        // Cleanup
        restTemplate.exchange("/oferta/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders), String.class);
        System.out.println("[OK] POST oferta con restricción → tiposPedidoRestriccion=[DOMICILIO]");
    }
}
