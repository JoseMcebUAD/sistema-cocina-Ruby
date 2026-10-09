package com.pedido;

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
public class PedidoRestTest {

    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private int createdId;
    private int testProductoId;
    private int testRegistroClienteId;
    private int testRutaId;
    private int createdCocinaPickUpId;
    private int createdCocinaDomicilioId;
    private int createdSinMetodoPagoId;

    // ── campos para tests de complementos (escenarios 1-5) ───────────────────
    private int testComida1Id;            // limiteComplemento=1
    private int testComida3Id;            // limiteComplemento=3
    private int compAId;                  // cobrarSiempre=false, precioExtra=3.00
    private int compBId;                  // cobrarSiempre=false, precioExtra=3.00
    private int compCId;                  // cobrarSiempre=false, precioExtra=3.00
    private int compDId;                  // cobrarSiempre=false, precioExtra=3.00
    private int compEId;                  // cobrarSiempre=false, precioExtra=3.00
    private int compCSId;                 // cobrarSiempre=true,  precioExtra=5.00
    private int pedidoComplementos3Id;    // pedido creado en escenario 3, reutilizado en 4 y 5

    @BeforeAll
    void setUp() throws Exception {
        UserDetails jefa = usuarioDetailsService.loadUserByUsername("rubi");
        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwtService.generarToken(jefa));
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        // La ruta se crea primero para que el cliente pueda referenciarla
        String rutaJson = """
                {
                  "nombre": "Ruta Test Pedido",
                  "boundaryWkt": "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))",
                  "isActive": true,
                  "tarifaEnvio": 40.00,
                  "tiempoEstimadoMin": 20
                }
                """;
        ResponseEntity<String> rutaResp = restTemplate.exchange(
                "/ruta", HttpMethod.POST, new HttpEntity<>(rutaJson, authHeaders), String.class
        );
        testRutaId = mapper.readTree(rutaResp.getBody()).get("data").get("idRuta").asInt();

        String clienteJson = String.format("""
                {
                  "nombre": "Cliente Test Pedido",
                  "telefono": "5550009999",
                  "idRuta": %d,
                  "direccion": "{\\"calle\\": \\"Principal 42\\", \\"interior\\": \\"Int 3\\"}"
                }
                """, testRutaId);
        ResponseEntity<String> clienteResp = restTemplate.exchange(
                "/registroCliente", HttpMethod.POST, new HttpEntity<>(clienteJson, authHeaders), String.class
        );
        testRegistroClienteId = mapper.readTree(clienteResp.getBody()).get("data").get("idRegistroCliente").asInt();

        // idCategoria=1 → BEBIDA en el seeder de V23
        String productoJson = """
                {
                  "nombreProducto": "Agua Test Pedido",
                  "precioDomicilio": 15.00,
                  "precioNormal": 10.00,
                  "estatus": "DISPONIBLE",
                  "destacado": false,
                  "idCategoria": 1,
                  "idSubcategorias": [],
                  "saltarConfirmacion": true
                }
                """;
        ResponseEntity<String> productoResp = restTemplate.exchange(
                "/producto-cocina", HttpMethod.POST, new HttpEntity<>(productoJson, authHeaders), String.class
        );
        testProductoId = mapper.readTree(productoResp.getBody()).get("data").get("idProductoCocina").asInt();

