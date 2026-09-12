package com.cocinarubi.presentation.dto.response;

import java.time.LocalTime;

public class OrdenRutaResponseDTO {

    private Integer idOrdenRuta;
    private Integer tiempoEstimadoMin;
    private LocalTime horaLlegadaDesde;
    private LocalTime horaLlegadaHasta;

    public OrdenRutaResponseDTO() {}

    public OrdenRutaResponseDTO(Integer idOrdenRuta, Integer tiempoEstimadoMin,
                                LocalTime horaLlegadaDesde, LocalTime horaLlegadaHasta) {
        this.idOrdenRuta = idOrdenRuta;
        this.tiempoEstimadoMin = tiempoEstimadoMin;
        this.horaLlegadaDesde = horaLlegadaDesde;
        this.horaLlegadaHasta = horaLlegadaHasta;
    }

    public Integer getIdOrdenRuta() { return idOrdenRuta; }
    public void setIdOrdenRuta(Integer idOrdenRuta) { this.idOrdenRuta = idOrdenRuta; }

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }

    public LocalTime getHoraLlegadaDesde() { return horaLlegadaDesde; }
    public void setHoraLlegadaDesde(LocalTime horaLlegadaDesde) { this.horaLlegadaDesde = horaLlegadaDesde; }

    public LocalTime getHoraLlegadaHasta() { return horaLlegadaHasta; }
    public void setHoraLlegadaHasta(LocalTime horaLlegadaHasta) { this.horaLlegadaHasta = horaLlegadaHasta; }
}
