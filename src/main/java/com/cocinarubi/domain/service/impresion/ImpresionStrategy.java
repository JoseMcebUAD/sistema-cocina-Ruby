package com.cocinarubi.domain.service.impresion;

import com.cocinarubi.DBConstants.TipoEntidadImpresion;
import com.cocinarubi.util.template.BaseTicketTemplate;

/**
 * Estrategia de impresión por tipo de entidad. Cada implementación sabe cómo
 * cargar su entidad y construir el {@link BaseTicketTemplate} correspondiente,
 * y qué hacer una vez que los bytes se generaron correctamente.
 */
public interface ImpresionStrategy {

    /** Tipo de entidad que maneja esta estrategia (usado por el registry para el dispatch). */
    TipoEntidadImpresion getTipo();

    /** Carga la entidad por id y construye el template listo para renderizar. */
    BaseTicketTemplate<?> buildTemplate(Integer id);

    /** Hook post-generación de bytes exitosa (ej. marcar impreso=true). */
    void onPrintSuccess(Integer id);
}
