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

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c WHERE c.ruta.idRuta = :id")
    boolean existsClientesConRuta(@Param("id") int id);

    @Query("SELECT CASE WHEN COUNT(pd) > 0 THEN true ELSE false END FROM PedidoDomicilio pd WHERE pd.ruta.idRuta = :id")
    boolean existsPedidosDomicilioConRuta(@Param("id") int id);
}
