package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.domain.entity.ProductoCocina;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para ProductoCocina. Extiende la verificación de existencia
 * combinando id y tipoProducto para que cada handler valide el subtipo correcto.
 */
public interface ProductoCocinaRepository extends JpaRepository<ProductoCocina, Integer> {

    // Verifica que el producto exista Y que su tipoProducto coincida con el subtipo declarado
    boolean existsByIdProductoCocinaAndTipoProducto(Integer idProductoCocina, TipoProducto tipoProducto);
}
