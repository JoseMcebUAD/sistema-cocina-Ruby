package com.cocinarubi.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String nombreUsuario,
        @NotBlank String contrasena
) {}
