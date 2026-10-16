package com.tarjetatransferencia;

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

/**
 * Tests de integración para los endpoints de tarjetas de transferencia.
 * Los tests son ordenados para controlar el estado de la BD durante el flujo:
 * crear A → crear B → activar A → verificar invariantes → intercambiar a B → limpiar A.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class TarjetaTransferenciaRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private HttpHeaders authHeadersCocina;
    private final ObjectMapper mapper = new ObjectMapper();

    private int idTarjetaA;
    private int idTarjetaB;

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

    // ── CRUD base ─────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("GET /tarjeta-transferencia - Debe retornar lista con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | tarjetas=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /tarjeta-transferencia - Debe crear tarjeta A con estatus NO_DISPONIBLE")
    public void save_tarjetaA() throws Exception {
        String json = """
                {
                  "clabe": "123456789012345678",
                  "numeroTarjeta": "1234567890123456",
                  "nombreDestinatario": "Rubí García Test",
                  "banco": "BBVA"
                }
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        idTarjetaA = data.get("idTarjeta").asInt();
        assertEquals("NO_DISPONIBLE", data.get("estatus").asText());
        assertEquals("BBVA", data.get("banco").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | idA=" + idTarjetaA + " estatus=" + data.get("estatus").asText());
    }

    @Test
    @Order(3)
    @DisplayName("POST /tarjeta-transferencia - Debe crear tarjeta B con estatus NO_DISPONIBLE")
    public void save_tarjetaB() throws Exception {
        String json = """
                {
                  "clabe": "987654321098765432",
                  "numeroTarjeta": "9876543210987654",
                  "nombreDestinatario": "Rubí García Test B",
                  "banco": "SANTANDER"
                }
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        idTarjetaB = data.get("idTarjeta").asInt();
        assertEquals("NO_DISPONIBLE", data.get("estatus").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | idB=" + idTarjetaB + " estatus=" + data.get("estatus").asText());
    }

    // ── Cambio de estatus ─────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("PATCH /{id}/estatus - Activar tarjeta A debe retornar DISPONIBLE y desactivar las demás")
    public void cambiarEstatus_activar_tarjetaA() throws Exception {
        String json = """
                { "estatus": "DISPONIBLE" }
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia/" + idTarjetaA + "/estatus",
                HttpMethod.PATCH, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("DISPONIBLE", data.get("estatus").asText());
        assertEquals(idTarjetaA, data.get("idTarjeta").asInt());
        System.out.println("[OK] " + response.getStatusCode() + " | idA=" + idTarjetaA + " estatus=" + data.get("estatus").asText());
    }

    @Test
    @Order(5)
    @DisplayName("PATCH /{id}/estatus - Enviar AGOTADO debe retornar 400")
    public void cambiarEstatus_AGOTADO_lanza400() {
        String json = """
                { "estatus": "AGOTADO" }
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia/" + idTarjetaA + "/estatus",
                HttpMethod.PATCH, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("[OK] AGOTADO → " + response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("PATCH /{id}/estatus - Desactivar la única tarjeta activa debe retornar 409")
    public void cambiarEstatus_noDisponible_unicaActiva_lanza409() {
        String json = """
                { "estatus": "NO_DISPONIBLE" }
                """;

        // Tras el test 4, A es la única DISPONIBLE (updateAllEstatus desactivó las demás)
        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia/" + idTarjetaA + "/estatus",
                HttpMethod.PATCH, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        System.out.println("[OK] desactivar única activa → " + response.getStatusCode());
    }

    // ── Endpoint web público ──────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("GET /web/tarjeta-transferencia/activa - Sin token debe retornar 200 con los 4 campos sin idTarjeta ni estatus")
    public void web_obtenerActiva() throws Exception {
        // Endpoint público: no se envía token
        ResponseEntity<String> response = restTemplate.exchange(
                "/web/tarjeta-transferencia/activa",
                HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data.get("clabe"));
        assertNotNull(data.get("numeroTarjeta"));
        assertNotNull(data.get("nombreDestinatario"));
        assertNotNull(data.get("banco"));
        // El DTO público no expone idTarjeta ni estatus
        assertNull(data.get("idTarjeta"));
        assertNull(data.get("estatus"));
        System.out.println("[OK] web activa → " + response.getStatusCode() + " banco=" + data.get("banco").asText());
    }

    // ── Invariante DELETE ─────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("DELETE /{id} - Eliminar la única tarjeta activa debe retornar 409")
    public void delete_unicaActiva_lanza409() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia/" + idTarjetaA,
                HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        System.out.println("[OK] delete única activa → " + response.getStatusCode());
    }

    // ── Intercambio de tarjeta activa ─────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("PATCH /{id}/estatus - Activar tarjeta B debe dejar tarjeta A como NO_DISPONIBLE")
    public void cambiarEstatus_activar_tarjetaB_intercambia() throws Exception {
        String json = """
                { "estatus": "DISPONIBLE" }
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia/" + idTarjetaB + "/estatus",
                HttpMethod.PATCH, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("DISPONIBLE", data.get("estatus").asText());
        assertEquals(idTarjetaB, data.get("idTarjeta").asInt());

        // Verificar que A quedó NO_DISPONIBLE tras el intercambio
        ResponseEntity<String> getA = restTemplate.exchange(
                "/tarjeta-transferencia", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        JsonNode lista = mapper.readTree(getA.getBody()).get("data");
        JsonNode tarjetaA = null;
        for (JsonNode t : lista) {
            if (t.get("idTarjeta").asInt() == idTarjetaA) { tarjetaA = t; break; }
        }
        assertNotNull(tarjetaA, "Tarjeta A debe aparecer en el listado");
        assertEquals("NO_DISPONIBLE", tarjetaA.get("estatus").asText());
        System.out.println("[OK] intercambio: idB=" + idTarjetaB + " DISPONIBLE, idA=" + idTarjetaA + " NO_DISPONIBLE");
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("DELETE /{id} - Debe eliminar tarjeta A (NO_DISPONIBLE) con status 204")
    public void delete_tarjetaA_noDisponible() {
        // A quedó NO_DISPONIBLE tras el intercambio del test 9
        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia/" + idTarjetaA,
                HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        System.out.println("[OK] DELETE idA=" + idTarjetaA + " → " + response.getStatusCode());
    }

    // ── Seguridad ─────────────────────────────────────────────────────────────

    @Test
    @Order(11)
    @DisplayName("GET /tarjeta-transferencia - Sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → " + response.getStatusCode());
    }

    @Test
    @Order(12)
    @DisplayName("GET /tarjeta-transferencia - Con rol COCINA debe responder 200")
    public void seguridad_rolCocina() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/tarjeta-transferencia", HttpMethod.GET, new HttpEntity<>(authHeadersCocina), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("[OK] rol COCINA → " + response.getStatusCode());
    }
}
