package com.cocinarubi.presentation.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * Resumen de ingresos del negocio para el apartado de ventas del dashboard.
 * Capa: Response DTO.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasVentasResponseDTO {

    private BigDecimal ingresoTotal;
    private BigDecimal ingresoEfectivo;
    private BigDecimal ingresoTransferencia;
    private BigDecimal ingresoTarjeta;
    /** ingresoTotal menos la suma de pagos registrados a repartidores en el período. */
    private BigDecimal ingresoTotalRepartidor;
    /** Suma de tarifas de envío de pedidos a domicilio (WEB y COCINA) en el período. */
    private BigDecimal ingresoTarifas;
}
