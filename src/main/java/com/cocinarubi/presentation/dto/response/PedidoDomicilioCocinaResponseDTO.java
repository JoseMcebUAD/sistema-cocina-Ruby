package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class PedidoDomicilioCocinaResponseDTO {

    private int idPedido;
    private int idRegistroCliente;
    private String nombreCliente;
    private String telefono;
    private int idRuta;
    private String nombreRuta;
    private String domicilio;
    private BigDecimal precioTarifa;

    public PedidoDomicilioCocinaResponseDTO() {}

    public PedidoDomicilioCocinaResponseDTO(int idPedido, int idRegistroCliente, String nombreCliente,
                                            String telefono, int idRuta, String nombreRuta,
                                            String domicilio, BigDecimal precioTarifa) {
        this.idPedido = idPedido;
        this.idRegistroCliente = idRegistroCliente;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.idRuta = idRuta;
        this.nombreRuta = nombreRuta;
        this.domicilio = domicilio;
        this.precioTarifa = precioTarifa;
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdRegistroCliente() { return idRegistroCliente; }
    public void setIdRegistroCliente(int idRegistroCliente) { this.idRegistroCliente = idRegistroCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getNombreRuta() { return nombreRuta; }
    public void setNombreRuta(String nombreRuta) { this.nombreRuta = nombreRuta; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public BigDecimal getPrecioTarifa() { return precioTarifa; }
    public void setPrecioTarifa(BigDecimal precioTarifa) { this.precioTarifa = precioTarifa; }
}
