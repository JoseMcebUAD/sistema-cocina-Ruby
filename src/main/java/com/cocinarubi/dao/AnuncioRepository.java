package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Integer> {

    @Query("SELECT a FROM Anuncio a ORDER BY " +
           "CASE a.prioridad WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END ASC, " +
           "a.fechaExpiracionAnuncio ASC")
    List<Anuncio> findAllOrdered();

    @Query("SELECT a FROM Anuncio a WHERE a.activo = true ORDER BY " +
           "CASE a.prioridad WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END ASC, " +
           "a.fechaExpiracionAnuncio ASC")
    List<Anuncio> findAllActivosOrdered();
}
