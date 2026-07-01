package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.ProductoCocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoCocinaRepository extends JpaRepository<ProductoCocina, Integer> {

    @Query("SELECT COUNT(pcp) FROM ProductoCocinaPedido pcp WHERE pcp.productoCocina.idProductoCocina = :id")
    int countEnPedidos(@Param("id") int id);
}
