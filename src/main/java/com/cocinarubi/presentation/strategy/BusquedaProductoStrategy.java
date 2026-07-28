package com.cocinarubi.presentation.strategy;

import java.util.List;

/**
 * Strategy de búsqueda cross-catálogo.
 * Cada implementación busca en un catálogo filtrando por DISPONIBLE
 * y retorna el tipo que ese catálogo ya usa en sus propios endpoints.
 */
public interface BusquedaProductoStrategy {

    String getNombreCategoria();

    /**
     * Retorna todos los resultados DISPONIBLE que coincidan con el término.
     * La paginación global se aplica en el servicio, no aquí.
     */
    List<?> buscarTodos(String termino);
}
