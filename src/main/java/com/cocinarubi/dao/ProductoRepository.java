package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Override
    @Query("SELECT p FROM Producto p ORDER BY p.nombre ASC")
    List<Producto> findAll();

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Producto> findByNombreIgnoreCase(String nombre);

    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END "
            + "FROM ProductoReceta pr WHERE pr.producto.idProducto = :id")
    boolean existsEnReceta(@Param("id") int id);
}
