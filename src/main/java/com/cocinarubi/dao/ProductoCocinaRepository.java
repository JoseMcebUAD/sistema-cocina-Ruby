package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.ProductoCocina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoCocinaRepository extends JpaRepository<ProductoCocina, Integer> {

    @Query("SELECT CASE WHEN COUNT(pcp) > 0 THEN true ELSE false END FROM ProductoCocinaPedido pcp WHERE pcp.productoCocina.idProductoCocina = :id")
    boolean existsEnPedidos(@Param("id") int id);

    // Usado por CategoriaService.delete para bloquear la baja si hay productos asociados.
    boolean existsByCategoria_IdCategoria(Integer idCategoria);

    @Query("SELECT p FROM ProductoCocina p WHERE p.estatus = :estatus ORDER BY p.nombreProducto ASC")
    List<ProductoCocina> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);

    // Usado por MenuDigitalService para las categorías: hidrata subcategorías en una sola query (evita N+1).
    @Query("SELECT DISTINCT p FROM ProductoCocina p LEFT JOIN FETCH p.subcategorias WHERE p.estatus = :estatus ORDER BY p.nombreProducto ASC")
    List<ProductoCocina> findDisponiblesOrdenadosConSubcategoria(@Param("estatus") DBConstants.Estatus estatus);

    @Query(value = "SELECT p FROM ProductoCocina p WHERE p.estatus = :estatus " +
                   "ORDER BY p.destacado DESC, p.categoria.nombre ASC, p.nombreProducto ASC",
           countQuery = "SELECT COUNT(p) FROM ProductoCocina p WHERE p.estatus = :estatus")
    Page<ProductoCocina> findDisponiblesPaginado(@Param("estatus") DBConstants.Estatus estatus, Pageable pageable);

    @Query(value = "SELECT p FROM ProductoCocina p ORDER BY " +
                   "p.destacado DESC, " +
                   "CASE p.estatus WHEN 'DISPONIBLE' THEN 0 WHEN 'NO_DISPONIBLE' THEN 1 WHEN 'AGOTADO' THEN 2 ELSE 3 END, " +
                   "p.categoria.nombre ASC, " +
                   "p.nombreProducto ASC",
           countQuery = "SELECT COUNT(p) FROM ProductoCocina p")
    Page<ProductoCocina> findAllPaginado(Pageable pageable);

    @Query("SELECT p FROM ProductoCocina p " +
           "WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "AND p.estatus = :estatus " +
           "ORDER BY p.destacado DESC, p.categoria.nombre ASC, p.nombreProducto ASC")
    List<ProductoCocina> buscarDisponiblesPorNombre(@Param("termino") String termino,
                                                    @Param("estatus") DBConstants.Estatus estatus);

    // Hidrata subcategorias en la misma query para evitar N+1 al construir el DTO.
    @Query("SELECT DISTINCT pc FROM ProductoCocina pc " +
           "LEFT JOIN FETCH pc.subcategorias " +
           "WHERE pc.idProductoCocina = :id")
    Optional<ProductoCocina> findByIdConSubcategorias(@Param("id") Integer id);
}
