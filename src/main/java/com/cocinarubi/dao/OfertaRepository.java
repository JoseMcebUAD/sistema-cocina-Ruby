package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de {@link Oferta}.
 * Capa: Repository — acceso a datos.
 */
public interface OfertaRepository extends JpaRepository<Oferta, Integer> {

    /**
     * Carga las ofertas activas y vigentes junto con sus líneas para evitar N+1 en el matcher.
     * Fechas nulas = sin restricción por ese extremo. {@code hoy} se calcula en la app con
     * zona América/Mérida para evitar el desfase UTC del servidor MySQL.
     */
    @Query("SELECT DISTINCT o FROM Oferta o LEFT JOIN FETCH o.lineas " +
           "WHERE o.activo = true " +
           "  AND (o.fechaInicio IS NULL OR o.fechaInicio <= :hoy) " +
           "  AND (o.fechaFin    IS NULL OR o.fechaFin    >= :hoy) " +
           "ORDER BY o.prioridad DESC, o.idOferta ASC")
    List<Oferta> findActivasConLineas(@Param("hoy") LocalDate hoy);

    /** Verifica si existe al menos una {@code OfertaPedido} que referencie esta oferta. */
    @Query("SELECT COUNT(op) > 0 FROM OfertaPedido op WHERE op.oferta.idOferta = :idOferta")
    boolean existeEnPedidos(int idOferta);

    @Query("SELECT DISTINCT o FROM Oferta o LEFT JOIN FETCH o.lineas WHERE o.idOferta = :id")
    Optional<Oferta> findByIdConLineas(int id);

    boolean existsByPrioridad(int prioridad);

    boolean existsByPrioridadAndIdOfertaNot(int prioridad, int idOferta);
}
