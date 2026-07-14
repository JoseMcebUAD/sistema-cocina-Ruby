package com.cocinarubi.presentation.dto.response.pedido;

public class PedidoEscResponseDTO extends EscPosBytesDTO  {
    
    private int idPedido;

    public PedidoEscResponseDTO(int idPedido, String escposData) {
        super(escposData);
        this.idPedido = idPedido;
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }


}
