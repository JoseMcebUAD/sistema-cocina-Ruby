package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.OfertaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio de {@link OfertaPedido}.
 * Capa: Repository — acceso a datos.
 */
public interface OfertaPedidoRepository extends JpaRepository<OfertaPedido, Integer> {

    /** Carga las aplicaciones de oferta de un pedido junto con sus líneas. */
    @Query("SELECT DISTINCT op FROM OfertaPedido op LEFT JOIN FETCH op.lineas WHERE op.pedido.idPedido = :idPedido")
    List<OfertaPedido> findByPedidoIdConLineas(int idPedido);
}
