package com.cocinarubi.presentation.filter;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.util.IpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class GlobalRateLimitFilter extends OncePerRequestFilter {

    // Endpoints administrativos y otros: 12 req / 10s por IP (sin autenticación)
    private static final int MAX_POR_IP = 12;
    // Usuarios autenticados en rutas admin: 35 req / 10s por userId del JWT
    private static final int MAX_POR_USUARIO = 35;
    // Endpoints publicos web (carrito, menu): 50 req / 10s por IP.
    // Se levanta porque el frontend hace muchas peticiones paralelas al cargar (rutas, menu, imagenes)
    // y redes compartidas (familias, oficinas) pueden sumar ~30 req simultáneos en la carga inicial.
    private static final int MAX_POR_IP_WEB = 50;
    private static final int MAX_LOGIN_GLOBAL = 50;
    private static final Duration VENTANA = Duration.ofSeconds(10);
    private static final String LOGIN_PATH = "/auth/login";
    private static final String FINGERPRINT_HEADER = "X-Fingerprint";

    // Presupuestos independientes con TTL y tamaño máximo para prevenir OOM bajo carga masiva
    private final Cache<String, Bucket> bucketsPorIp = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(50_000)
            .build();
    private final Cache<String, Bucket> bucketsPorIpWeb = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(50_000)
            .build();
    private final Cache<String, Bucket> bucketsPorHuella = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(100_000)
            .build();
    // Presupuesto por userId (sub del JWT) para rutas admin autenticadas
    private final Cache<String, Bucket> bucketsPorUsuario = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();
    private final Bucket bucketGlobalLogin = crearBucket(MAX_LOGIN_GLOBAL);
    private final ObjectMapper objectMapper;
    private final IpUtils ipUtils;

    public GlobalRateLimitFilter(ObjectMapper objectMapper, IpUtils ipUtils) {
        this.objectMapper = objectMapper;
        this.ipUtils = ipUtils;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return "/actuator/health".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (LOGIN_PATH.equals(request.getRequestURI())) {
            ConsumptionProbe loginProbe = bucketGlobalLogin.tryConsumeAndReturnRemaining(1);
            if (!loginProbe.isConsumed()) {
                responder429(response, loginProbe.getNanosToWaitForRefill(),
                        "Límite global de login alcanzado. Intente más tarde.");
                return;
            }
        }

        String ip = ipUtils.obtenerIp(request);
        boolean esRutaPublicaWeb = esRutaPublicaWeb(request.getRequestURI());

        if (esRutaPublicaWeb) {
            // Rutas web: fingerprint + IP (el fingerprint aplica porque no hay identidad de usuario)
            String huella = request.getHeader(FINGERPRINT_HEADER);
            if (huella != null && !huella.isBlank()) {
                Bucket bucketHuella = bucketsPorHuella.get(huella, k -> crearBucket(MAX_POR_IP));
                ConsumptionProbe huellaProbe = bucketHuella.tryConsumeAndReturnRemaining(1);
                if (!huellaProbe.isConsumed()) {
                    responder429(response, huellaProbe.getNanosToWaitForRefill(),
                            "Demasiadas solicitudes. Intente de nuevo en un momento.");
                    return;
                }
            }
            Bucket bucketIp = bucketsPorIpWeb.get(ip, k -> crearBucket(MAX_POR_IP_WEB));
            ConsumptionProbe ipProbe = bucketIp.tryConsumeAndReturnRemaining(1);
            if (!ipProbe.isConsumed()) {
                responder429(response, ipProbe.getNanosToWaitForRefill(),
                        "Demasiadas solicitudes. Intente de nuevo en un momento.");
                return;
            }
            response.setHeader("X-RateLimit-Remaining", String.valueOf(ipProbe.getRemainingTokens()));
        } else {
            // Rutas admin: usuarios autenticados tienen bucket propio (el fingerprint no aplica,
            // ya hay identidad de usuario que actúa como discriminador más preciso)
            String userId = extraerUserIdDeJwt(request);
            if (userId != null) {
                Bucket bucketUsuario = bucketsPorUsuario.get(userId, k -> crearBucket(MAX_POR_USUARIO));
                ConsumptionProbe usuarioProbe = bucketUsuario.tryConsumeAndReturnRemaining(1);
                if (!usuarioProbe.isConsumed()) {
                    responder429(response, usuarioProbe.getNanosToWaitForRefill(),
                            "Demasiadas solicitudes. Intente de nuevo en un momento.");
                    return;
                }
                response.setHeader("X-RateLimit-Remaining", String.valueOf(usuarioProbe.getRemainingTokens()));
            } else {
                // Sin JWT válido: fingerprint + IP (protege contra ataques no autenticados)
                String huella = request.getHeader(FINGERPRINT_HEADER);
                if (huella != null && !huella.isBlank()) {
                    Bucket bucketHuella = bucketsPorHuella.get(huella, k -> crearBucket(MAX_POR_IP));
                    ConsumptionProbe huellaProbe = bucketHuella.tryConsumeAndReturnRemaining(1);
                    if (!huellaProbe.isConsumed()) {
                        responder429(response, huellaProbe.getNanosToWaitForRefill(),
                                "Demasiadas solicitudes. Intente de nuevo en un momento.");
                        return;
                    }
                }
                Bucket bucketIp = bucketsPorIp.get(ip, k -> crearBucket(MAX_POR_IP));
                ConsumptionProbe ipProbe = bucketIp.tryConsumeAndReturnRemaining(1);
                if (!ipProbe.isConsumed()) {
                    responder429(response, ipProbe.getNanosToWaitForRefill(),
                            "Demasiadas solicitudes. Intente de nuevo en un momento.");
                    return;
                }
                response.setHeader("X-RateLimit-Remaining", String.valueOf(ipProbe.getRemainingTokens()));
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el userId (claim "sub") del JWT del header Authorization sin verificar firma.
     * La firma ya es validada por el filtro de seguridad de Spring; aquí solo necesitamos
     * la identidad para asignar el bucket correcto antes de que llegue a esa validación.
     * Retorna null si no hay token, si expiró, o si el payload no es parseable.
     */
    private String extraerUserIdDeJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String raw = authHeader.substring(7).split("\\.")[1];
            // JWT usa base64url sin padding; se añade manualmente antes de decodificar
            int pad = (4 - raw.length() % 4) % 4;
            JsonNode claims = objectMapper.readTree(Base64.getUrlDecoder().decode(raw + "=".repeat(pad)));
            JsonNode exp = claims.get("exp");
            if (exp != null && exp.asLong() < System.currentTimeMillis() / 1000) return null;
            JsonNode sub = claims.get("sub");
            return (sub != null && !sub.asText().isBlank()) ? sub.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean esRutaPublicaWeb(String uri) {
        return uri != null && (uri.startsWith("/web/") || uri.startsWith("/menu-web/"));
    }

    private void responder429(HttpServletResponse response, long nanosToWait, String mensaje)
            throws IOException {
        long waitSeconds = nanosToWait / 1_000_000_000 + 1;
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(429, mensaje));
    }

    private static Bucket crearBucket(int capacidad) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacidad)
                        .refillGreedy(capacidad, VENTANA)
                        .build())
                .build();
    }
}
