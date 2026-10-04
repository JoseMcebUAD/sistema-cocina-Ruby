package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoLineaCombo;
import com.cocinarubi.domain.entity.ComboProducto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComboProductoRepository extends JpaRepository<ComboProducto, Integer> {

    /**
     * Detecta si un producto (comida/desayuno/complemento/producto_cocina) forma parte
     * de algún combo. Usado por los services de esas entidades para bloquear con 409
     * el DELETE de un producto referenciado.
     */
    boolean existsByTipoProductoAndIdProducto(TipoLineaCombo tipoProducto, Integer idProducto);
}
