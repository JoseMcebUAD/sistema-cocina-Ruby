package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.PaquetePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaquetePedidoRepository extends JpaRepository<PaquetePedido, Integer> {

    /** Bloquea con 409 el DELETE de un Paquete referenciado por algún Pedido histórico. */
    boolean existsByPaquete_IdPaquete(Integer idPaquete);
}
