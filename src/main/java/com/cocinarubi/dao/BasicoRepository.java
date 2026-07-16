package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Basico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BasicoRepository extends JpaRepository<Basico, Integer> {

    @Override
    @Query("SELECT DISTINCT b FROM Basico b JOIN FETCH b.comida c LEFT JOIN FETCH b.complementos bc LEFT JOIN FETCH bc.complemento ORDER BY c.nombreComida ASC")
    List<Basico> findAll();

    @Query("SELECT b FROM Basico b JOIN FETCH b.comida LEFT JOIN FETCH b.complementos bc LEFT JOIN FETCH bc.complemento WHERE b.idBasico = :id")
    Optional<Basico> findByIdWithComplementos(@Param("id") int id);

    @Query("SELECT DISTINCT b FROM Basico b JOIN FETCH b.comida c LEFT JOIN FETCH b.complementos bc LEFT JOIN FETCH bc.complemento WHERE b.estatus = :estatus ORDER BY c.nombreComida ASC")
    List<Basico> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);
}
