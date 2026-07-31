package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.domain.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Override
    @Query("SELECT p FROM Pedido p ORDER BY p.fechaExpedicionPedido DESC")
    List<Pedido> findAll();

    /*SOCKETS */

    List<Pedido> findByPedidoCreadoDesdeAndImpresoFalse(PedidoCreadoDesde pedidoCreadoDesde);

    long countByPedidoCreadoDesdeAndImpresoFalseAndFechaExpedicionPedidoBetween(
            PedidoCreadoDesde pedidoCreadoDesde, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Página de pedidos filtrada por rango de fechas, tipo y origen.
     * Usada por VistaResumenPedidoService para construir el detalle completo en /metricas.
     */
    @Query("""
            SELECT p FROM Pedido p
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:creadoDesde IS NULL OR p.pedidoCreadoDesde = :creadoDesde)
              AND (:pagado IS NULL OR p.pagado = :pagado)
            ORDER BY p.fechaExpedicionPedido DESC
            """)
    Page<Pedido> findByFiltros(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido,
            @Param("creadoDesde") DBConstants.PedidoCreadoDesde creadoDesde,
            @Param("pagado") Boolean pagado,
            Pageable pageable
    );
}
