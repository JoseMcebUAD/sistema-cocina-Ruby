package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BasicoRequestDTO {

    @NotNull(message = "El id de la comida no puede ser nulo")
    @Positive(message = "El id de la comida debe ser mayor a cero")
    @JsonProperty("idComida")
    private Integer idComida;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("destacado")
    private boolean destacado;

    @NotNull(message = "El precio del básico no puede ser nulo")
    @Positive(message = "El precio del básico debe ser mayor a cero")
    @JsonProperty("precioBasico")
    private BigDecimal precioBasico;

    @NotNull(message = "El estatus del básico no puede ser nulo")
    @JsonProperty("estatus")
    private Estatus estatus;

    @JsonProperty("idComplementos")
    private List<Integer> idComplementos = new ArrayList<>();

    @JsonProperty("descuentoActivo")
    private boolean descuentoActivo = false;

    @JsonProperty("descripcionDescuento")
    private String descripcionDescuento;

    @JsonProperty("precioDescuento")
    private BigDecimal precioDescuento;

    @JsonProperty("saltarConfirmacion")
    private boolean saltarConfirmacion = false;

    public BasicoRequestDTO() {}

    public BasicoRequestDTO(Integer idComida, String descripcion, boolean destacado,
                             BigDecimal precioBasico, Estatus estatus, List<Integer> idComplementos) {
        this.idComida = idComida;
        this.descripcion = descripcion;
        this.destacado = destacado;
        this.precioBasico = precioBasico;
        this.estatus = estatus;
        this.idComplementos = idComplementos != null ? idComplementos : new ArrayList<>();
    }

    public Integer getIdComida() { return idComida; }
    public void setIdComida(Integer idComida) { this.idComida = idComida; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public BigDecimal getPrecioBasico() { return precioBasico; }
    public void setPrecioBasico(BigDecimal precioBasico) { this.precioBasico = precioBasico; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }

    public List<Integer> getIdComplementos() { return idComplementos; }
    public void setIdComplementos(List<Integer> idComplementos) { this.idComplementos = idComplementos; }

    public boolean isDescuentoActivo() { return descuentoActivo; }
    public void setDescuentoActivo(boolean descuentoActivo) { this.descuentoActivo = descuentoActivo; }

    public String getDescripcionDescuento() { return descripcionDescuento; }
    public void setDescripcionDescuento(String descripcionDescuento) { this.descripcionDescuento = descripcionDescuento; }

    public BigDecimal getPrecioDescuento() { return precioDescuento; }
    public void setPrecioDescuento(BigDecimal precioDescuento) { this.precioDescuento = precioDescuento; }

    public boolean isSaltarConfirmacion() { return saltarConfirmacion; }
    public void setSaltarConfirmacion(boolean saltarConfirmacion) { this.saltarConfirmacion = saltarConfirmacion; }
}
