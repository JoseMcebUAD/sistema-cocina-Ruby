package com.cocinarubi.presentation.dto.response;

/**
 * Resultado de la operación atómica marcar-impreso.
 * Capa: Response DTO — transporte de la decisión de concurrencia al cliente.
 */
public class MarcarImpresoResponseDTO {

    private boolean otorgado;

    public MarcarImpresoResponseDTO(boolean otorgado) {
        this.otorgado = otorgado;
    }

    public boolean isOtorgado() {
        return otorgado;
    }
}
