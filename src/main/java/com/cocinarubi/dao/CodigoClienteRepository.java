package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.CodigoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoClienteRepository extends JpaRepository<CodigoCliente, Integer> {

    Optional<CodigoCliente> findByCodigoCliente(String codigoCliente);

    boolean existsByCodigoCliente(String codigoCliente);
}
