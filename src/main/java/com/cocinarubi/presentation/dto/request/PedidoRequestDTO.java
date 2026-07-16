package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PedidoRequestDTO {

    @NotNull(message = "El método de pago principal no puede ser nulo")
    @JsonProperty("metodoPagoPrincipal")
    private MetodoPago metodoPagoPrincipal;

    @JsonProperty("metodoPagoSecundario")
    private MetodoPago metodoPagoSecundario;

    @NotNull(message = "El tipo de pedido no puede ser nulo")
    @JsonProperty("tipoPedido")
    private TipoPedido tipoPedido;

    @JsonProperty("pedidoCreadoDesde")
    private PedidoCreadoDesde pedidoCreadoDesde = PedidoCreadoDesde.COCINA;

    @JsonProperty("pagoCliente")
    private BigDecimal pagoCliente;

    @JsonProperty("uuidCliente")
    private String uuidCliente;

    @Valid
    @JsonProperty("comidas")
    private List<ComidaPedidoDTO> comidas = new ArrayList<>();

    @Valid
    @JsonProperty("desayunos")
    private List<DesayunoPedidoDTO> desayunos = new ArrayList<>();

    @Valid
    @JsonProperty("basicos")
    private List<BasicoPedidoDTO> basicos = new ArrayList<>();

    @Valid
    @JsonProperty("productosCocina")
    private List<ProductoCocinaPedidoDTO> productosCocina = new ArrayList<>();

    @Valid
    @JsonProperty("domicilio")
    private PedidoDomicilioDTO domicilio;

    @JsonProperty("saltarConfirmacion")
    private boolean saltarConfirmacion = false;

    public PedidoRequestDTO() {}

    public MetodoPago getMetodoPagoPrincipal() { return metodoPagoPrincipal; }
    public void setMetodoPagoPrincipal(MetodoPago metodoPagoPrincipal) { this.metodoPagoPrincipal = metodoPagoPrincipal; }

    public MetodoPago getMetodoPagoSecundario() { return metodoPagoSecundario; }
    public void setMetodoPagoSecundario(MetodoPago metodoPagoSecundario) { this.metodoPagoSecundario = metodoPagoSecundario; }

    public TipoPedido getTipoPedido() { return tipoPedido; }
    public void setTipoPedido(TipoPedido tipoPedido) { this.tipoPedido = tipoPedido; }

    public PedidoCreadoDesde getPedidoCreadoDesde() { return pedidoCreadoDesde; }
    public void setPedidoCreadoDesde(PedidoCreadoDesde pedidoCreadoDesde) { this.pedidoCreadoDesde = pedidoCreadoDesde; }

    public BigDecimal getPagoCliente() { return pagoCliente; }
    public void setPagoCliente(BigDecimal pagoCliente) { this.pagoCliente = pagoCliente; }

    public String getUuidCliente() { return uuidCliente; }
    public void setUuidCliente(String uuidCliente) { this.uuidCliente = uuidCliente; }

    public List<ComidaPedidoDTO> getComidas() { return comidas; }
    public void setComidas(List<ComidaPedidoDTO> comidas) {
        this.comidas = comidas != null ? comidas : new ArrayList<>();
    }

    public List<DesayunoPedidoDTO> getDesayunos() { return desayunos; }
    public void setDesayunos(List<DesayunoPedidoDTO> desayunos) {
        this.desayunos = desayunos != null ? desayunos : new ArrayList<>();
    }

    public List<BasicoPedidoDTO> getBasicos() { return basicos; }
    public void setBasicos(List<BasicoPedidoDTO> basicos) {
        this.basicos = basicos != null ? basicos : new ArrayList<>();
    }

    public List<ProductoCocinaPedidoDTO> getProductosCocina() { return productosCocina; }
    public void setProductosCocina(List<ProductoCocinaPedidoDTO> productosCocina) {
        this.productosCocina = productosCocina != null ? productosCocina : new ArrayList<>();
    }

    public PedidoDomicilioDTO getDomicilio() { return domicilio; }
    public void setDomicilio(PedidoDomicilioDTO domicilio) { this.domicilio = domicilio; }

    public boolean isSaltarConfirmacion() { return saltarConfirmacion; }
    public void setSaltarConfirmacion(boolean saltarConfirmacion) { this.saltarConfirmacion = saltarConfirmacion; }
}
