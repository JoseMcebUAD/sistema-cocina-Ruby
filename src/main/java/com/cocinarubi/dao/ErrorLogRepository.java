package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para registros de error técnico.
 * Capa: DAO — acceso a la tabla error_log.
 */
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Integer> {
}
