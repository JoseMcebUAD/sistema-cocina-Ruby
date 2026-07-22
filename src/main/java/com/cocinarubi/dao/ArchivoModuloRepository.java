package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.entity.ArchivoModulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para ArchivoModulo. Cada fila define la carpeta destino en Cloudinary
 * y los MIME types permitidos para un TipoCatalogoProducto específico.
 */
public interface ArchivoModuloRepository extends JpaRepository<ArchivoModulo, Integer> {

    // Recupera la configuración de módulo para un tipo de catálogo concreto
    Optional<ArchivoModulo> findByTipoCatalogoProducto(TipoCatalogoProducto tipo);

    // Usado por ArchivoModuloCache al arrancar para cargar todos los módulos catalogados
    List<ArchivoModulo> findByTipoCatalogoProductoIsNotNull();
}
