package com.cocinarubi.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VistaResumenPedidoConMetricasResponseDTO {

    private Page<VistaResumenPedidoResponseDTO> pedidos;
    private long cantidadTotal;
    private long cantidadImpresos;
    private long cantidadNoImpresos;
    private BigDecimal ingresoTotal;
    private BigDecimal ingresoEfectivo;
    private BigDecimal ingresoTransferencia;
    private BigDecimal ingresoTarjeta;
}
