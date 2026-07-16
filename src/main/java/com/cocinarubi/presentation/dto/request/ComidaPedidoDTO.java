package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TamanoPorcion;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ComidaPedidoDTO {

    @NotNull(message = "El id de la comida no puede ser nulo")
    @Positive(message = "El id de la comida debe ser mayor a cero")
    @JsonProperty("idComida")
    private Integer idComida;

    @NotNull(message = "El precio unitario no puede ser nulo")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    @JsonProperty("precioUnitario")
    private BigDecimal precioUnitario;

    @NotNull(message = "El tamaño de porción no puede ser nulo")
    @JsonProperty("tamanoPorcion")
    private TamanoPorcion tamanoPorcion;

    @JsonProperty("complementos")
    private List<ComplementoPedidoDTO> complementos = new ArrayList<>();

    public ComidaPedidoDTO() {}

    public ComidaPedidoDTO(Integer idComida, BigDecimal precioUnitario,
                           TamanoPorcion tamanoPorcion, List<ComplementoPedidoDTO> complementos) {
        this.idComida = idComida;
        this.precioUnitario = precioUnitario;
        this.tamanoPorcion = tamanoPorcion;
        this.complementos = complementos != null ? complementos : new ArrayList<>();
    }

    public Integer getIdComida() { return idComida; }
    public void setIdComida(Integer idComida) { this.idComida = idComida; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public TamanoPorcion getTamanoPorcion() { return tamanoPorcion; }
    public void setTamanoPorcion(TamanoPorcion tamanoPorcion) { this.tamanoPorcion = tamanoPorcion; }

    public List<ComplementoPedidoDTO> getComplementos() { return complementos; }
    public void setComplementos(List<ComplementoPedidoDTO> complementos) { this.complementos = complementos; }
}
