package com.cocinarubi.util;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {

    private IpUtils() {}

    public static String obtenerIp(HttpServletRequest request) {
        // X-Real-IP lo establece nginx con $remote_addr (no manipulable por el cliente).
        // X-Forwarded-For se descarta: el cliente puede inyectar valores arbitrarios en posición [0].
        String ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
