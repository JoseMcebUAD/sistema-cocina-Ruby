package com.cocinarubi.event.ws;

import com.cocinarubi.domain.service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Escucha cambios en pedidos WEB y hace broadcast via STOMP.
 * Capa: Event — puente entre dominio y transporte WebSocket.
 *
 * <p>Usa {@code AFTER_COMMIT} para garantizar que el cliente nunca recibe datos
 * de una transacción que luego hizo rollback. Si no hay transacción activa, el
 * evento se ejecuta sincrónicamente al publicarse.</p>
 */
@Component
public class PedidoWebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoWebSocketListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final PedidoService pedidoService;

    public PedidoWebSocketListener(SimpMessagingTemplate messagingTemplate,
                                   PedidoService pedidoService) {
        this.messagingTemplate = messagingTemplate;
        this.pedidoService = pedidoService;
    }

    /**
     * Envía la lista actualizada y el contador de pedidos WEB sin imprimir
     * a todos los suscriptores activos, una vez que la transacción confirmó.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPedidoWebActualizado(PedidoWebActualizadoEvent event) {
        log.debug("Broadcast pedido-web disparado por {}", event.getSource().getClass().getSimpleName());
        // PedidoService: recupera pedidos WEB con impreso=false ya confirmados en BD
        messagingTemplate.convertAndSend("/pedido-web/lista",    pedidoService.findWebSinImprimir());
        messagingTemplate.convertAndSend("/pedido-web/contador", pedidoService.contarWebSinImprimir());
    }
}
