package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.ProductoCocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoCocinaRepository extends JpaRepository<ProductoCocina, Integer> {

    @Query("SELECT COUNT(pcp) FROM ProductoCocinaPedido pcp WHERE pcp.productoCocina.idProductoCocina = :id")
    int countEnPedidos(@Param("id") int id);

    @Query("SELECT p FROM ProductoCocina p WHERE p.estatus = :estatus ORDER BY p.nombreProducto ASC")
    List<ProductoCocina> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);
}
