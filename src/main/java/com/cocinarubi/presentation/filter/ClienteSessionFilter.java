package com.cocinarubi.presentation.filter;

import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class ClienteSessionFilter extends OncePerRequestFilter {

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
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            rechazar(response, "Token de sesión requerido");
            return;
        }

        String token = header.substring(7);
        Optional<Cliente> cliente = clienteRepository.findBySessionToken(token);

        if (cliente.isEmpty()
                || cliente.get().getTokenExpiracion() == null
                || cliente.get().getTokenExpiracion().isBefore(LocalDateTime.now())) {
            rechazar(response, "Token de sesión inválido o expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rechazar(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(401, mensaje));
    }
}
