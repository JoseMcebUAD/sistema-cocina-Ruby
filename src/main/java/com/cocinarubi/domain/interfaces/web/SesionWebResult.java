package com.cocinarubi.domain.interfaces.web;

import com.cocinarubi.presentation.dto.web.ClienteWebResponseDTO;

/**
 * Resultado de crear/renovar una sesion web de cliente.
 *
 * <p>El {@code tokenPlano} viaja solo por cookie {@code HttpOnly Secure};
 * jamas debe incluirse en el body de la respuesta ni loggearse.
 */
public record SesionWebResult(ClienteWebResponseDTO dto, String tokenPlano) {}
