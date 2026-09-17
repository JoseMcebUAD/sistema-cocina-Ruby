package com.cocinarubi.presentation.filter;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.util.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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
import java.util.regex.Pattern;

public class PedidoWebRateLimitFilter extends OncePerRequestFilter {

    // POST /web/pedidos  o  PUT /web/pedidos/{id}
    private static final Pattern RUTA_PEDIDOS = Pattern.compile("^/web/pedidos(/\\d+)?$");

    private static final int MAX_PEDIDOS = 3;
    private static final Duration VENTANA = Duration.ofMinutes(1);
    // Bloqueo extendido al agotar el bucket rapido: Bucket4j no soporta esto nativamente,
    // por eso se usa un mapa de penalizacion separado.
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    // Tope duro diario: 3 pedidos cada 24h con reset de golpe (no goteo)
    private static final int MAX_PEDIDOS_DIA = 3;
    private static final Duration VENTANA_DIA = Duration.ofHours(24);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> bucketsDiarios = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> bloqueados = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public PedidoWebRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Aplica a POST /web/pedidos y PUT /web/pedidos/{id}; el resto pasa sin revision
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String metodo = request.getMethod();
        boolean metodoAplica = "POST".equalsIgnoreCase(metodo) || "PUT".equalsIgnoreCase(metodo);
        return !(metodoAplica && RUTA_PEDIDOS.matcher(request.getRequestURI()).matches());
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
            bloqueados.remove(id);
        }

        Bucket bucket = buckets.computeIfAbsent(id, k -> crearBucketRapido());
        if (!bucket.tryConsume(1)) {
            // Tokens agotados: se aplica el bloqueo extendido de 15 minutos
            bloqueados.put(id, Instant.now().plus(BLOQUEO));
            responder429(response, BLOQUEO.getSeconds(),
                    "Has superado el limite de pedidos. Intenta de nuevo en 15 minutos.");
            return;
        }

        // Bucket diario solo para POST y solo si hay cookie uuid_cliente
        // (evita castigar IPs compartidas en hogares/oficinas con NAT)
        if ("POST".equalsIgnoreCase(request.getMethod()) && id.startsWith("uuid:")) {
            Bucket bucketDia = bucketsDiarios.computeIfAbsent(id, k -> crearBucketDiario());
            ConsumptionProbe probeDia = bucketDia.tryConsumeAndReturnRemaining(1);
            if (!probeDia.isConsumed()) {
                long segundos = probeDia.getNanosToWaitForRefill() / 1_000_000_000 + 1;
                responder429(response, segundos,
                        "Has alcanzado el limite de " + MAX_PEDIDOS_DIA
                                + " pedidos por dia. Podras hacer un nuevo pedido en "
                                + formatearEspera(segundos) + ".");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // Cookie uuid_cliente como identificador (persiste aunque cambie de IP);
    // si no hay cookie, se cae a IP para el bucket rapido
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
        return "ip:" + IpUtils.obtenerIp(request);
    }

    private String formatearEspera(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        if (horas > 0) return horas + " horas " + minutos + " minutos";
        return minutos + " minutos";
    }

    private void responder429(HttpServletResponse response, long segundos, String mensaje)
            throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(segundos));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(429, mensaje));
    }

    private Bucket crearBucketRapido() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(MAX_PEDIDOS)
                        .refillGreedy(MAX_PEDIDOS, VENTANA)
                        .build())
                .build();
    }

    // refillIntervally: recarga todos los tokens de golpe cada 24h (no gradual)
    private Bucket crearBucketDiario() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(MAX_PEDIDOS_DIA)
                        .refillIntervally(MAX_PEDIDOS_DIA, VENTANA_DIA)
                        .build())
                .build();
    }
}
