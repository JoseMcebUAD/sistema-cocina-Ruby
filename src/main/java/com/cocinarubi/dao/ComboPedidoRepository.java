package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.ComboPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComboPedidoRepository extends JpaRepository<ComboPedido, Integer> {

    /** Bloquea con 409 el DELETE de un Combo referenciado por algún Pedido histórico. */
    boolean existsByCombo_IdCombo(Integer idCombo);
}
