package com.cocinarubi.presentation.security;

import com.cocinarubi.domain.service.JwtDenylistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtDenylistService jwtDenylistService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UsuarioDetailsService usuarioDetailsService,
                                   JwtDenylistService jwtDenylistService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtDenylistService = jwtDenylistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtService.extraerUsername(token);

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(username);

        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Rechaza tokens revocados por logout (persisten en Redis hasta su expiracion natural)
        String jti = extraerJti(token);
        if (jti != null && jwtDenylistService.estaRevocado(jti)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // esTokenValido cubre firma, expiracion, subject y tokenVersion del usuario
        if (!jwtService.esTokenValido(token, userDetails)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        autenticar(userDetails, request);
        // Renueva el token solo si esta valido; si expiro habra 401 y el frontend fuerza re-login
        response.setHeader("Authorization", "Bearer " + jwtService.renovarToken(token));

        filterChain.doFilter(request, response);
    }

    /** Extrae el {@code jti} incluso si el token esta expirado; devuelve null si la firma es invalida. */
    private String extraerJti(String token) {
        try {
            Claims claims = jwtService.extraerClaimsAunqueExpirado(token);
            return claims != null ? claims.getId() : null;
        } catch (JwtException e) {
            return null;
        }
    }

    private void autenticar(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
