package com.cocinarubi.presentation.dto.response;

public class OrdenRutaResponseDTO {

    private Integer idOrdenRuta;
    private Integer tiempoEstimadoMin;

    public OrdenRutaResponseDTO() {}

    public OrdenRutaResponseDTO(Integer idOrdenRuta, Integer tiempoEstimadoMin) {
        this.idOrdenRuta = idOrdenRuta;
        this.tiempoEstimadoMin = tiempoEstimadoMin;
    }

    public Integer getIdOrdenRuta() { return idOrdenRuta; }
    public void setIdOrdenRuta(Integer idOrdenRuta) { this.idOrdenRuta = idOrdenRuta; }

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }
}
