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

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración end-to-end que verifican el ciclo completo de una Oferta
 * aplicada en un pedido: creación de catálogo, aplicación en pedido COCINA (explícito)
 * y restricciones de eliminación.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class OfertaPedidoIntegrationTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper();

    // IDs de catálogo creados en setUp
    private int testComidaId;
    private int testProductoId;
    private int testOfertaId;

    // IDs de pedidos creados en los tests
    private int pedidoConOfertaId;
    private int ofertaInactivaId;
    private int ofertaRestringidaId;

    // Precios de catálogo usados en los tests
    private static final double PRECIO_COMIDA_ENTERA = 45.00;
    private static final double PRECIO_PRODUCTO_NORMAL = 20.00;
    private static final double PRECIO_OFERTA = 55.00;

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Comida FIJA para que el matcher COMIDA_POR_TIPO(FIJA) la detecte
        String comidaJson = String.format(Locale.US, """
                {
                  "uuidComida": "test-uuid-oferta-integ-001",
                  "nombreComida": "Comida Test Oferta Integ",
                  "precioMedia": 30.00,
                  "precioEntera": %.2f,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "limiteComplemento": 2,
                  "tipoComida": "FIJA"
                }
                """, PRECIO_COMIDA_ENTERA);
        ResponseEntity<String> comidaResp = restTemplate.exchange(
                "/comida", HttpMethod.POST, new HttpEntity<>(comidaJson, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, comidaResp.getStatusCode(),
                "Fallo al crear comida de prueba: " + comidaResp.getBody());
        testComidaId = mapper.readTree(comidaResp.getBody()).get("data").get("idComida").asInt();

        // ProductoCocina en idCategoria=1 (BEBIDA, creada por el seeder V23)
        String productoJson = String.format(Locale.US, """
                {
                  "nombreProducto": "Producto Test Oferta Integ",
                  "precioDomicilio": 25.00,
                  "precioNormal": %.2f,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "idCategoria": 1,
                  "idSubcategorias": [],
                  "saltarConfirmacion": true
                }
                """, PRECIO_PRODUCTO_NORMAL);
        ResponseEntity<String> productoResp = restTemplate.exchange(
                "/producto-cocina", HttpMethod.POST, new HttpEntity<>(productoJson, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, productoResp.getStatusCode(),
                "Fallo al crear producto de prueba: " + productoResp.getBody());
        testProductoId = mapper.readTree(productoResp.getBody()).get("data").get("idProductoCocina").asInt();

        // Oferta: COMIDA_POR_TIPO(FIJA) + PRODUCTO_POR_CATEGORIA(1=BEBIDA)
        String ofertaJson = String.format(Locale.US, """
                {
                  "nombre": "Oferta Integ Test",
                  "descripcion": "Comida FIJA + bebida",
                  "precioOferta": %.2f,
                  "prioridad": 5,
                  "activo": true,
                  "lineas": [
                    { "modoMatch": "COMIDA_POR_TIPO", "tipoComidaFiltro": "FIJA", "cantidad": 1 },
                    { "modoMatch": "PRODUCTO_POR_CATEGORIA", "idReferencia": 1, "cantidad": 1 }
                  ]
                }
                """, PRECIO_OFERTA);
        ResponseEntity<String> ofertaResp = restTemplate.exchange(
                "/oferta", HttpMethod.POST, new HttpEntity<>(ofertaJson, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, ofertaResp.getStatusCode(),
                "Fallo al crear oferta de prueba: " + ofertaResp.getBody());
        testOfertaId = mapper.readTree(ofertaResp.getBody()).get("data").get("idOferta").asInt();

        System.out.println("[SETUP] comidaId=" + testComidaId
                + " productoId=" + testProductoId
                + " ofertaId=" + testOfertaId);
    }

    @AfterAll
    void tearDown() {
        // Orden importante: pedido primero (CASCADE elimina oferta_pedido), luego oferta, luego catálogo
        if (pedidoConOfertaId > 0) {
            restTemplate.exchange("/pedido/" + pedidoConOfertaId, HttpMethod.DELETE,
                    new HttpEntity<>(authHeaders), String.class);
        }
        if (ofertaRestringidaId > 0) {
            restTemplate.exchange("/oferta/" + ofertaRestringidaId + "?saltarConfirmacion=true",
                    HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (ofertaInactivaId > 0) {
            restTemplate.exchange("/oferta/" + ofertaInactivaId + "?saltarConfirmacion=true",
                    HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testOfertaId > 0) {
            restTemplate.exchange("/oferta/" + testOfertaId + "?saltarConfirmacion=true",
                    HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testComidaId > 0) {
            restTemplate.exchange("/comida/" + testComidaId + "?saltarConfirmacion=true",
                    HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testProductoId > 0) {
            restTemplate.exchange("/producto-cocina/" + testProductoId,
                    HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        System.out.println("[TEARDOWN] datos de prueba eliminados");
    }

    // -------------------------------------------------------------------------
    // Test 1: COCINA pedido con oferta explícita → 201, ofertasAplicadas poblado
    // -------------------------------------------------------------------------

    @Test @Order(1)
    @DisplayName("COCINA pedido con oferta explícita → 201, ofertasAplicadas.size=1 y precio correcto")
    void cocinaPedido_ofertaExplicita() throws Exception {
        String json = String.format(Locale.US, """
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "nombreCliente": "Test Oferta Aplicada",
                  "comidas": [
                    { "idComida": %d, "tamanoPorcion": "ENTERA", "precioUnitario": %.2f }
                  ],
                  "productosCocina": [
                    { "idProductoCocina": %d, "precioUnitario": %.2f, "cantidad": 1 }
                  ],
                  "ofertas": [
                    {
                      "idOferta": %d,
                      "lineas": [
                        { "tipoEntidad": "COMIDA", "idProducto": %d },
                        { "tipoEntidad": "PRODUCTO_COCINA", "idProducto": %d }
                      ]
                    }
                  ],
                  "saltarConfirmacion": true
                }
                """,
                testComidaId, PRECIO_COMIDA_ENTERA,
                testProductoId, PRECIO_PRODUCTO_NORMAL,
                testOfertaId, testComidaId, testProductoId);

        ResponseEntity<String> response = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        pedidoConOfertaId = data.get("idPedido").asInt();

        JsonNode ofertasAplicadas = data.get("ofertasAplicadas");
        assertNotNull(ofertasAplicadas, "ofertasAplicadas no debe ser null");
        assertFalse(ofertasAplicadas.isNull(), "ofertasAplicadas no debe ser JSON null");
        assertEquals(1, ofertasAplicadas.size(), "Debe haber exactamente 1 oferta aplicada");

        JsonNode aplicada = ofertasAplicadas.get(0);
        assertEquals(testOfertaId, aplicada.get("idOferta").asInt());
        assertEquals(PRECIO_OFERTA, aplicada.get("precioAplicado").asDouble(), 0.01);

        // ahorro = (45 + 20) - 55 = 10
        assertEquals(10.00, aplicada.get("ahorro").asDouble(), 0.01);
        assertEquals(2, aplicada.get("productos").size());

        // Verificar precioNormalConsumido por línea
        JsonNode productos = aplicada.get("productos");
        double totalNormal = 0;
        for (int i = 0; i < productos.size(); i++) {
            totalNormal += productos.get(i).get("precioNormalConsumido").asDouble();
        }
        // suma de precioNormalConsumido debe ser igual al total normal (45 + 20 = 65)
        assertEquals(PRECIO_COMIDA_ENTERA + PRECIO_PRODUCTO_NORMAL, totalNormal, 0.01);

        // precioFinalOrden = (45 + 20) - 10 = 55
        assertEquals(PRECIO_OFERTA, data.get("precioFinalOrden").asDouble(), 0.01);

        System.out.println("[OK] COCINA oferta explícita → pedidoId=" + pedidoConOfertaId
                + " precioFinal=" + data.get("precioFinalOrden").asDouble()
                + " ahorro=" + aplicada.get("ahorro").asDouble());
    }

    // -------------------------------------------------------------------------
    // Test 2: DELETE oferta con pedido asociado → 409 CONFLICT
    // -------------------------------------------------------------------------

    @Test @Order(2)
    @DisplayName("DELETE /oferta/{id} con pedido asociado y sin saltarConfirmacion → 409")
    void deleteOferta_conPedido_retorna409() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/oferta/" + testOfertaId, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders), String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        JsonNode body = mapper.readTree(response.getBody());
        assertTrue(body.get("message").asText().contains("pedidos asociados"),
                "El mensaje debe indicar que la oferta tiene pedidos asociados");
        System.out.println("[OK] DELETE oferta con pedido → 409: " + body.get("message").asText());
    }

    // -------------------------------------------------------------------------
    // Test 3: COCINA pedido con línea que no satisface las reglas → 400
    // -------------------------------------------------------------------------

    @Test @Order(3)
    @DisplayName("COCINA pedido con líneas que no satisfacen las reglas de la oferta → 400")
    void cocinaPedido_lineaInvalida_retorna400() throws Exception {
        // Enviamos la oferta con solo la línea de PRODUCTO_COCINA, omitiendo la comida.
        // El pool lookup pasa, pero validarOfertaMatch falla porque la regla
        // COMIDA_POR_TIPO(FIJA) no está satisfecha.
        String json = String.format(Locale.US, """
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "nombreCliente": "Test Oferta Invalida",
                  "comidas": [
                    { "idComida": %d, "tamanoPorcion": "ENTERA", "precioUnitario": %.2f }
                  ],
                  "productosCocina": [
                    { "idProductoCocina": %d, "precioUnitario": %.2f, "cantidad": 1 }
                  ],
                  "ofertas": [
                    {
                      "idOferta": %d,
                      "lineas": [
                        { "tipoEntidad": "PRODUCTO_COCINA", "idProducto": %d }
                      ]
                    }
                  ],
                  "saltarConfirmacion": true
                }
                """,
                testComidaId, PRECIO_COMIDA_ENTERA,
                testProductoId, PRECIO_PRODUCTO_NORMAL,
                testOfertaId, testProductoId);

        ResponseEntity<String> response = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode errorBody = mapper.readTree(response.getBody());
        String errorMsg = errorBody.get("message").asText();
        assertTrue(errorMsg.contains("COMIDA_POR_TIPO"),
                "El mensaje debe indicar qué regla falló: " + errorMsg);
        assertTrue(errorMsg.contains("cantidad requerida"),
                "El mensaje debe indicar la cantidad esperada: " + errorMsg);
        System.out.println("[OK] COCINA pedido línea inválida → 400: " + errorMsg);
    }

    // -------------------------------------------------------------------------
    // Test 4: COCINA pedido con oferta inactiva → 400
    // -------------------------------------------------------------------------

    @Test @Order(4)
    @DisplayName("COCINA pedido con oferta inactiva → 400")
    void cocinaPedido_ofertaInactiva_retorna400() throws Exception {
        // Crear una oferta y desactivarla
        String ofertaJson = """
                {
                  "nombre": "Oferta Inactiva Test",
                  "precioOferta": 50.00,
                  "prioridad": 0,
                  "activo": false,
                  "lineas": [
                    { "modoMatch": "PRODUCTO_GENERAL", "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> ofertaResp = restTemplate.exchange(
                "/oferta", HttpMethod.POST, new HttpEntity<>(ofertaJson, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, ofertaResp.getStatusCode());
        ofertaInactivaId = mapper.readTree(ofertaResp.getBody()).get("data").get("idOferta").asInt();

        String json = String.format(Locale.US, """
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "nombreCliente": "Test Oferta Inactiva",
                  "productosCocina": [
                    { "idProductoCocina": %d, "precioUnitario": %.2f, "cantidad": 1 }
                  ],
                  "ofertas": [
                    {
                      "idOferta": %d,
                      "lineas": [
                        { "tipoEntidad": "PRODUCTO_COCINA", "idProducto": %d }
                      ]
                    }
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId, PRECIO_PRODUCTO_NORMAL, ofertaInactivaId, testProductoId);

        ResponseEntity<String> response = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = mapper.readTree(response.getBody());
        assertTrue(body.get("message").asText().toLowerCase().contains("activ"),
                "El mensaje debe indicar que la oferta no está activa");
        System.out.println("[OK] COCINA oferta inactiva → 400: " + body.get("message").asText());
    }

    // -------------------------------------------------------------------------
    // Test 5: COCINA pedido con oferta inexistente → 400
    // -------------------------------------------------------------------------

    @Test @Order(5)
    @DisplayName("COCINA pedido con idOferta inexistente → 400")
    void cocinaPedido_ofertaNoExiste_retorna400() throws Exception {
        String json = String.format(Locale.US, """
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "nombreCliente": "Test Oferta No Existe",
                  "productosCocina": [
                    { "idProductoCocina": %d, "precioUnitario": %.2f, "cantidad": 1 }
                  ],
                  "ofertas": [
                    {
                      "idOferta": 999999,
                      "lineas": [
                        { "tipoEntidad": "PRODUCTO_COCINA", "idProducto": %d }
                      ]
                    }
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId, PRECIO_PRODUCTO_NORMAL, testProductoId);

        ResponseEntity<String> response = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("[OK] COCINA oferta inexistente → 400");
    }

    // -------------------------------------------------------------------------
    // Test 6: COCINA pedido MOSTRADOR con oferta restringida a DOMICILIO → 400
    // -------------------------------------------------------------------------

    @Test @Order(6)
    @DisplayName("COCINA pedido MOSTRADOR con oferta restringida a DOMICILIO → 400")
    void cocinaPedido_ofertaRestringidaDomicilio_retorna400() throws Exception {
        // Crear oferta que solo aplica a DOMICILIO
        String ofertaJson = """
                {
                  "nombre": "Oferta Solo Domicilio",
                  "precioOferta": 40.00,
                  "prioridad": 1,
                  "activo": true,
                  "tiposPedidoRestriccion": ["DOMICILIO"],
                  "lineas": [
                    { "modoMatch": "PRODUCTO_GENERAL", "cantidad": 1 }
                  ]
                }
                """;
        ResponseEntity<String> ofertaResp = restTemplate.exchange(
                "/oferta", HttpMethod.POST, new HttpEntity<>(ofertaJson, authHeaders), String.class);
        assertEquals(HttpStatus.CREATED, ofertaResp.getStatusCode(),
                "Fallo al crear oferta restringida: " + ofertaResp.getBody());
        ofertaRestringidaId = mapper.readTree(ofertaResp.getBody()).get("data").get("idOferta").asInt();

        // Pedido MOSTRADOR intentando usar la oferta restringida a DOMICILIO → 400
        String json = String.format(Locale.US, """
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "nombreCliente": "Test Restriccion TipoPedido",
                  "productosCocina": [
                    { "idProductoCocina": %d, "precioUnitario": %.2f, "cantidad": 1 }
                  ],
                  "ofertas": [
                    {
                      "idOferta": %d,
                      "lineas": [
                        { "tipoEntidad": "PRODUCTO_COCINA", "idProducto": %d }
                      ]
                    }
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId, PRECIO_PRODUCTO_NORMAL, ofertaRestringidaId, testProductoId);

        ResponseEntity<String> response = restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = mapper.readTree(response.getBody());
        String msg = body.get("message").asText();
        assertTrue(msg.toLowerCase().contains("domicilio") || msg.toLowerCase().contains("tipo"),
                "El mensaje debe indicar la restricción de tipoPedido: " + msg);
        System.out.println("[OK] oferta restringida a DOMICILIO en pedido MOSTRADOR → 400: " + msg);
    }
}
