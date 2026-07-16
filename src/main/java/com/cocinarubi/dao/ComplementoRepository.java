package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Complemento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ComplementoRepository extends JpaRepository<Complemento, Integer> {

    @Override
    @Query("SELECT c FROM Complemento c ORDER BY c.nombreComplemento ASC")
    List<Complemento> findAll();

    boolean existsByNombreComplemento(String nombreComplemento);

    @Query("SELECT COUNT(bc) FROM BasicoComplemento bc WHERE bc.complemento.idComplemento = :id")
    long countEnBasicos(@Param("id") int id);

    @Query("SELECT COUNT(ccp) FROM ComplementoComidaPedido ccp WHERE ccp.complemento.idComplemento = :id")
    long countEnPedidos(@Param("id") int id);

    @Query("SELECT c FROM Complemento c WHERE c.estatus = :estatus ORDER BY c.nombreComplemento ASC")
    List<Complemento> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);
}