        System.out.println("[SETUP] productoId=" + testProductoId
                + " clienteId=" + testRegistroClienteId
                + " rutaId=" + testRutaId);
    }

    @AfterAll
    void tearDown() {
        if (createdCocinaPickUpId > 0) {
            restTemplate.exchange("/pedido/" + createdCocinaPickUpId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (createdCocinaDomicilioId > 0) {
            restTemplate.exchange("/pedido/" + createdCocinaDomicilioId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (createdSinMetodoPagoId > 0) {
            restTemplate.exchange("/pedido/" + createdSinMetodoPagoId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testProductoId > 0) {
            restTemplate.exchange("/producto-cocina/" + testProductoId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testRegistroClienteId > 0) {
            restTemplate.exchange("/registroCliente/" + testRegistroClienteId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        if (testRutaId > 0) {
            restTemplate.exchange("/ruta/" + testRutaId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        }
        System.out.println("[TEARDOWN] datos de prueba eliminados");
    }

    @Test
    @Order(1)
    @DisplayName("GET /pedido - Debe retornar lista de pedidos con status 200")
    public void findAll() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertTrue(data.isArray());
        System.out.println("[OK] " + response.getStatusCode() + " | pedidos=" + data.size());
    }

    @Test
    @Order(2)
    @DisplayName("POST /pedido - Debe crear un pedido COCINA MOSTRADOR y retornar status 201")
    public void save() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 50.00,
                  "nombreCliente": "Test REST",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdId = data.get("idPedido").asInt();
        assertTrue(createdId > 0);
        assertEquals("MOSTRADOR", data.get("tipoPedido").asText());
        assertNotNull(data.get("pedidoCocina"));
        assertEquals("Test REST", data.get("pedidoCocina").get("nombreCliente").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + createdId);
    }

    @Test
    @Order(3)
    @DisplayName("GET /pedido/{id} - Debe retornar el pedido correspondiente al ID con status 200")
    public void findById() throws Exception {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals(createdId, data.get("idPedido").asInt());
        assertEquals("MOSTRADOR", data.get("tipoPedido").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | id=" + data.get("idPedido").asInt());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /pedido/{id} - Debe actualizar el pedido y retornar status 200")
    public void update() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "TARJETA",
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "nombreCliente": "Test REST",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.PUT, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertEquals("TARJETA", data.get("metodoPagoPrincipal").asText());
        System.out.println("[OK] " + response.getStatusCode() + " | metodoPagoPrincipal=" + data.get("metodoPagoPrincipal").asText());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /pedido/{id} - Debe eliminar el pedido y retornar 404 al buscarlo nuevamente")
    public void delete() throws Exception {
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = this.restTemplate.exchange(
                "/pedido/" + createdId, HttpMethod.GET, new HttpEntity<>(authHeaders), String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        System.out.println("[OK] DELETE 204 → GET 404 para pedido id=" + createdId);
    }

    @Test
    @Order(6)
    @DisplayName("GET /pedido - sin token debe responder 401")
    public void seguridad_sinToken() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] sin token → 401");
    }

    @Test
    @Order(7)
    @DisplayName("POST /pedido - COCINA PICK_UP con nombreCliente debe retornar 201")
    public void saveCocina_pickUp() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "PICK_UP",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 30.00,
                  "nombreCliente": "Ana García",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdCocinaPickUpId = data.get("idPedido").asInt();
        assertTrue(createdCocinaPickUpId > 0);
        assertEquals("PICK_UP", data.get("tipoPedido").asText());
        assertFalse(data.get("pedidoCocina").isNull());
        assertEquals("Ana García", data.get("pedidoCocina").get("nombreCliente").asText());
        assertTrue(data.get("domicilioCocina").isNull());
        System.out.println("[OK] COCINA+PICK_UP id=" + createdCocinaPickUpId
                + " nombreCliente=" + data.get("pedidoCocina").get("nombreCliente").asText());
    }

    @Test
    @Order(8)
    @DisplayName("POST /pedido - COCINA DOMICILIO con pedidoDomicilioCocina debe retornar 201 con datos del cliente")
    public void saveCocina_domicilio() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "DOMICILIO",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 80.00,
                  "pedidoDomicilioCocina": {
                    "idRegistroCliente": %d,
                    "tarifa": 40.00,
                    "domicilio": "Calle Principal 42 Int 3",
                    "idRuta": %d
                  },
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testRegistroClienteId, testRutaId, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdCocinaDomicilioId = data.get("idPedido").asInt();
        assertTrue(createdCocinaDomicilioId > 0);
        assertEquals("DOMICILIO", data.get("tipoPedido").asText());

        JsonNode domicilioCocina = data.get("domicilioCocina");
        assertFalse(domicilioCocina.isNull());
        assertEquals(testRegistroClienteId, domicilioCocina.get("idRegistroCliente").asInt());
        assertEquals("Cliente Test Pedido", domicilioCocina.get("nombreCliente").asText());
        assertEquals(testRutaId, domicilioCocina.get("idRuta").asInt());
        assertEquals("Calle Principal 42 Int 3", domicilioCocina.get("domicilio").asText());
        assertEquals(40.00, domicilioCocina.get("precioTarifa").asDouble());

        assertTrue(data.get("domicilio").isNull());
        assertTrue(data.get("pedidoCocina").isNull());

        assertEquals(50.00, data.get("precioFinalOrden").asDouble());
        System.out.println("[OK] COCINA+DOMICILIO id=" + createdCocinaDomicilioId
                + " cliente=" + domicilioCocina.get("nombreCliente").asText()
                + " precioFinal=" + data.get("precioFinalOrden").asDouble());
    }

    @Test
    @Order(9)
    @DisplayName("POST /pedido - COCINA DOMICILIO sin pedidoDomicilioCocina debe retornar 400")
    public void save_cocinaDomicilio_sinRegistroCliente_retorna400() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "EFECTIVO",
                  "tipoPedido": "DOMICILIO",
                  "pedidoCreadoDesde": "COCINA",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = mapper.readTree(response.getBody());
        assertTrue(body.get("message").asText().contains("pedidoDomicilioCocina"));
        System.out.println("[OK] COCINA+DOMICILIO sin pedidoDomicilioCocina → 400: " + body.get("message").asText());
    }

    @Test
    @Order(10)
    @DisplayName("POST /pedido - Sin metodoPagoPrincipal debe crear el pedido y retornar 201")
    public void save_sinMetodoPago() throws Exception {
        String json = String.format("""
                {
                  "tipoPedido": "MOSTRADOR",
                  "pedidoCreadoDesde": "COCINA",
                  "pagoCliente": 50.00,
                  "nombreCliente": "Test Sin Metodo Pago",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        createdSinMetodoPagoId = data.get("idPedido").asInt();
        assertTrue(createdSinMetodoPagoId > 0);
        assertTrue(data.get("metodoPagoPrincipal").isNull());
        System.out.println("[OK] save sin metodoPagoPrincipal → 201 | id=" + createdSinMetodoPagoId);
    }

    // ── Setup / teardown exclusivos de los tests de complementos ─────────────

    @BeforeAll
    void setUpComplementos() throws Exception {
        String compNoCobrar = """
                {"nombreComplemento":"%s","precioExtra":3.00,"estatus":"DISPONIBLE","destacado":false,"cobrarSiempre":false}
                """;
        String compCobrarSiempre = """
                {"nombreComplemento":"CS-Test","precioExtra":5.00,"estatus":"DISPONIBLE","destacado":false,"cobrarSiempre":true}
                """;

        compAId = crearComplemento(String.format(compNoCobrar, "CompA-Test"));
        compBId = crearComplemento(String.format(compNoCobrar, "CompB-Test"));
        compCId = crearComplemento(String.format(compNoCobrar, "CompC-Test"));
        compDId = crearComplemento(String.format(compNoCobrar, "CompD-Test"));
        compEId = crearComplemento(String.format(compNoCobrar, "CompE-Test"));
        compCSId = crearComplemento(compCobrarSiempre);

        testComida1Id = crearComida(1);
        testComida3Id = crearComida(3);

        System.out.println("[SETUP-COMP] comida1=" + testComida1Id + " comida3=" + testComida3Id
                + " compA=" + compAId + " compB=" + compBId + " compC=" + compCId
                + " compD=" + compDId + " compE=" + compEId + " compCS=" + compCSId);
    }

    @AfterAll
    void tearDownComplementos() {
        if (pedidoComplementos3Id > 0) {
            restTemplate.exchange("/pedido/" + pedidoComplementos3Id, HttpMethod.DELETE,
                    new HttpEntity<>(authHeaders), String.class);
        }
        for (int id : new int[]{compAId, compBId, compCId, compDId, compEId, compCSId}) {
            if (id > 0) {
                restTemplate.exchange("/complemento/" + id + "?saltarConfirmacion=true",
                        HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
            }
        }
        for (int id : new int[]{testComida1Id, testComida3Id}) {
            if (id > 0) {
                restTemplate.exchange("/comida/" + id + "?saltarConfirmacion=true",
                        HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
            }
        }
        System.out.println("[TEARDOWN-COMP] datos de complementos eliminados");
    }

    private int crearComplemento(String json) throws Exception {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/complemento", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);
        return mapper.readTree(resp.getBody()).get("data").get("idComplemento").asInt();
    }

    private int crearComida(int limiteComplemento) throws Exception {
        String json = String.format("""
                {"nombreComida":"ComidaTest-Limite%d","precioMedia":45.00,"precioEntera":90.00,
                 "estatus":"DISPONIBLE","destacado":false,"limiteComplemento":%d}
                """, limiteComplemento, limiteComplemento);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/comida", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class);
        return mapper.readTree(resp.getBody()).get("data").get("idComida").asInt();
    }

    private String pedidoConComplementos(int idComida, String complementosJson) {
        return String.format("""
                {"metodoPagoPrincipal":"EFECTIVO","tipoPedido":"MOSTRADOR","pedidoCreadoDesde":"COCINA",
                 "pagoCliente":100.00,"nombreCliente":"Test Complementos",
                 "comidas":[{"idComida":%d,"precioUnitario":90.00,"tamanoPorcion":"ENTERA","complementos":%s}],
                 "desayunos":[],"basicos":[],"productosCocina":[],"saltarConfirmacion":true}
                """, idComida, complementosJson);
    }

    // ── Escenarios 1-5 de complementos ───────────────────────────────────────

    @Test
    @Order(20)
    @DisplayName("Escenario 1 — limite=1: primer no-cobrar gratis, segundo no-cobrar cobrado")
    public void limite1_primerNoCobrarGratis_segundoNoCobrarCobrado() throws Exception {
        String comps = String.format(
                "[{\"idComplemento\":%d},{\"idComplemento\":%d,\"precio_unitario\":3.00}]",
                compAId, compBId);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/pedido", HttpMethod.POST,
                new HttpEntity<>(pedidoConComplementos(testComida1Id, comps), authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        JsonNode complementos = mapper.readTree(resp.getBody())
                .get("data").get("comidasPedido").get(0).get("complementos");

        assertEquals(2, complementos.size());
        assertEquals(0.0, complementos.get(0).get("precioUnitario").asDouble(), "compA debe ser gratis");
        assertEquals(3.0, complementos.get(1).get("precioUnitario").asDouble(), "compB debe ser cobrado");

        int pedidoId = mapper.readTree(resp.getBody()).get("data").get("idPedido").asInt();
        restTemplate.exchange("/pedido/" + pedidoId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        System.out.println("[OK] Esc1 compA=0, compB=3");
    }

    @Test
    @Order(21)
    @DisplayName("Escenario 2 — limite=1: no-cobrar gratis + cobrarSiempre cobrado (no consume slot)")
    public void limite1_noCobrarGratis_cobrarSiempreCobrado() throws Exception {
        String comps = String.format(
                "[{\"idComplemento\":%d},{\"idComplemento\":%d,\"precio_unitario\":5.00}]",
                compAId, compCSId);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/pedido", HttpMethod.POST,
                new HttpEntity<>(pedidoConComplementos(testComida1Id, comps), authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        JsonNode complementos = mapper.readTree(resp.getBody())
                .get("data").get("comidasPedido").get(0).get("complementos");

        assertEquals(2, complementos.size());
        assertEquals(0.0, complementos.get(0).get("precioUnitario").asDouble(), "compA debe ser gratis");
        assertEquals(5.0, complementos.get(1).get("precioUnitario").asDouble(), "cobrarSiempre debe ser cobrado");

        int pedidoId = mapper.readTree(resp.getBody()).get("data").get("idPedido").asInt();
        restTemplate.exchange("/pedido/" + pedidoId, HttpMethod.DELETE, new HttpEntity<>(authHeaders), String.class);
        System.out.println("[OK] Esc2 compA=0, compCS=5");
    }

    @Test
    @Order(22)
    @DisplayName("Escenario 3 — limite=3: tres gratis y el cuarto cobrado")
    public void limite3_tresFreeUnoExceso() throws Exception {
        String comps = String.format(
                "[{\"idComplemento\":%d},{\"idComplemento\":%d},{\"idComplemento\":%d}" +
                ",{\"idComplemento\":%d,\"precio_unitario\":3.00}]",
                compAId, compBId, compCId, compDId);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/pedido", HttpMethod.POST,
                new HttpEntity<>(pedidoConComplementos(testComida3Id, comps), authHeaders), String.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        JsonNode data = mapper.readTree(resp.getBody()).get("data");
        pedidoComplementos3Id = data.get("idPedido").asInt();
        JsonNode complementos = data.get("comidasPedido").get(0).get("complementos");

        assertEquals(4, complementos.size());
        assertEquals(0.0, complementos.get(0).get("precioUnitario").asDouble(), "compA gratis");
        assertEquals(0.0, complementos.get(1).get("precioUnitario").asDouble(), "compB gratis");
        assertEquals(0.0, complementos.get(2).get("precioUnitario").asDouble(), "compC gratis");
        assertEquals(3.0, complementos.get(3).get("precioUnitario").asDouble(), "compD cobrado");
        System.out.println("[OK] Esc3 id=" + pedidoComplementos3Id + " A,B,C=0 D=3");
    }

    @Test
    @Order(23)
    @DisplayName("Escenario 4 — PUT agrega compE y cobrarSiempre: mantiene 3 gratis")
    public void limite3_put_agregarNoCobrarYCobrarSiempre_mantienenTresGratis() throws Exception {
        // Lista completa: A, B, C (gratis), D (cobrado), E (cobrado), CS (cobrarSiempre)
        String comps = String.format(
                "[{\"idComplemento\":%d},{\"idComplemento\":%d},{\"idComplemento\":%d}" +
                ",{\"idComplemento\":%d,\"precio_unitario\":3.00}" +
                ",{\"idComplemento\":%d,\"precio_unitario\":3.00}" +
                ",{\"idComplemento\":%d,\"precio_unitario\":5.00}]",
                compAId, compBId, compCId, compDId, compEId, compCSId);
        ResponseEntity<String> resp = restTemplate.exchange(
                "/pedido/" + pedidoComplementos3Id, HttpMethod.PUT,
                new HttpEntity<>(pedidoConComplementos(testComida3Id, comps), authHeaders), String.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode complementos = mapper.readTree(resp.getBody())
                .get("data").get("comidasPedido").get(0).get("complementos");

        assertEquals(6, complementos.size());
        assertEquals(0.0, complementos.get(0).get("precioUnitario").asDouble(), "A gratis");
        assertEquals(0.0, complementos.get(1).get("precioUnitario").asDouble(), "B gratis");
        assertEquals(0.0, complementos.get(2).get("precioUnitario").asDouble(), "C gratis");
        assertEquals(3.0, complementos.get(3).get("precioUnitario").asDouble(), "D cobrado");
        assertEquals(3.0, complementos.get(4).get("precioUnitario").asDouble(), "E cobrado");
        assertEquals(5.0, complementos.get(5).get("precioUnitario").asDouble(), "CS cobrado");
        System.out.println("[OK] Esc4 A,B,C=0 D,E=3 CS=5");
    }

    @Test
    @Order(24)
    @DisplayName("Escenario 5 — PUT quita un gratis: el siguiente cobrado pasa a gratis")
    public void limite3_put_quitarUnGratis_siguienteCobradoPasaAGratis() throws Exception {
        // Quitamos A: lista = B, C, D, E, CS
        // Con límite=3: B(gratis), C(gratis), D(gratis←era cobrado), E(cobrado), CS(cobrarSiempre)
        // El servidor fuerza precio=0 para slots libres; D ya no necesita precio en el DTO
        String compsCorrectos = String.format(
                "[{\"idComplemento\":%d},{\"idComplemento\":%d},{\"idComplemento\":%d}" +
                ",{\"idComplemento\":%d,\"precio_unitario\":3.00}" +
                ",{\"idComplemento\":%d,\"precio_unitario\":5.00}]",
                compBId, compCId, compDId, compEId, compCSId);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/pedido/" + pedidoComplementos3Id, HttpMethod.PUT,
                new HttpEntity<>(pedidoConComplementos(testComida3Id, compsCorrectos), authHeaders), String.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode complementos = mapper.readTree(resp.getBody())
                .get("data").get("comidasPedido").get(0).get("complementos");

        assertEquals(5, complementos.size());
        assertEquals(0.0, complementos.get(0).get("precioUnitario").asDouble(), "B gratis");
        assertEquals(0.0, complementos.get(1).get("precioUnitario").asDouble(), "C gratis");
        assertEquals(0.0, complementos.get(2).get("precioUnitario").asDouble(), "D ahora gratis");
        assertEquals(3.0, complementos.get(3).get("precioUnitario").asDouble(), "E cobrado");
        assertEquals(5.0, complementos.get(4).get("precioUnitario").asDouble(), "CS cobrado");
        System.out.println("[OK] Esc5 B,C,D=0 E=3 CS=5 — D pasó de cobrado a gratis");
    }

    @Test
    @Order(11)
    @DisplayName("POST /pedido - WEB DOMICILIO sin campo domicilio debe retornar 400")
    public void save_webDomicilio_sinDomicilio_retorna400() throws Exception {
        String json = String.format("""
                {
                  "metodoPagoPrincipal": "TARJETA",
                  "tipoPedido": "DOMICILIO",
                  "pedidoCreadoDesde": "WEB",
                  "comidas": [],
                  "desayunos": [],
                  "basicos": [],
                  "productosCocina": [
                    {"idProductoCocina": %d, "precioUnitario": 10.00, "cantidad": 1}
                  ],
                  "saltarConfirmacion": true
                }
                """, testProductoId);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/pedido", HttpMethod.POST, new HttpEntity<>(json, authHeaders), String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JsonNode body = mapper.readTree(response.getBody());
        assertTrue(body.get("message").asText().contains("domicilio"));
        System.out.println("[OK] WEB+DOMICILIO sin domicilio → 400: " + body.get("message").asText());
    }
}
