package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Basico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BasicoRepository extends JpaRepository<Basico, Integer> {

    @Override
    @Query("SELECT DISTINCT b FROM Basico b JOIN FETCH b.comida c LEFT JOIN FETCH b.complementos bc LEFT JOIN FETCH bc.complemento ORDER BY c.nombreComida ASC")
    List<Basico> findAll();

    Optional<Basico> findByUuidBasico(String uuidBasico);

    @Query("SELECT b FROM Basico b JOIN FETCH b.comida LEFT JOIN FETCH b.complementos bc LEFT JOIN FETCH bc.complemento WHERE b.idBasico = :id")
    Optional<Basico> findByIdWithComplementos(@Param("id") int id);

    @Query("SELECT CASE WHEN COUNT(bp) > 0 THEN true ELSE false END FROM BasicoPedido bp WHERE bp.basico.idBasico = :id")
    boolean existsEnPedidos(@Param("id") int id);

    @Query("SELECT DISTINCT b FROM Basico b JOIN FETCH b.comida c LEFT JOIN FETCH b.complementos bc LEFT JOIN FETCH bc.complemento WHERE b.estatus = :estatus ORDER BY c.nombreComida ASC")
    List<Basico> findDisponiblesOrdenados(@Param("estatus") DBConstants.Estatus estatus);

    @Query(value = "SELECT b FROM Basico b ORDER BY " +
                   "b.destacado DESC, " +
                   "CASE b.estatus WHEN 'DISPONIBLE' THEN 0 WHEN 'NO_DISPONIBLE' THEN 1 WHEN 'AGOTADO' THEN 2 ELSE 3 END, " +
                   "b.comida.nombreComida ASC",
           countQuery = "SELECT COUNT(b) FROM Basico b")
    Page<Basico> findAllPaginado(Pageable pageable);

    @Query(value = "SELECT b FROM Basico b WHERE b.estatus = :estatus ORDER BY b.destacado DESC, b.comida.nombreComida ASC",
           countQuery = "SELECT COUNT(b) FROM Basico b WHERE b.estatus = :estatus")
    Page<Basico> findDisponiblesPaginado(@Param("estatus") DBConstants.Estatus estatus, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Basico b " +
           "JOIN FETCH b.comida c " +
           "LEFT JOIN FETCH b.complementos bc " +
           "LEFT JOIN FETCH bc.complemento " +
           "WHERE LOWER(c.nombreComida) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "AND b.estatus = :estatus " +
           "ORDER BY b.destacado DESC, c.nombreComida ASC")
    List<Basico> buscarDisponiblesPorNombreComida(@Param("termino") String termino,
                                                  @Param("estatus") DBConstants.Estatus estatus);
}
