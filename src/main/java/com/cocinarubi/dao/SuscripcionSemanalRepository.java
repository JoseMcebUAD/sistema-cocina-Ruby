package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.SuscripcionSemanal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para {@link SuscripcionSemanal}.
 * Capa: Repository — acceso a datos.
 */
public interface SuscripcionSemanalRepository extends JpaRepository<SuscripcionSemanal, Integer> {

    Page<SuscripcionSemanal> findByActivoOrderBySemanaInicioDesc(boolean activo, Pageable pageable);

    List<SuscripcionSemanal> findBySemanaInicio(LocalDate semanaInicio);

    boolean existsByCodigoRecuperacion(String codigoRecuperacion);

    Optional<SuscripcionSemanal> findByUuidCliente(String uuidCliente);
}
