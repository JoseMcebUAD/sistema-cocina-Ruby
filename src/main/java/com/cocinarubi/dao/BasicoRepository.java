package com.cocinarubi.dao;

import com.cocinarubi.entity.Basico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BasicoRepository extends JpaRepository<Basico, Integer> {

    @Override
    @Query("SELECT b FROM Basico b JOIN b.comida c ORDER BY c.nombreComida ASC")
    List<Basico> findAll();

    @Query("SELECT COUNT(bc) FROM BasicoComplemento bc WHERE bc.basico.idBasico = :id")
    long countComplementosByBasicoId(@Param("id") int id);
}
