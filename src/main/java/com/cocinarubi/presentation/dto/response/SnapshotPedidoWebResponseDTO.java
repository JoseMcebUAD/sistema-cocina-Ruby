package com.cocinarubi.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Envelope del snapshot inicial de pedidos WEB sin imprimir.
 * Incluye el timestamp del servidor para que el cliente pueda
 * detectar desfases de reloj y ordenar eventos correctamente.
 */
public class SnapshotPedidoWebResponseDTO {

    private LocalDateTime serverTimestamp;
    private List<PedidoResponseDTO> pedidos;

    public SnapshotPedidoWebResponseDTO(LocalDateTime serverTimestamp, List<PedidoResponseDTO> pedidos) {
        this.serverTimestamp = serverTimestamp;
        this.pedidos = pedidos;
    }

    public LocalDateTime getServerTimestamp() { return serverTimestamp; }
    public List<PedidoResponseDTO> getPedidos() { return pedidos; }
}
