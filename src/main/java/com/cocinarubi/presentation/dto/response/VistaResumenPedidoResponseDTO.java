package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VistaResumenPedidoResponseDTO {

    private Integer idPedido;
    private Boolean impreso;
    private String nombreCliente;
    private DBConstants.MetodoPago metodoPagoPrincipal;
    private DBConstants.MetodoPago metodoPagoSecundario;
    private DBConstants.TipoPedido tipoPedido;
    private DBConstants.PedidoCreadoDesde pedidoCreadoDesde;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaExpedicionPedido;

    private BigDecimal precioFinalOrden;
    private BigDecimal pagoClientePrincipal;
    private String ruta;
    private String domicilio;
    private BigDecimal precioTarifa;
}
