package com.cocinarubi.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Parámetros para generar el ticket ESC/POS del corte de caja.
 * Capa: Request DTO — entrada del endpoint POST /corte-caja/impresion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorteCajaImpresionRequestDTO {

    /** Modalidad del corte a generar. */
    @NotNull(message = "El tipo de corte es obligatorio")
    private TipoCorte tipo;

    /**
     * Día del corte. Si es null el servicio usa la fecha actual
     * en zona horaria America/Merida.
     */
    private LocalDate fecha;

    public enum TipoCorte { DOMICILIO, COMEDOR }
}
