package com.cocinarubi.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representación de salida de {@code PagoRepartidor}.
 * Capa: DTO — evita exponer la entidad JPA directamente en la respuesta REST.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoRepartidorResponseDTO {

    private Integer idPagoRepartidor;
    private BigDecimal pago;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaPago;
}
