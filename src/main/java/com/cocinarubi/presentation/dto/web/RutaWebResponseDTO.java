package com.cocinarubi.presentation.dto.web;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaWebResponseDTO {

    private int idRuta;
    private String uuidRuta;
    private String nombre;
    private boolean active;
    private BigDecimal tarifaEnvio;
    private Integer idOrdenRuta;

    /** Inicio de la ventana horaria de reparto del grupo (OrdenRuta). Null si no tiene grupo asignado. */
    private LocalTime horaLlegadaDesde;

    /** Fin de la ventana horaria de reparto del grupo (OrdenRuta). Null si no tiene grupo asignado. */
    private LocalTime horaLlegadaHasta;
}
