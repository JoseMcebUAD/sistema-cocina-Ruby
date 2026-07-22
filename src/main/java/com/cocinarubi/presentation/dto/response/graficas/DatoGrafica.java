package com.cocinarubi.presentation.dto.response.graficas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Punto de datos genérico para gráficas del dashboard.
 * Capa: DTO — valor y etiqueta por punto de la serie.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatoGrafica {

    private BigDecimal valor;
    private String leyenda;
}
