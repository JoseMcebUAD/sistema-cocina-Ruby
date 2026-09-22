package com.cocinarubi.presentation.dto.web;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vista pública del cliente para el flujo web.
 *
 * <p>NO incluye {@code sessionToken}, {@code huella}, {@code userAgent} ni
 * {@code ipAddress}: el token viaja solo por cookie HttpOnly y los otros
 * son fingerprinting interno que no debe salir al frontend.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteWebResponseDTO {

    private Integer idCliente;
    private String uuidCliente;
    private LocalDateTime tokenExpiracion;
    private String codigoCliente;
    private BigDecimal ubicacionLatitud;
    private BigDecimal ubicacionLongitud;
    private String nombre;
    private String direccionCliente;
    private String telefono;
    private Integer idRuta;
}
