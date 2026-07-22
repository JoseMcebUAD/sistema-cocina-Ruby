package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.InventarioComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventarioComidaRepository extends JpaRepository<InventarioComida, Integer> {

    @Query("SELECT ic FROM InventarioComida ic JOIN FETCH ic.comida WHERE ic.comida.idComida = :idComida")
    List<InventarioComida> findByComidaIdComida(@Param("idComida") int idComida);
}
