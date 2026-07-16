package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.ProductoCocina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoCocinaRepository extends JpaRepository<ProductoCocina, Integer> {

    @Query("SELECT COUNT(pcp) FROM ProductoCocinaPedido pcp WHERE pcp.productoCocina.idProductoCocina = :id")
    int countEnPedidos(@Param("id") int id);

    @Query("SELECT p FROM ProductoCocina p WHERE p.estatus = :estatus ORDER BY p.nombreProducto ASC")
    List<ProductoCocina> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);

    @Query(value = "SELECT p FROM ProductoCocina p ORDER BY " +
                   "CASE p.estatus WHEN 'DISPONIBLE' THEN 0 WHEN 'NO_DISPONIBLE' THEN 1 WHEN 'AGOTADO' THEN 2 ELSE 3 END, " +
                   "p.tipoProducto ASC, " +
                   "p.nombreProducto ASC",
           countQuery = "SELECT COUNT(p) FROM ProductoCocina p")
    Page<ProductoCocina> findAllPaginado(Pageable pageable);
}
