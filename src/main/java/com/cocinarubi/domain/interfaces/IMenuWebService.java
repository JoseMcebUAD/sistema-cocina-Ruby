package com.cocinarubi.domain.interfaces;

import com.cocinarubi.presentation.dto.response.MenuWebResponseDTO;

/**
 * Contrato del servicio de menú web.
 * Agrega todas las secciones del menú en una sola consulta optimizada.
 * Capa: Service.
 */
public interface IMenuWebService {

    /**
     * Retorna todas las secciones del menú web con ítems en estatus DISPONIBLE.
     * Las categorías sin productos disponibles no aparecen en la respuesta.
     */
    MenuWebResponseDTO getMenu();
}
