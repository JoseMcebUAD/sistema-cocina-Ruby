package com.cocinarubi.presentation.dto.response;

public class PedidoCocinaResponseDTO {

    private int idPedido;
    private String nombreCliente;

    public PedidoCocinaResponseDTO() {}

    public PedidoCocinaResponseDTO(int idPedido, String nombreCliente) {
        this.idPedido = idPedido;
        this.nombreCliente = nombreCliente;
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}
