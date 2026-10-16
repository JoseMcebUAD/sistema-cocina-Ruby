package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.domain.entity.TarjetaTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio de {@link TarjetaTransferencia}.
 * Capa: Repository — acceso a datos.
 */
public interface TarjetaTransferenciaRepository extends JpaRepository<TarjetaTransferencia, Integer> {

    Optional<TarjetaTransferencia> findFirstByEstatus(Estatus estatus);

    long countByEstatus(Estatus estatus);

    /**
     * Pone todas las tarjetas en NO_DISPONIBLE en una sola sentencia.
     * Se usa antes de activar una tarjeta para garantizar la invariante "una sola activa".
     * clearAutomatically limpia el contexto de persistencia tras la operación masiva.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TarjetaTransferencia t SET t.estatus = :estatus")
    void updateAllEstatus(@Param("estatus") Estatus estatus);
}
