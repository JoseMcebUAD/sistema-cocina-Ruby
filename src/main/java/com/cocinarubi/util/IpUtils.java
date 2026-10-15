package com.cocinarubi.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resuelve la IP real del cliente respetando una lista blanca de proxies confiables.
 * Capa: Util — infraestructura de red.
 *
 * <p>Rechaza el spoofing de {@code X-Real-IP} desde clientes no confiables:
 * solo se lee esa cabecera cuando {@link HttpServletRequest#getRemoteAddr()} pertenece
 * a la lista de proxies configurada (típicamente la IP interna de nginx).
 */
@Component
public class IpUtils {

    private final Set<String> trustedProxies;

    public IpUtils(@Value("${security.trusted-proxies:127.0.0.1,::1,0:0:0:0:0:0:0:1}") String csv) {
        this.trustedProxies = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public String obtenerIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        if (remote != null && trustedProxies.contains(remote)) {
            // X-Real-IP solo se confia cuando la conexion viene de un proxy conocido
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }
        return remote;
    }
}
