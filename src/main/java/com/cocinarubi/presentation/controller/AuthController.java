package com.cocinarubi.presentation.controller;

import com.cocinarubi.aop.SkipAudit;
import com.cocinarubi.domain.service.JwtDenylistService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.LoginRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SkipAudit
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtDenylistService jwtDenylistService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          JwtDenylistService jwtDenylistService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtDenylistService = jwtDenylistService;
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.nombreUsuario(), dto.contrasena())
            );
            String token = jwtService.generarToken((UserDetails) auth.getPrincipal());
            return ApiResponse.exito(200, "Login exitoso", token);
        } catch (BadCredentialsException e) {
            throw new BusinessException("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Revoca el token del header {@code Authorization} en la denylist Redis
     * hasta su expiracion natural. Idempotente: llamar dos veces no da error.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ApiResponse.exito(200, "Sesión cerrada", null);
        }
        String token = header.substring(7);
        try {
            Claims claims = jwtService.extraerClaimsAunqueExpirado(token);
            if (claims != null && claims.getId() != null && claims.getExpiration() != null) {
                long ttl = Math.max(0,
                        (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
                if (ttl > 0) jwtDenylistService.revocar(claims.getId(), ttl);
            }
        } catch (JwtException ignored) {
            // Token invalido/expirado con firma rota: nada que revocar
        }
        return ApiResponse.exito(200, "Sesión cerrada", null);
    }
}
