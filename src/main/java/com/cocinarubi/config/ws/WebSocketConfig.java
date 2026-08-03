package com.cocinarubi.config.ws;

import com.cocinarubi.presentation.security.ws.JwtChannelInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.Collections;
import java.util.List;

/**
 * Configuración central del broker STOMP sobre WebSocket.
 * Capa: Config — protocolo de mensajería en tiempo real.
 *
 * <p>Define los prefijos del broker, el endpoint de conexión, el interceptor de
 * autenticación y el rechazo de {@code permessage-deflate} para compatibilidad
 * con clientes OkHttp (Android).</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtChannelInterceptor jwtChannelInterceptor;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    /**
     * Scheduler dedicado a enviar frames STOMP de heartbeat periódicamente.
     * Un solo hilo es suficiente porque el volumen de conexiones simultáneas es bajo.
     */
    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // El broker distribuye mensajes a suscriptores en estos prefijos
        // Heartbeat: [servidor→cliente, cliente→servidor] en milisegundos
        registry.enableSimpleBroker("/pedido-web", "/topic")
                .setHeartbeatValue(new long[]{20000, 20000})
                .setTaskScheduler(heartbeatScheduler());
        // Mensajes dirigidos a @MessageMapping / @SubscribeMapping
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/todos")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected List<WebSocketExtension> filterRequestedExtensions(
                            ServerHttpRequest request,
                            List<WebSocketExtension> requested,
                            List<WebSocketExtension> supported) {
                        // Rechaza permessage-deflate para compatibilidad con OkHttp en Android
                        return Collections.emptyList();
                    }
                });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JwtChannelInterceptor valida JWT en CONNECT y autoriza destinos en SUBSCRIBE/SEND
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        log.info("WS conexion establecida. session={} remoteAddress={}",
                                session.getId(), session.getRemoteAddress());
                        super.afterConnectionEstablished(session);
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                        log.info("WS conexion cerrada. session={} status={}",
                                session.getId(), closeStatus);
                        super.afterConnectionClosed(session, closeStatus);
                    }
                };
            }
        });
    }
}
