package com.cocinarubi.presentation.security;

import com.cocinarubi.domain.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRACION_MS = 5L * 60 * 60 * 1000;

    // JEFA_COCINA gestiona el local todo el día → token de 24 horas
    private static final long EXPIRACION_JEFA_COCINA_MS = 24L * 60 * 60 * 1000;

    // ── Generación ───────────────────────────────────────────────────────────

    public String generarToken(UserDetails userDetails) {
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        boolean esJefaCocina = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_JEFA_COCINA"::equals);

        long expiracion = esJefaCocina ? EXPIRACION_JEFA_COCINA_MS : EXPIRACION_MS;
        int tokenVersion = (userDetails instanceof Usuario u) ? u.getTokenVersion() : 0;

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())                 // jti para denylist
                .claim("roles", roles)
                .claim("ver", tokenVersion)                       // permite revocar todos los tokens del usuario
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracion))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Renueva un token vigente. Si el token esta expirado, se propaga
     * {@link ExpiredJwtException}: hay que forzar re-login.
     */
    public String renovarToken(String tokenOriginal) {
        Claims claims = parsearClaims(tokenOriginal);
        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())                 // nuevo jti — el anterior queda inactivo naturalmente
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Extracción ───────────────────────────────────────────────────────────

    public String extraerUsername(String token) {
        try {
            return parsearClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Devuelve los claims incluso si el token esta expirado. Solo lanza
     * {@link JwtException} si la firma es invalida.
     * Uso: logout — necesita leer el jti para revocarlo aunque el token
     * ya haya caducado.
     */
    public Claims extraerClaimsAunqueExpirado(String token) {
        try {
            return parsearClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // ── Validación ───────────────────────────────────────────────────────────

    public boolean esTokenValido(String token, UserDetails userDetails) {
        try {
            Claims claims = parsearClaims(token);
            if (!claims.getSubject().equals(userDetails.getUsername())) return false;
            // Comparar version del token contra la del usuario para invalidar
            // masivamente si el operador cambio password o forzo logout global
            if (userDetails instanceof Usuario u) {
                Integer ver = claims.get("ver", Integer.class);
                return ver != null && ver >= u.getTokenVersion();
            }
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ── Privados ─────────────────────────────────────────────────────────────

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
