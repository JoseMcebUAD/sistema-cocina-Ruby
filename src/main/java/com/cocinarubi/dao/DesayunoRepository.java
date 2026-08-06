package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Desayuno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DesayunoRepository extends JpaRepository<Desayuno, Integer> {

    @Override
    @Query("SELECT d FROM Desayuno d ORDER BY d.nombreDesayuno ASC")
    List<Desayuno> findAll();

    boolean existsByNombreDesayuno(String nombreDesayuno);

    @Query("SELECT CASE WHEN COUNT(dp) > 0 THEN true ELSE false END FROM DesayunoPedido dp WHERE dp.desayuno.idDesayuno = :id")
    boolean existsEnPedidos(@Param("id") int id);

    @Query("SELECT d FROM Desayuno d WHERE d.estatus = :estatus ORDER BY d.nombreDesayuno ASC")
    List<Desayuno> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);

    @Query(value = "SELECT d FROM Desayuno d WHERE d.estatus = :estatus ORDER BY d.destacado DESC, d.nombreDesayuno ASC",
           countQuery = "SELECT COUNT(d) FROM Desayuno d WHERE d.estatus = :estatus")
    Page<Desayuno> findDisponiblesPaginado(@Param("estatus") DBConstants.Estatus estatus, Pageable pageable);

    @Query(value = "SELECT d FROM Desayuno d ORDER BY " +
                   "d.destacado DESC, " +
                   "CASE d.estatus WHEN 'DISPONIBLE' THEN 0 WHEN 'NO_DISPONIBLE' THEN 1 WHEN 'AGOTADO' THEN 2 ELSE 3 END, " +
                   "d.nombreDesayuno ASC",
           countQuery = "SELECT COUNT(d) FROM Desayuno d")
    Page<Desayuno> findAllPaginado(Pageable pageable);

    @Query("SELECT d FROM Desayuno d " +
           "WHERE LOWER(d.nombreDesayuno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "AND d.estatus = :estatus " +
           "ORDER BY d.destacado DESC, d.nombreDesayuno ASC")
    List<Desayuno> buscarDisponiblesPorNombre(@Param("termino") String termino,
                                              @Param("estatus") DBConstants.Estatus estatus);
}
