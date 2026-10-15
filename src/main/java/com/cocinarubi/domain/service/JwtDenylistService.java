package com.cocinarubi.domain.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Denylist de JWTs en Redis, indexada por {@code jti}.
 * Capa: Service — se consulta en cada request autenticada y se escribe en logout.
 *
 * <p>La TTL se ajusta al tiempo restante hasta la expiracion del JWT para
 * evitar acumular entradas eternas. Cuando el token caduca de forma natural
 * la clave se autoelimina.</p>
 */
@Service
public class JwtDenylistService {

    private static final String PREFIX = "jwt:blacklist:";
    private final StringRedisTemplate redis;

    public JwtDenylistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void revocar(String jti, long ttlSegundos) {
        if (jti == null || jti.isBlank() || ttlSegundos <= 0) return;
        redis.opsForValue().set(PREFIX + jti, "1", Duration.ofSeconds(ttlSegundos));
    }

    public boolean estaRevocado(String jti) {
        if (jti == null || jti.isBlank()) return false;
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + jti));
    }
}
