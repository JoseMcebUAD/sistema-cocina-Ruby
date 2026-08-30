package com.cocinarubi.presentation.filter;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class PedidoWebRateLimitFilter extends OncePerRequestFilter {

    private static final String PEDIDOS_PATH = "/web/pedidos";
    private static final int MAX_PEDIDOS = 3;
    private static final Duration VENTANA = Duration.ofMinutes(1);
    // Bloqueo extendido al agotar el bucket: Bucket4j no soporta esto nativamente,
    // por eso se usa un mapa de penalizacion separado.
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> bloqueados = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public PedidoWebRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Solo aplica a POST /web/pedidos; el resto de rutas pasan sin revision
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && PEDIDOS_PATH.equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String id = obtenerIdentificador(request);

        // Si el cliente esta en el mapa de penalizacion, se rechaza hasta que expire el bloqueo
        Instant bloqueadoHasta = bloqueados.get(id);
        if (bloqueadoHasta != null) {
            if (Instant.now().isBefore(bloqueadoHasta)) {
                long segundosRestantes = Duration.between(Instant.now(), bloqueadoHasta).getSeconds() + 1;
                responder429(response, segundosRestantes,
                        "Has superado el limite de pedidos. Intenta de nuevo en " + segundosRestantes + " segundos.");
                return;
            }
            // El bloqueo ya expiro: se limpia para que el cliente pueda volver a intentar
            bloqueados.remove(id);
        }

        Bucket bucket = buckets.computeIfAbsent(id, k -> crearBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            // Tokens agotados: se aplica el bloqueo extendido de 15 minutos
            bloqueados.put(id, Instant.now().plus(BLOQUEO));
            responder429(response, BLOQUEO.getSeconds(),
                    "Has superado el limite de pedidos. Intenta de nuevo en 15 minutos.");
        }
    }

    // Usa la cookie uuid_cliente como identificador para que el limite persista aunque el cliente cambie de IP
    private String obtenerIdentificador(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String uuid = Arrays.stream(request.getCookies())
                    .filter(c -> "uuid_cliente".equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst()
                    .orElse(null);
            if (uuid != null) return "uuid:" + uuid;
        }
        // Sin cookie: fallback a IP (clientes que no pasaron por /sesion)
        return "ip:" + obtenerIp(request);
    }

    // Respeta proxies: X-Forwarded-For -> X-Real-IP -> remoteAddr
    private String obtenerIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    // Respuesta uniforme con el mismo formato que los demas filtros del proyecto
    private void responder429(HttpServletResponse response, long segundos, String mensaje)
            throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(segundos));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(429, mensaje));
    }

    private Bucket crearBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(MAX_PEDIDOS)
                        .refillGreedy(MAX_PEDIDOS, VENTANA)
                        .build())
                .build();
    }
}
