package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Integer> {

    List<Subcategoria> findByCategoria_IdCategoriaOrderByNombreAsc(Integer idCategoria);

    /** Devuelve todas las subcategorías ordenadas por categoría/nombre (para armado del árbol). */
    List<Subcategoria> findAllByOrderByCategoria_NombreAscNombreAsc();

    boolean existsByCategoria_IdCategoriaAndNombreIgnoreCase(Integer idCategoria, String nombre);

    boolean existsByCategoria_IdCategoriaAndNombreIgnoreCaseAndIdSubcategoriaNot(
            Integer idCategoria, String nombre, Integer idSubcategoria);

    long countByCategoria_IdCategoria(Integer idCategoria);
}
