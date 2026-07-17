package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.RegistroCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroClienteRepository extends JpaRepository<RegistroCliente, Integer> {

    Page<RegistroCliente> findByTelefonoContaining(String telefono, Pageable pageable);
}
