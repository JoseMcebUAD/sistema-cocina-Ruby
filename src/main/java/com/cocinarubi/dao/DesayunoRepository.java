package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Desayuno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DesayunoRepository extends JpaRepository<Desayuno, Integer> {

    @Override
    @Query("SELECT d FROM Desayuno d ORDER BY d.nombreDesayuno ASC")
    List<Desayuno> findAll();

    boolean existsByNombreDesayuno(String nombreDesayuno);

    @Query("SELECT COUNT(dp) FROM DesayunoPedido dp WHERE dp.desayuno.idDesayuno = :id")
    long countEnPedidos(@Param("id") int id);

    @Query("SELECT d FROM Desayuno d WHERE d.estatus = :estatus ORDER BY d.nombreDesayuno ASC")
    List<Desayuno> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);
}
