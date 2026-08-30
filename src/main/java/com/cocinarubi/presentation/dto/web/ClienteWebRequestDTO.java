package com.cocinarubi.presentation.dto.web;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteWebRequestDTO {

    @Size(max = 45)
    private String uuidCliente;

    @Size(max = 255)
    private String userAgent;

    private Integer screenWidth;
    private Integer screenHeight;

    private String timezone;
    private String language;
    private Integer colorDepth;

    @Size(max = 45)
    private String ipAddress;
}
