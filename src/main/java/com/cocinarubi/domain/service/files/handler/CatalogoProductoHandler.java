package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;

/**
 * Contrato de handler para el subsistema de archivos. Cada implementación decide
 * en qué tabla existe la entidad dueña del archivo.
 *
 * <p>Dos tipos de handler:
 * <ul>
 *   <li><b>Estático</b>: {@link #getEntityType()} devuelve un valor de
 *       {@link TipoCatalogoProducto} (COMIDA / DESAYUNO / BASICO). El factory
 *       lo registra en un EnumMap y lo resuelve por enum.</li>
 *   <li><b>Universal</b> (dinámico): {@link #getEntityType()} devuelve {@code null}.
 *       El factory lo usa para cualquier lookup por {@code idCategoria}, dado
 *       que todos los productos de cocina dinámicos viven en la misma tabla.</li>
 * </ul></p>
 */
public interface CatalogoProductoHandler {

    /**
     * Enum estático que gestiona este handler, o {@code null} si es el handler
     * universal para categorías dinámicas.
     */
    TipoCatalogoProducto getEntityType();

    /** True si la entidad con el id dado existe en su tabla correspondiente. */
    boolean exists(Integer idEntidad);
}
