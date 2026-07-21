package com.cocinarubi.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entrada del endpoint {@code GET /pago-repartidor/rango}: representa un día hábil dentro
 * del rango consultado, con su pago asociado (o {@code null} si ese día no tiene registro).
 * Capa: DTO — respuesta paginada.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoRepartidorPorFechaResponseDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private PagoRepartidorResponseDTO pagoRepartidor;
}
