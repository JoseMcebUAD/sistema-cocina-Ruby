package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload de entrada para crear o actualizar un {@code PagoRepartidor}.
 * Capa: DTO — se traduce a entidad en {@code PagoRepartidorMapper}.
 *
 * <p>{@code idPagoRepartidor} es nullable: se ignora en POST y se requiere en PUT.
 * Las reglas de negocio (día hábil, unicidad por día, monto vs ingreso del día) se validan
 * en {@code PagoRepartidorValidationImp}, no con Bean Validation.</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoRepartidorRequestDTO {

    private Integer idPagoRepartidor;

    private BigDecimal pago;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaPago;
}
