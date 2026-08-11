package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RutaRepository extends JpaRepository<Ruta, Integer> {

    @Override
    @Query("SELECT r FROM Ruta r ORDER BY r.orden ASC")
    List<Ruta> findAll();

    Optional<Ruta> findByUuidRuta(String uuidRuta);

    boolean existsByNombre(String nombre);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.ruta.idRuta = :id")
    long countClientesConRuta(@Param("id") int id);

    @Query("SELECT COUNT(pd) FROM PedidoDomicilio pd WHERE pd.ruta.idRuta = :id")
    long countPedidosDomicilioConRuta(@Param("id") int id);
}
