package com.cocinarubi.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ingreso agregado por ruta de reparto para el endpoint de estadísticas.
 * Capa: Response DTO — también se usa como proyección JPQL (constructor expression).
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaRutaItemDTO {

    private Integer idRuta;
    private String nombre;
    @JsonIgnore
    private Integer orden;
    private BigDecimal ingresos;

    /** Suma ingresos adicionales al fusionar resultados WEB + COCINA en el servicio. */
    public void addIngresos(BigDecimal extra) {
        this.ingresos = this.ingresos.add(extra);
    }
}
