package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponseDTO {

    private int idPedido;
    private MetodoPago metodoPagoPrincipal;
    private MetodoPago metodoPagoSecundario;
    private TipoPedido tipoPedido;
    private LocalDateTime fechaExpedicionPedido;
    private PedidoCreadoDesde pedidoCreadoDesde;
    private BigDecimal precioFinalOrden;
    private BigDecimal pagoCliente;
    private BigDecimal cambio;
    private String uuidCliente;
    private List<ComidaPedidoResponseDTO> comidas;
    private List<DesayunoPedidoResponseDTO> desayunos;
    private List<BasicoPedidoResponseDTO> basicos;
    private List<ProductoCocinaPedidoResponseDTO> productosCocina;
    private PedidoDomicilioResponseDTO domicilio;

    public PedidoResponseDTO() {}

    public PedidoResponseDTO(int idPedido, MetodoPago metodoPagoPrincipal, MetodoPago metodoPagoSecundario,
                             TipoPedido tipoPedido,
                             LocalDateTime fechaExpedicionPedido, PedidoCreadoDesde pedidoCreadoDesde,
                             BigDecimal precioFinalOrden, BigDecimal pagoCliente, BigDecimal cambio,
                             String uuidCliente,
                             List<ComidaPedidoResponseDTO> comidas,
                             List<DesayunoPedidoResponseDTO> desayunos,
                             List<BasicoPedidoResponseDTO> basicos,
                             List<ProductoCocinaPedidoResponseDTO> productosCocina,
                             PedidoDomicilioResponseDTO domicilio) {
        this.idPedido = idPedido;
        this.metodoPagoPrincipal = metodoPagoPrincipal;
        this.metodoPagoSecundario = metodoPagoSecundario;
        this.tipoPedido = tipoPedido;
        this.fechaExpedicionPedido = fechaExpedicionPedido;
        this.pedidoCreadoDesde = pedidoCreadoDesde;
        this.precioFinalOrden = precioFinalOrden;
        this.pagoCliente = pagoCliente;
        this.cambio = cambio;
        this.uuidCliente = uuidCliente;
        this.comidas = comidas;
        this.desayunos = desayunos;
        this.basicos = basicos;
        this.productosCocina = productosCocina;
        this.domicilio = domicilio;
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public MetodoPago getMetodoPagoPrincipal() { return metodoPagoPrincipal; }
    public void setMetodoPagoPrincipal(MetodoPago metodoPagoPrincipal) { this.metodoPagoPrincipal = metodoPagoPrincipal; }

    public MetodoPago getMetodoPagoSecundario() { return metodoPagoSecundario; }
    public void setMetodoPagoSecundario(MetodoPago metodoPagoSecundario) { this.metodoPagoSecundario = metodoPagoSecundario; }

    public TipoPedido getTipoPedido() { return tipoPedido; }
    public void setTipoPedido(TipoPedido tipoPedido) { this.tipoPedido = tipoPedido; }

    public LocalDateTime getFechaExpedicionPedido() { return fechaExpedicionPedido; }
    public void setFechaExpedicionPedido(LocalDateTime fechaExpedicionPedido) { this.fechaExpedicionPedido = fechaExpedicionPedido; }

    public PedidoCreadoDesde getPedidoCreadoDesde() { return pedidoCreadoDesde; }
    public void setPedidoCreadoDesde(PedidoCreadoDesde pedidoCreadoDesde) { this.pedidoCreadoDesde = pedidoCreadoDesde; }

    public BigDecimal getPrecioFinalOrden() { return precioFinalOrden; }
    public void setPrecioFinalOrden(BigDecimal precioFinalOrden) { this.precioFinalOrden = precioFinalOrden; }

    public BigDecimal getPagoCliente() { return pagoCliente; }
    public void setPagoCliente(BigDecimal pagoCliente) { this.pagoCliente = pagoCliente; }

    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio; }

    public String getUuidCliente() { return uuidCliente; }
    public void setUuidCliente(String uuidCliente) { this.uuidCliente = uuidCliente; }

    public List<ComidaPedidoResponseDTO> getComidas() { return comidas; }
    public void setComidas(List<ComidaPedidoResponseDTO> comidas) { this.comidas = comidas; }

    public List<DesayunoPedidoResponseDTO> getDesayunos() { return desayunos; }
    public void setDesayunos(List<DesayunoPedidoResponseDTO> desayunos) { this.desayunos = desayunos; }

    public List<BasicoPedidoResponseDTO> getBasicos() { return basicos; }
    public void setBasicos(List<BasicoPedidoResponseDTO> basicos) { this.basicos = basicos; }

    public List<ProductoCocinaPedidoResponseDTO> getProductosCocina() { return productosCocina; }
    public void setProductosCocina(List<ProductoCocinaPedidoResponseDTO> productosCocina) { this.productosCocina = productosCocina; }

    public PedidoDomicilioResponseDTO getDomicilio() { return domicilio; }
    public void setDomicilio(PedidoDomicilioResponseDTO domicilio) { this.domicilio = domicilio; }
}
