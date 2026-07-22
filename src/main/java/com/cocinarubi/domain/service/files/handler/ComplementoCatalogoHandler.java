package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.ComplementoRepository;
import org.springframework.stereotype.Component;

/**
 * Handler para la entidad Complemento. Verifica la existencia del registro en BD
 * antes de permitir la subida de archivos asociados.
 */
@Component
public class ComplementoCatalogoHandler implements CatalogoProductoHandler {

    private final ComplementoRepository complementoRepository;

    public ComplementoCatalogoHandler(ComplementoRepository complementoRepository) {
        this.complementoRepository = complementoRepository;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        return TipoCatalogoProducto.COMPLEMENTO;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        // Consulta directa al repo sin cargar la entidad completa
        return complementoRepository.existsById(idEntidad);
    }
}
