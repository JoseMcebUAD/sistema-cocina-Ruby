package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdCategoriaNot(String nombre, Integer idCategoria);

    // LEFT JOIN FETCH hidrata subcategorias en una sola consulta (evita N+1).
    // DISTINCT quita los duplicados que produce el join cartesiano.
    // Se ordena por categoria.nombre y luego subcategoria.nombre para dejar
    // el árbol alfabetizado sin ordenar en memoria.
    @Query("SELECT DISTINCT c FROM Categoria c " +
           "LEFT JOIN FETCH c.subcategorias s " +
           "ORDER BY c.nombre ASC, s.nombre ASC")
    List<Categoria> findAllConSubcategorias();
}
