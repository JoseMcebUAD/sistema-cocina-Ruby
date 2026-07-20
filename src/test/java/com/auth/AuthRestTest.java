package com.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = com.cocinarubi.Application.class)
public class AuthRestTest {

    // TODO: Reemplazar con la contraseña real del usuario 'rubi' en la DB de test (exactamente 5 caracteres)
    private static final String rubi_PASSWORD = "cocina123";

    @Autowired private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpHeaders jsonHeaders;

    public AuthRestTest() {
        jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("POST /auth/login - Debe autenticar al usuario y retornar un token JWT con status 200")
    public void login_exitoso() throws Exception {
        String json = """
                {
                  "nombreUsuario": "rubi",
                  "contrasena": "%s"
                }
                """.formatted(rubi_PASSWORD);

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/auth/login", HttpMethod.POST, new HttpEntity<>(json, jsonHeaders), String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = mapper.readTree(response.getBody()).get("data");
        assertNotNull(data);
        assertFalse(data.asText().isBlank(), "El token JWT no debe estar vacío");
        System.out.println("[OK] " + response.getStatusCode() + " | token generado (longitud=" + data.asText().length() + ")");
    }

    @Test
    @DisplayName("POST /auth/login - Debe retornar 401 con credenciales incorrectas")
    public void login_credencialesInvalidas() throws Exception {
        String json = """
                {
                  "nombreUsuario": "rubi",
                  "contrasena": "wrong"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/auth/login", HttpMethod.POST, new HttpEntity<>(json, jsonHeaders), String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println("[OK] credenciales inválidas → 401");
    }

    @Test
    @DisplayName("POST /auth/login - Debe retornar 400 cuando falta el campo nombreUsuario")
    public void login_campoRequerido_retorna400() {
        String json = """
                {
                  "contrasena": "rubi1"
                }
                """;

        ResponseEntity<String> response = this.restTemplate.exchange(
                "/auth/login", HttpMethod.POST, new HttpEntity<>(json, jsonHeaders), String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        System.out.println("[OK] campo requerido ausente → 400");
    }
}
