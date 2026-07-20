package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Comida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT c FROM Comida c WHERE c.estatus = :estatus ORDER BY c.nombreComida ASC")
    List<Comida> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);

    @Query(value = "SELECT c FROM Comida c ORDER BY " +
                   "CASE c.estatus WHEN 'DISPONIBLE' THEN 0 WHEN 'NO_DISPONIBLE' THEN 1 WHEN 'AGOTADO' THEN 2 ELSE 3 END, " +
                   "c.nombreComida ASC",
           countQuery = "SELECT COUNT(c) FROM Comida c")
    Page<Comida> findAllPaginado(Pageable pageable);
}
