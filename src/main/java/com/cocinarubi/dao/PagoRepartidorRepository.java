package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.PagoRepartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepartidorRepository extends JpaRepository<PagoRepartidor, Integer> {

    @Override
    @Query("SELECT p FROM PagoRepartidor p ORDER BY p.fechaPago DESC")
    List<PagoRepartidor> findAll();

    @Query("SELECT COUNT(p) > 0 FROM PagoRepartidor p WHERE FUNCTION('DATE', p.fechaPago) = :fecha")
    boolean existsByFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT p FROM PagoRepartidor p WHERE p.fechaPago >= :desde AND p.fechaPago < :hasta ORDER BY p.fechaPago ASC")
    List<PagoRepartidor> findByRango(@Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta);
}
