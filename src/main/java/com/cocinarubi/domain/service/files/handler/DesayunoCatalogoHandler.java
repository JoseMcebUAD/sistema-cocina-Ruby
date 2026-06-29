package com.cocinarubi.domain.service.files.handler;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.DesayunoRepository;
import org.springframework.stereotype.Component;

/**
 * Handler para la entidad Desayuno. Verifica la existencia del registro en BD
 * antes de permitir la subida de archivos asociados.
 */
@Component
public class DesayunoCatalogoHandler implements CatalogoProductoHandler {

    private final DesayunoRepository desayunoRepository;

    public DesayunoCatalogoHandler(DesayunoRepository desayunoRepository) {
        this.desayunoRepository = desayunoRepository;
    }

    @Override
    public TipoCatalogoProducto getEntityType() {
        return TipoCatalogoProducto.DESAYUNO;
    }

    @Override
    public boolean exists(Integer idEntidad) {
        // Consulta directa al repo sin cargar la entidad completa
        return desayunoRepository.existsById(idEntidad);
    }
}
