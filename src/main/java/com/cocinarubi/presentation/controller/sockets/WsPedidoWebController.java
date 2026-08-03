package com.cocinarubi.presentation.controller.sockets;

import com.cocinarubi.Constants;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.presentation.dto.response.SnapshotPedidoWebResponseDTO;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * Entrega el estado inicial de pedidos WEB al cliente en el momento de la suscripción.
 * Capa: Controller — snapshot STOMP (no broadcast).
 *
 * <p>El valor de retorno de {@code @SubscribeMapping} se envía directamente al
 * suscriptor como frame MESSAGE privado, sin pasar por el broker. Esto asegura
 * que el cliente tenga datos actuales desde el primer instante, antes de que
 * llegue cualquier broadcast posterior.</p>
 */
@Controller
public class WsPedidoWebController {

    private final PedidoService pedidoService;

    public WsPedidoWebController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Retorna la lista completa de pedidos WEB sin imprimir y el timestamp del servidor al momento de suscribirse. */
    @SubscribeMapping("/pedido-web/lista")
    public SnapshotPedidoWebResponseDTO snapshotLista() {
        return new SnapshotPedidoWebResponseDTO(
                LocalDateTime.now(Constants.ZONA_MERIDA),
                pedidoService.findWebSinImprimir()
        );
    }

    /** Retorna el conteo de pedidos WEB sin imprimir al momento de suscribirse. */
    @SubscribeMapping("/pedido-web/contador")
    public long snapshotContador() {
        return pedidoService.contarWebSinImprimir();
    }
}
