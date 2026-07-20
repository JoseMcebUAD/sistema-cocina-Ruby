package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ComidaRepository;
import org.springframework.stereotype.Component;

/**
 * Handler para la entidad Comida. Verifica la existencia del registro en BD
 * antes de permitir la subida de archivos asociados.
 */
@Component
public class ComidaCatalogoHandler implements CatalogoProductoHandler {

    private final ComidaRepository comidaRepository;

    public ComidaCatalogoHandler(ComidaRepository comidaRepository) {
        this.comidaRepository = comidaRepository;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        return TipoCatalogoProducto.COMIDA;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        // Consulta directa al repo sin cargar la entidad completa
        return comidaRepository.existsById(idEntidad);
    }
}
