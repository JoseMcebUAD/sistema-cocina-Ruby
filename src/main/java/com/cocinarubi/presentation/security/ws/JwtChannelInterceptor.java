package com.cocinarubi.presentation.security.ws;

import com.cocinarubi.presentation.security.JwtService;
import com.cocinarubi.presentation.security.UsuarioDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Set;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    // TODO: reemplazar con el prefijo del topic STOMP que requiera autenticación
    private static final String TOPIC_PROTEGIDO = "/topic-protegido";

    // TODO: reemplazar con los roles que pueden suscribirse/publicar en ese topic
    private static final Set<String> ROLES_REQUERIDOS = Set.of(
            "ROLE_JEFA_COCINA",
            "ROLE_COCINA"
    );

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtChannelInterceptor(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        log.info("STOMP frame recibido. command={} session={}", command, accessor.getSessionId());

        try {
            if (StompCommand.CONNECT.equals(command)) {
                autenticarConnect(accessor);
            } else if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
                autorizarDestino(accessor);
            }
        } catch (Exception e) {
            log.error("STOMP preSend excepcion. command={} session={} error={} causa={}",
                    command, accessor.getSessionId(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }

        return message;
    }

    private void autenticarConnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("STOMP CONNECT rechazado: Authorization ausente. session={}", sessionId);
            throw new BadCredentialsException("Token JWT ausente en CONNECT");
        }

        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extraerUsername(token);
        } catch (Exception e) {
            log.warn("STOMP CONNECT rechazado: no se pudo extraer username. session={}", sessionId);
            throw new BadCredentialsException("Token JWT inválido");
        }

        if (username == null) {
            throw new BadCredentialsException("Token JWT inválido");
        }

        if (!jwtService.estaEnVentanaDeRenovacion(token)) {
            log.warn("STOMP CONNECT rechazado: token expirado definitivamente. session={} username={}", sessionId, username);
            throw new BadCredentialsException("Token JWT expirado");
        }

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(username);
        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            log.warn("STOMP CONNECT rechazado: cuenta deshabilitada. session={} username={}", sessionId, username);
            throw new BadCredentialsException("Usuario deshabilitado");
        }

        accessor.setUser(new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        ));
        log.info("STOMP CONNECT autenticado. session={} username={}", sessionId, username);
    }

    private void autorizarDestino(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        String command = accessor.getCommand() != null ? accessor.getCommand().name() : "UNKNOWN";

        if (destination == null || !destination.startsWith(TOPIC_PROTEGIDO)) {
            log.debug("STOMP {} ignorado (destino no protegido). session={} destination={}", command, sessionId, destination);
            return;
        }

        Principal principal = accessor.getUser();
        if (!(principal instanceof Authentication auth) || !auth.isAuthenticated()) {
            log.warn("STOMP {} rechazado: sin autenticacion. session={} destination={}", command, sessionId, destination);
            throw new AccessDeniedException("No autenticado para " + destination);
        }

        boolean tieneRol = auth.getAuthorities().stream()
                .anyMatch(a -> ROLES_REQUERIDOS.contains(a.getAuthority()));
        if (!tieneRol) {
            log.warn("STOMP {} rechazado: rol insuficiente. session={} username={} destination={}",
                    command, sessionId, auth.getName(), destination);
            throw new AccessDeniedException("Rol insuficiente para " + destination);
        }

        log.info("STOMP {} autorizado. session={} username={} destination={}", command, sessionId, auth.getName(), destination);
    }
}
