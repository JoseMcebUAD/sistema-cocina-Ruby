package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

/**
 * Snapshot de una tarifa especial aplicada a un pedido.
 * Capa: DTO de respuesta — expuesto en {@code PedidoResponseDTO.tarifasEspeciales}.
 */
public class TarifaEspecialAplicadaResponseDTO {

    private Integer idTarifa;
    private String nombreTarifa;
    private BigDecimal precioTarifa;

    public TarifaEspecialAplicadaResponseDTO() {}

    public TarifaEspecialAplicadaResponseDTO(Integer idTarifa, String nombreTarifa, BigDecimal precioTarifa) {
        this.idTarifa = idTarifa;
        this.nombreTarifa = nombreTarifa;
        this.precioTarifa = precioTarifa;
    }

    public Integer getIdTarifa() { return idTarifa; }
    public void setIdTarifa(Integer idTarifa) { this.idTarifa = idTarifa; }

    public String getNombreTarifa() { return nombreTarifa; }
    public void setNombreTarifa(String nombreTarifa) { this.nombreTarifa = nombreTarifa; }

    public BigDecimal getPrecioTarifa() { return precioTarifa; }
    public void setPrecioTarifa(BigDecimal precioTarifa) { this.precioTarifa = precioTarifa; }
}
