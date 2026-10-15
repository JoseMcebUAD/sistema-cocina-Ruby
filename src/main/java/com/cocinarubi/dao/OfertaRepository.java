package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de {@link Oferta}.
 * Capa: Repository — acceso a datos.
 */
public interface OfertaRepository extends JpaRepository<Oferta, Integer> {

    /** Carga las ofertas activas junto con sus líneas para evitar N+1 en el matcher. */
    @Query("SELECT DISTINCT o FROM Oferta o LEFT JOIN FETCH o.lineas WHERE o.activo = true ORDER BY o.prioridad DESC, o.idOferta ASC")
    List<Oferta> findActivasConLineas();

    /** Verifica si existe al menos una {@code OfertaPedido} que referencie esta oferta. */
    @Query("SELECT COUNT(op) > 0 FROM OfertaPedido op WHERE op.oferta.idOferta = :idOferta")
    boolean existeEnPedidos(int idOferta);

    @Query("SELECT DISTINCT o FROM Oferta o LEFT JOIN FETCH o.lineas WHERE o.idOferta = :id")
    Optional<Oferta> findByIdConLineas(int id);
}
