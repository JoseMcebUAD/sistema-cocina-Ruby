package com.cocinarubi.config.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * Registra el contenedor WebSocket de Tomcat como bean de Spring.
 * Capa: Config — infraestructura del protocolo WebSocket.
 *
 * <p>Sin este bean, Tomcat negocia la extensión {@code permessage-deflate} antes de
 * que el {@code DefaultHandshakeHandler} de Spring pueda rechazarla, lo que rompe la
 * conexión con clientes OkHttp (Android). Al declararlo explícitamente, Spring toma
 * control completo del handshake.</p>
 */
@Configuration
public class TomcatWebSocketConfig {

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}
