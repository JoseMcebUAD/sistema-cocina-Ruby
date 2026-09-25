package com.cocinarubi.presentation.filter;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.util.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Rate limiter para GET /web/codigo-cliente/{codigo}.
 * Límite: 2 peticiones cada 5 segundos por uuid_cliente (cookie) o IP.
 * Capa: Filter — control de abuso en el canal web público.
 */
public class CodigoClienteWebRateLimitFilter extends OncePerRequestFilter {

    private static final Pattern RUTA = Pattern.compile("^/web/codigo-cliente/[^/]+$");
    private static final int CAPACIDAD = 2;
    private static final Duration VENTANA = Duration.ofSeconds(5);

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(50_000)
            .build();

    private final ObjectMapper objectMapper;
    private final IpUtils ipUtils;

    public CodigoClienteWebRateLimitFilter(ObjectMapper objectMapper, IpUtils ipUtils) {
        this.objectMapper = objectMapper;
        this.ipUtils = ipUtils;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !("GET".equalsIgnoreCase(request.getMethod())
                && RUTA.matcher(request.getRequestURI()).matches());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String id = obtenerIdentificador(request);
        Bucket bucket = buckets.get(id, k -> crearBucket());

        if (!bucket.tryConsume(1)) {
            responder429(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

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
        return "ip:" + ipUtils.obtenerIp(request);
    }

    private Bucket crearBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(CAPACIDAD)
                        .refillGreedy(CAPACIDAD, VENTANA)
                        .build())
                .build();
    }

    private void responder429(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(VENTANA.getSeconds()));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.error(429, "Demasiadas consultas. Intenta de nuevo en 5 segundos."));
    }
}
