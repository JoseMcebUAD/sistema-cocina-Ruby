package com.cocinarubi.presentation.dto.web;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaWebResponseDTO {

    private String uuidRuta;
    private String nombre;
    private boolean active;
    private BigDecimal tarifaEnvio;
    private Integer tiempoEstimadoMin;
    private Integer orden;
}
