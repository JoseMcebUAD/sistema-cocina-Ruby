package com.cocinarubi.presentation.filter;

import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.util.HashUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

public class ClienteSessionFilter extends OncePerRequestFilter {

    /** Nombre del atributo del request donde se expone el Cliente autenticado. */
    public static final String CLIENTE_ATTR = "clienteAutenticado";
    /** Nombre de la cookie HttpOnly que porta el token de sesion. */
    public static final String SESSION_COOKIE = "session_token";

    private final ClienteRepository clienteRepository;
    private final ObjectMapper objectMapper;

    public ClienteSessionFilter(ClienteRepository clienteRepository, ObjectMapper objectMapper) {
        this.clienteRepository = clienteRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/menu-web") && !path.startsWith("/web/pedidos");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tokenPlano = extraerToken(request);
        if (tokenPlano == null) {
            rechazar(response, "Token de sesión requerido");
            return;
        }

        // HashUtils: solo el hash es la clave de lookup — el token plano nunca se persiste
        String hash = HashUtils.sha256Hex(tokenPlano);
        Optional<Cliente> cliente = clienteRepository.findBySessionTokenHash(hash);

        if (cliente.isEmpty()
                || cliente.get().getTokenExpiracion() == null
                || cliente.get().getTokenExpiracion().isBefore(LocalDateTime.now())) {
            rechazar(response, "Token de sesión inválido o expirado");
            return;
        }

        // Expone el cliente autenticado a los controladores downstream para evitar IDOR
        // (no dependen del uuidCliente que venga en path/body — inseguro por sí mismo)
        request.setAttribute(CLIENTE_ATTR, cliente.get());
        filterChain.doFilter(request, response);
    }

    /** Prioriza la cookie {@code session_token} (HttpOnly) sobre el header {@code Authorization: Bearer}. */
    private String extraerToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String desdeCookie = Arrays.stream(request.getCookies())
                    .filter(c -> SESSION_COOKIE.equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst()
                    .orElse(null);
            if (desdeCookie != null) return desdeCookie;
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String bearer = header.substring(7).trim();
            if (!bearer.isEmpty()) return bearer;
        }
        return null;
    }

    private void rechazar(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(401, mensaje));
    }
}
