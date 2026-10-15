package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.MetodoPago;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PedidoMetodoPagoDTO {

    @NotNull(message = "El método de pago principal no puede ser nulo")
    @JsonProperty("metodoPagoPrincipal")
    @JsonAlias({"metodoPago", "metodo_pago", "metodo_pago_principal"})
    private MetodoPago metodoPagoPrincipal;

    @JsonProperty("metodoPagoSecundario")
    @JsonAlias({"metodo_pago_secundario"})
    private MetodoPago metodoPagoSecundario;

    @JsonProperty("pagoClientePrincipal")
    @JsonAlias({"pagoCliente", "pago_cliente", "pago_cliente_principal"})
    private BigDecimal pagoClientePrincipal;

    @JsonProperty("pagado")
    @JsonAlias({"marcarPagado", "marcar_pagado"})
    private Boolean pagado = true;

    public PedidoMetodoPagoDTO() {}

    public PedidoMetodoPagoDTO(MetodoPago metodoPagoPrincipal) {
        this.metodoPagoPrincipal = metodoPagoPrincipal;
        this.pagado = true;
    }

    public MetodoPago getMetodoPagoPrincipal() {
        return metodoPagoPrincipal;
    }

    public void setMetodoPagoPrincipal(MetodoPago metodoPagoPrincipal) {
        this.metodoPagoPrincipal = metodoPagoPrincipal;
    }

    public MetodoPago getMetodoPagoSecundario() {
        return metodoPagoSecundario;
    }

    public void setMetodoPagoSecundario(MetodoPago metodoPagoSecundario) {
        this.metodoPagoSecundario = metodoPagoSecundario;
    }

    public BigDecimal getPagoClientePrincipal() {
        return pagoClientePrincipal;
    }

    public void setPagoClientePrincipal(BigDecimal pagoClientePrincipal) {
        this.pagoClientePrincipal = pagoClientePrincipal;
    }

    public Boolean getPagado() {
        return pagado;
    }

    public void setPagado(Boolean pagado) {
        this.pagado = pagado;
    }
}
