package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.domain.entity.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComboRepository extends JpaRepository<Combo, Integer> {

    @Query("SELECT c FROM Combo c LEFT JOIN FETCH c.productos WHERE c.idCombo = :id")
    Optional<Combo> findByIdWithProductos(@Param("id") Integer id);

    @Query("SELECT DISTINCT c FROM Combo c LEFT JOIN FETCH c.productos ORDER BY c.idCombo ASC")
    List<Combo> findAllWithProductos();

    @Query("SELECT DISTINCT c FROM Combo c LEFT JOIN FETCH c.productos WHERE c.estatus = :estatus ORDER BY c.idCombo ASC")
    List<Combo> findByEstatusWithProductos(@Param("estatus") Estatus estatus);

    @Query(value = "SELECT DISTINCT c FROM Combo c LEFT JOIN FETCH c.productos ORDER BY c.idCombo ASC",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Combo c")
    Page<Combo> findAllWithProductosPaginado(Pageable pageable);

    @Query(value = "SELECT DISTINCT c FROM Combo c LEFT JOIN FETCH c.productos WHERE c.estatus = :estatus ORDER BY c.idCombo ASC",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Combo c WHERE c.estatus = :estatus")
    Page<Combo> findByEstatusWithProductosPaginado(@Param("estatus") Estatus estatus, Pageable pageable);
}
