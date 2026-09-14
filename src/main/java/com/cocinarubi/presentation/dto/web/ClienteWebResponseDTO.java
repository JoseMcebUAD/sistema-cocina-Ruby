package com.cocinarubi.presentation.dto.web;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteWebResponseDTO {

    private Integer idCliente;
    private String uuidCliente;
    private String sessionToken;
    private LocalDateTime tokenExpiracion;
    private String huella;
    private String codigoCliente;
    private String userAgent;
    private String ipAddress;
    private BigDecimal ubicacionLatitud;
    private BigDecimal ubicacionLongitud;
    private String nombre;
    private String direccionCliente;
    private String telefono;
    private Integer idRuta;
}
