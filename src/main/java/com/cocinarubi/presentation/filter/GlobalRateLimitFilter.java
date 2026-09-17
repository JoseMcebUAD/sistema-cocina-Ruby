package com.cocinarubi.presentation.filter;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.util.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.ConcurrentHashMap;

public class GlobalRateLimitFilter extends OncePerRequestFilter {

    // Endpoints administrativos y otros: 12 req / 10s por IP
    private static final int MAX_POR_IP = 12;
    // Endpoints publicos web (carrito, menu): 30 req / 10s por IP.
    // Se levanta porque el frontend hace muchas peticiones paralelas al cargar (rutas, menu, imagenes).
    private static final int MAX_POR_IP_WEB = 30;
    private static final int MAX_LOGIN_GLOBAL = 50;
    private static final Duration VENTANA = Duration.ofSeconds(10);
    private static final String LOGIN_PATH = "/auth/login";
    private static final String FINGERPRINT_HEADER = "X-Fingerprint";

    // Presupuestos independientes: consumir la app web no gasta el presupuesto general y viceversa
    private final ConcurrentHashMap<String, Bucket> bucketsPorIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> bucketsPorIpWeb = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> bucketsPorHuella = new ConcurrentHashMap<>();
    private final Bucket bucketGlobalLogin = crearBucket(MAX_LOGIN_GLOBAL);
    private final ObjectMapper objectMapper;

    public GlobalRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

        String huella = request.getHeader(FINGERPRINT_HEADER);
        if (huella != null && !huella.isBlank()) {
            Bucket bucketHuella = bucketsPorHuella.computeIfAbsent(huella, k -> crearBucket(MAX_POR_IP));
            ConsumptionProbe huellaProbe = bucketHuella.tryConsumeAndReturnRemaining(1);
            if (!huellaProbe.isConsumed()) {
                responder429(response, huellaProbe.getNanosToWaitForRefill(),
                        "Demasiadas solicitudes. Intente de nuevo en un momento.");
                return;
            }
        }

        String ip = IpUtils.obtenerIp(request);
        boolean esRutaPublicaWeb = esRutaPublicaWeb(request.getRequestURI());
        int capacidad = esRutaPublicaWeb ? MAX_POR_IP_WEB : MAX_POR_IP;
        ConcurrentHashMap<String, Bucket> mapa = esRutaPublicaWeb ? bucketsPorIpWeb : bucketsPorIp;

        Bucket bucketIp = mapa.computeIfAbsent(ip, k -> crearBucket(capacidad));
        ConsumptionProbe ipProbe = bucketIp.tryConsumeAndReturnRemaining(1);

        if (!ipProbe.isConsumed()) {
            responder429(response, ipProbe.getNanosToWaitForRefill(),
                    "Demasiadas solicitudes. Intente de nuevo en un momento.");
            return;
        }

        response.setHeader("X-RateLimit-Remaining", String.valueOf(ipProbe.getRemainingTokens()));
        filterChain.doFilter(request, response);
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
