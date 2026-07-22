package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Override
    @Query("SELECT p FROM Pedido p ORDER BY p.fechaExpedicionPedido DESC")
    List<Pedido> findAll();
}
