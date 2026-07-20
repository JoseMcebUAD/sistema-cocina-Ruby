package com.cocinarubi.util.template.data;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.presentation.dto.response.BasicoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComidaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.DesayunoPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoDomicilioResponseDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaPedidoResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de datos que consume {@code PedidoTicketTemplate} para renderizar el ticket.
 * Contiene únicamente lo que el ticket necesita imprimir; el campo {@code domicilio}
 * es null cuando el pedido no es de tipo DOMICILIO.
 */
public class PedidoTicketData {

    private int numeroPedido;
    private MetodoPago metodoPagoPrincipal;
    private MetodoPago metodoPagoSecundario;
    private TipoPedido tipoPedido;
    private LocalDateTime fechaExpedicionPedido;
    private BigDecimal precioFinalOrden;
    private BigDecimal pagoCliente;
    private BigDecimal cambio;
    private List<ComidaPedidoResponseDTO> comidas;
    private List<DesayunoPedidoResponseDTO> desayunos;
    private List<BasicoPedidoResponseDTO> basicos;
    private List<ProductoCocinaPedidoResponseDTO> productosCocina;
    private PedidoDomicilioResponseDTO domicilio;

    public PedidoTicketData() {}

    public int getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(int numeroPedido) { this.numeroPedido = numeroPedido; }

    public MetodoPago getMetodoPagoPrincipal() { return metodoPagoPrincipal; }
    public void setMetodoPagoPrincipal(MetodoPago metodoPagoPrincipal) { this.metodoPagoPrincipal = metodoPagoPrincipal; }

    public MetodoPago getMetodoPagoSecundario() { return metodoPagoSecundario; }
    public void setMetodoPagoSecundario(MetodoPago metodoPagoSecundario) { this.metodoPagoSecundario = metodoPagoSecundario; }

    public TipoPedido getTipoPedido() { return tipoPedido; }
    public void setTipoPedido(TipoPedido tipoPedido) { this.tipoPedido = tipoPedido; }

    public LocalDateTime getFechaExpedicionPedido() { return fechaExpedicionPedido; }
    public void setFechaExpedicionPedido(LocalDateTime fechaExpedicionPedido) { this.fechaExpedicionPedido = fechaExpedicionPedido; }

    public BigDecimal getPrecioFinalOrden() { return precioFinalOrden; }
    public void setPrecioFinalOrden(BigDecimal precioFinalOrden) { this.precioFinalOrden = precioFinalOrden; }

    public BigDecimal getPagoCliente() { return pagoCliente; }
    public void setPagoCliente(BigDecimal pagoCliente) { this.pagoCliente = pagoCliente; }

    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio; }

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
