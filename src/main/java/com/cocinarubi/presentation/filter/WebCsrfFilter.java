package com.cocinarubi.presentation.filter;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Mitiga CSRF cross-origin en /web/** exigiendo el header custom X-Fingerprint
 * en cualquier peticion mutante. Al ser un header no-simple, el navegador
 * fuerza preflight CORS: los origenes no permitidos quedan bloqueados antes de
 * llegar al endpoint.
 * Capa: Filter — defensa en profundidad para el canal WEB.
 */
public class WebCsrfFilter extends OncePerRequestFilter {

    private static final String FINGERPRINT_HEADER = "X-Fingerprint";
    private final ObjectMapper objectMapper;

    public WebCsrfFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/web/")) return true;
        String metodo = request.getMethod();
        return !("POST".equalsIgnoreCase(metodo)
                || "PUT".equalsIgnoreCase(metodo)
                || "PATCH".equalsIgnoreCase(metodo)
                || "DELETE".equalsIgnoreCase(metodo));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String huella = request.getHeader(FINGERPRINT_HEADER);
        if (huella == null || huella.isBlank()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    ApiResponse.error(403, "Cabecera X-Fingerprint requerida"));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
