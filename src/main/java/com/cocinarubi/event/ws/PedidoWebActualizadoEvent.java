package com.cocinarubi.event.ws;

import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cada vez que la lista de pedidos WEB sin imprimir cambia.
 * Capa: Event — desacopla la lógica de negocio del transporte WebSocket.
 *
 * <p>Los consumidores de este evento ({@link PedidoWebSocketListener}) hacen el
 * broadcast via STOMP solo después de que la transacción confirma, gracias a
 * {@code @TransactionalEventListener(AFTER_COMMIT)}.</p>
 */
public class PedidoWebActualizadoEvent extends ApplicationEvent {

    public PedidoWebActualizadoEvent(Object source) {
        super(source);
    }
}
