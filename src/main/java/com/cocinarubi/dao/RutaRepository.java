package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RutaRepository extends JpaRepository<Ruta, Integer> {

    @Override
    @Query("SELECT r FROM Ruta r ORDER BY r.nombre ASC")
    List<Ruta> findAll();

    boolean existsByNombre(String nombre);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.ruta.idRuta = :id")
    long countClientesConRuta(@Param("id") int id);

    @Query("SELECT COUNT(pd) FROM PedidoDomicilio pd WHERE pd.ruta.idRuta = :id")
    long countPedidosDomicilioConRuta(@Param("id") int id);
}
