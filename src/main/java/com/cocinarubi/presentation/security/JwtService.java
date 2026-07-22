package com.cocinarubi.presentation.security;

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
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRACION_MS = 5L * 60 * 60 * 1000;

    // Ambos roles trabajan turnos largos → ventana de renovación de 8 horas
    private static final long VENTANA_RENOVACION_MS = 8L * 60 * 60 * 1000;

    // ── Generación ───────────────────────────────────────────────────────────

    public String generarToken(UserDetails userDetails) {
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("vrms", VENTANA_RENOVACION_MS)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    public String renovarToken(String tokenOriginal) {
        Claims claims;
        try {
            claims = parsearClaims(tokenOriginal);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }

        return Jwts.builder()
                .claims(claims)
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
        }
    }

    // ── Validación ───────────────────────────────────────────────────────────

    public boolean esTokenValido(String token, UserDetails userDetails) {
        try {
            String subject = parsearClaims(token).getSubject();
            return subject.equals(userDetails.getUsername());
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean estaEnVentanaDeRenovacion(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            long ventana = obtenerVentanaRenovacion(e.getClaims());
            long msDesdeExpiracion = System.currentTimeMillis() - e.getClaims().getExpiration().getTime();
            return msDesdeExpiracion < ventana;
        } catch (JwtException e) {
            return false;
        }
    }

    // ── Privados ─────────────────────────────────────────────────────────────

    private long obtenerVentanaRenovacion(Claims claims) {
        Long vrms = claims.get("vrms", Long.class);
        return vrms != null ? vrms : VENTANA_RENOVACION_MS;
    }

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
