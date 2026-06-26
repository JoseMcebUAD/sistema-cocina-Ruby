package com.cocinarubi.dao;

import com.cocinarubi.entity.PagoRepartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PagoRepartidorRepository extends JpaRepository<PagoRepartidor, Integer> {

    @Override
    @Query("SELECT p FROM PagoRepartidor p ORDER BY p.fechaPago DESC")
    List<PagoRepartidor> findAll();
}
