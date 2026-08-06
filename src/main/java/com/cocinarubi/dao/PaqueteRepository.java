package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.domain.entity.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaqueteRepository extends JpaRepository<Paquete, Integer> {

    @Query("SELECT p FROM Paquete p LEFT JOIN FETCH p.productos WHERE p.idPaquete = :id")
    Optional<Paquete> findByIdWithProductos(@Param("id") Integer id);

    @Query("SELECT DISTINCT p FROM Paquete p LEFT JOIN FETCH p.productos ORDER BY p.idPaquete ASC")
    List<Paquete> findAllWithProductos();

    @Query("SELECT DISTINCT p FROM Paquete p LEFT JOIN FETCH p.productos WHERE p.estatus = :estatus ORDER BY p.idPaquete ASC")
    List<Paquete> findByEstatusWithProductos(@Param("estatus") Estatus estatus);

    @Query(value = "SELECT DISTINCT p FROM Paquete p LEFT JOIN FETCH p.productos ORDER BY p.idPaquete ASC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Paquete p")
    Page<Paquete> findAllWithProductosPaginado(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Paquete p LEFT JOIN FETCH p.productos WHERE p.estatus = :estatus ORDER BY p.idPaquete ASC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Paquete p WHERE p.estatus = :estatus")
    Page<Paquete> findByEstatusWithProductosPaginado(@Param("estatus") Estatus estatus, Pageable pageable);
}
