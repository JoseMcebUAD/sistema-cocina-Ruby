package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @Override
    @Query("SELECT c FROM Cliente c ORDER BY c.idCliente ASC")
    List<Cliente> findAll();

    boolean existsBySessionToken(String sessionToken);

    boolean existsByUuidCliente(String uuidCliente);
}
