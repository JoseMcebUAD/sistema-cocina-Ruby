package com.produccion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
public class ResumenProduccionRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private int testProductoId;
    private int testPedidoId;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Crea un SNACK para poder verificar que el endpoint detalle/snacks devuelve items reales
        String productoJson = """
                {
                  "nombreProducto": "Snack Test Produccion",
                  "precioDomicilio": 30.00,
                  "precioNormal": 25.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "tipoProducto": "SNACK",
                  "saltarConfirmacion": true
                }
                """;
        ResponseEntity<String> productoResp = restTemplate.exchange(
                "/productoCocina", HttpMethod.POST, new HttpEntity<>(productoJson, authHeaders), String.class
        );
        testProductoId = mapper.readTree(productoResp.getBody()).get("data").get("idProductoCocina").asInt();

        // Crea un pedido MOSTRADOR con el snack (cantidad=2) para que aparezca en el resumen de hoy
        String pedidoJson = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 50.00,
                  "nombreCliente": "Test Produccion",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 25.00, "cantidad": 2}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);
        ResponseEntity<String> pedidoResp = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(pedidoJson, authHeaders), String.class
        );
        testPedidoId = mapper.readTree(pedidoResp.getBody()).get("data").get("idPedido").asInt();

        System.out.println("[SETUP] productoId=" + testProductoId + " pedidoId=" + testPedidoId);
    }

    @AfterAll
    void tearDown() {
        if (testPedidoId > 0) {
            restTemplate.exchange("/pedido/" + testPedidoId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testProductoId > 0) {
            restTemplate.exchange("/productoCocina/" + testProductoId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        System.out.println("[TEARDOWN] datos eliminados: pedidoId=" + testPedidoId + " productoId=" + testProductoId);
    }

    @Test
    @Order(1)
    @DisplayName("GET /produccion/resumen — sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token respondió 401");
    }

    @Test
    @Order(2)
    @DisplayName("GET /produccion/resumen — debe retornar 200 con estructura correcta")
    public void resumenProduccion_retorna200ConEstructura() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data.get("fecha"));
        assertTrue(data.get("totalComidas").asLong() >= 0);
        assertTrue(data.get("totalDesayunos").asLong() >= 0);
        assertTrue(data.get("totalSnacks").asLong() >= 2,
                "totalSnacks debe ser al menos 2 (el pedido del setUp tiene cantidad=2)");
        assertTrue(data.get("totalBebidas").asLong() >= 0);
        assertTrue(data.get("totalCharolas").asLong() >= 0);
        assertTrue(data.get("totalPostres").asLong() >= 0);
        assertTrue(data.get("totalBasicos").asLong() >= 0);
        System.out.println("[OK] resumen 200 | totalSnacks=" + data.get("totalSnacks").asLong()
                + " fecha=" + data.get("fecha").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /produccion/resumen?tipoPedido=MOSTRADOR — debe retornar 200")
    public void resumenProduccion_conTipoPedido_retorna200() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen?tipoPedido=MOSTRADOR", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("[OK] filtro tipoPedido=MOSTRADOR respondió 200");
    }

    @Test
    @Order(4)
    @DisplayName("GET /produccion/resumen?pedidoCreadoDesde=COCINA — debe retornar 200")
    public void resumenProduccion_conPedidoCreadoDesde_retorna200() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen?pedidoCreadoDesde=COCINA", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("[OK] filtro pedidoCreadoDesde=COCINA respondió 200");
    }

    @Test
    @Order(5)
    @DisplayName("GET /produccion/resumen?idRutas=99999 — ruta inexistente debe retornar 200 con ceros")
    public void resumenProduccion_conRutaInexistente_retorna200ConCeros() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen?idRutas=99999", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(0L, data.get("totalSnacks").asLong());
        assertEquals(0L, data.get("totalComidas").asLong());
        System.out.println("[OK] ruta inexistente retornó 200 | totalSnacks=0");
    }

    @Test
    @Order(6)
    @DisplayName("GET /produccion/resumen/detalle/snacks — debe retornar 200 con al menos un item")
    public void detalleProduccion_snacks_retorna200ConItems() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen/detalle/snacks", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("snacks", data.get("categoria").asText());
        assertTrue(data.get("items").isArray());
        assertTrue(data.get("items").size() >= 1, "Debe contener al menos el snack creado en setUp");
        JsonNode primerItem = data.get("items").get(0);
        assertNotNull(primerItem.get("nombre"));
        assertTrue(primerItem.get("cantidad").asInt() > 0);
        System.out.println("[OK] detalle snacks: " + data.get("items").size() + " item(s) | "
                + primerItem.get("nombre").asText() + " x" + primerItem.get("cantidad").asInt());
    }

    @Test
    @Order(7)
    @DisplayName("GET /produccion/resumen/detalle/comidas — debe retornar 200 con estructura correcta")
    public void detalleProduccion_comidas_retorna200() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen/detalle/comidas", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("comidas", data.get("categoria").asText());
        assertTrue(data.get("items").isArray());
        System.out.println("[OK] detalle comidas 200 | items=" + data.get("items").size());
    }

    @Test
    @Order(8)
    @DisplayName("GET /produccion/resumen/detalle/xyz — categoría inválida debe retornar 400")
    public void detalleProduccion_categoriaInvalida_retorna400() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/produccion/resumen/detalle/xyz", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("[OK] categoría inválida retornó 400");
    }
}
