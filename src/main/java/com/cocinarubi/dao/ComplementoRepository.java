package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Complemento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ComplementoRepository extends JpaRepository<Complemento, Integer> {

    @Override
    @Query("SELECT c FROM Complemento c ORDER BY c.nombreComplemento ASC")
    List<Complemento> findAll();

    boolean existsByNombreComplemento(String nombreComplemento);

    @Query("SELECT CASE WHEN COUNT(bc) > 0 THEN true ELSE false END FROM BasicoComplemento bc WHERE bc.complemento.idComplemento = :id")
    boolean existsEnBasicos(@Param("id") int id);

    @Query("SELECT CASE WHEN COUNT(ccp) > 0 THEN true ELSE false END FROM ComplementoComidaPedido ccp WHERE ccp.complemento.idComplemento = :id")
    boolean existsEnPedidos(@Param("id") int id);

    @Query("SELECT c FROM Complemento c WHERE c.estatus = :estatus ORDER BY c.nombreComplemento ASC")
    List<Complemento> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);

    @Query(value = "SELECT c FROM Complemento c ORDER BY " +
                   "CASE c.estatus WHEN 'DISPONIBLE' THEN 0 WHEN 'NO_DISPONIBLE' THEN 1 WHEN 'AGOTADO' THEN 2 ELSE 3 END, " +
                   "c.nombreComplemento ASC",
           countQuery = "SELECT COUNT(c) FROM Complemento c")
    Page<Complemento> findAllPaginado(Pageable pageable);
}
