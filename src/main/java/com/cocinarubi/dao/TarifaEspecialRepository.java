package com.cocinarubi.dao;

import com.cocinarubi.entity.TarifaEspecial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TarifaEspecialRepository extends JpaRepository<TarifaEspecial, Integer> {

    @Override
    @Query("SELECT t FROM TarifaEspecial t ORDER BY t.nombreTarifa ASC")
    List<TarifaEspecial> findAll();
}
