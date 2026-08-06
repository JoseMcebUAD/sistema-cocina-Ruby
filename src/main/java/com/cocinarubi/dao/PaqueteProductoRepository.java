package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoLineaPaquete;
import com.cocinarubi.domain.entity.PaqueteProducto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaqueteProductoRepository extends JpaRepository<PaqueteProducto, Integer> {

    /**
     * Detecta si un producto (comida/desayuno/complemento/producto_cocina) forma parte
     * de algún paquete. Usado por los services de esas entidades para bloquear con 409
     * el DELETE de un producto referenciado.
     */
    boolean existsByTipoProductoAndIdProducto(TipoLineaPaquete tipoProducto, Integer idProducto);
}
