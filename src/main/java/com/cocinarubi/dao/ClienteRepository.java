package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @Override
    @Query("SELECT c FROM Cliente c ORDER BY c.idCliente ASC")
    List<Cliente> findAll();

    boolean existsBySessionTokenHash(String sessionTokenHash);

    boolean existsByUuidCliente(String uuidCliente);

    Optional<Cliente> findBySessionTokenHash(String sessionTokenHash);

    Optional<Cliente> findByUuidCliente(String uuidCliente);

    /**
     * Pares (uuidCliente, nombre) de varios clientes en una sola consulta.
     *
     * <p>Existe para que {@code PedidoMapper} resuelva el nombre de los pedidos WEB sin disparar
     * un {@code findByUuidCliente} por pedido: al mapear una página de pedidos eso sería un N+1.
     * Devuelve proyección en vez de entidades porque solo se necesita el nombre.
     *
     * <p>El nombre puede ser NULL (la columna lo permite): el consumidor decide el fallback.
     */
    @Query("SELECT c.uuidCliente, c.nombre FROM Cliente c WHERE c.uuidCliente IN :uuids")
    List<Object[]> findNombresPorUuids(@Param("uuids") Collection<String> uuids);
}
