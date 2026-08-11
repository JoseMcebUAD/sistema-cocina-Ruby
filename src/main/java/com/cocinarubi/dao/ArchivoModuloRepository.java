package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.entity.ArchivoModulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para ArchivoModulo. Cada fila define la carpeta destino en Cloudinary
 * y los MIME types permitidos para un TipoCatalogoProducto estático (BASICO/COMIDA/DESAYUNO)
 * o para una Categoria dinámica.
 */
public interface ArchivoModuloRepository extends JpaRepository<ArchivoModulo, Integer> {

    // Módulos estáticos identificados por enum (BASICO, COMIDA, DESAYUNO).
    Optional<ArchivoModulo> findByTipoCatalogoProducto(TipoCatalogoProducto tipo);

    // Módulos dinámicos identificados por FK a categoria.
    Optional<ArchivoModulo> findByCategoria_IdCategoria(Integer idCategoria);

    // Carga inicial del cache: módulos estáticos.
    List<ArchivoModulo> findByTipoCatalogoProductoIsNotNull();

    // Carga inicial del cache: módulos dinámicos por categoria.
    List<ArchivoModulo> findByCategoriaIsNotNull();
}
