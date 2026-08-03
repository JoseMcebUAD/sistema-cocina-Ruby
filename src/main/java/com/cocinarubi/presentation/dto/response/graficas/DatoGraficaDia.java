package com.cocinarubi.presentation.dto.response.graficas;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Punto de datos de la gráfica de ventas por día de semana.
 * Extiende DatoGrafica añadiendo el ingreso económico del día.
 * Capa: DTO — respuesta del endpoint /estadisticas/resumen-dias-semana.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class DatoGraficaDia extends DatoGrafica {

    private BigDecimal ingreso;

    /**
     * @param leyenda         nombre del día ("Lunes", "Martes", …)
     * @param cantidadPedidos número de pedidos ese día
     * @param ingreso         suma de precio_final_orden de todos los pedidos del día
     */
    public DatoGraficaDia(String leyenda, BigDecimal cantidadPedidos, BigDecimal ingreso) {
        super(cantidadPedidos, leyenda);
        this.ingreso = ingreso;
    }
}
