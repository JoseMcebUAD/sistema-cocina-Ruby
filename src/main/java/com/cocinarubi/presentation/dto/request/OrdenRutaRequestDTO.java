package com.cocinarubi.presentation.dto.request;

import java.time.LocalTime;

/**
 * DTO para actualizar los campos editables de un grupo de rutas (OrdenRuta).
 * Todos los campos son opcionales: solo se aplica el valor si no es null.
 */
public class OrdenRutaRequestDTO {

    private Integer tiempoEstimadoMin;
    private LocalTime horaLlegadaDesde;
    private LocalTime horaLlegadaHasta;

    public OrdenRutaRequestDTO() {}

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }

    public LocalTime getHoraLlegadaDesde() { return horaLlegadaDesde; }
    public void setHoraLlegadaDesde(LocalTime horaLlegadaDesde) { this.horaLlegadaDesde = horaLlegadaDesde; }

    public LocalTime getHoraLlegadaHasta() { return horaLlegadaHasta; }
    public void setHoraLlegadaHasta(LocalTime horaLlegadaHasta) { this.horaLlegadaHasta = horaLlegadaHasta; }
}
