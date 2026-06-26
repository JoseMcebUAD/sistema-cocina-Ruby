package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Comida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ComidaRepository extends JpaRepository<Comida, Integer> {

    @Override
    @Query("SELECT c FROM Comida c ORDER BY c.nombreComida ASC")
    List<Comida> findAll();

    boolean existsByNombreComida(String nombreComida);

    @Query("SELECT COUNT(cp) FROM ComidaPedido cp WHERE cp.comida.idComida = :id")
    long countEnPedidos(@Param("id") int id);

    @Query("SELECT COUNT(b) FROM Basico b WHERE b.comida.idComida = :id")
    long countEnBasicos(@Param("id") int id);
}
