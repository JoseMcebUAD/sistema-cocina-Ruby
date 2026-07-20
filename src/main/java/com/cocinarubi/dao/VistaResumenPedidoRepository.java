package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.VistaResumenPedido;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VistaResumenPedidoRepository extends JpaRepository<VistaResumenPedido, Integer> {

    @Query(value = """
            SELECT new com.cocinarubi.presentation.dto.response.VistaResumenPedidoResponseDTO(
                v.idPedido,
                v.impreso,
                v.nombreCliente,
                v.metodoPagoPrincipal,
                v.metodoPagoSecundario,
                v.tipoPedido,
                v.pedidoCreadoDesde,
                v.fechaExpedicionPedido,
                v.precioFinalOrden,
                v.pagoClientePrincipal,
                v.ruta,
                v.domicilio,
                v.precioTarifa
            )
            FROM VistaResumenPedido v
            WHERE (:desde IS NULL OR v.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR v.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR v.tipoPedido = :tipoPedido)
              AND (:creadoDesde IS NULL OR v.pedidoCreadoDesde = :creadoDesde)
            ORDER BY v.fechaExpedicionPedido DESC
            """,
            countQuery = """
            SELECT COUNT(v)
            FROM VistaResumenPedido v
            WHERE (:desde IS NULL OR v.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR v.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR v.tipoPedido = :tipoPedido)
              AND (:creadoDesde IS NULL OR v.pedidoCreadoDesde = :creadoDesde)
            """)
    Page<VistaResumenPedidoResponseDTO> findVistaConFiltros(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido,
            @Param("creadoDesde") DBConstants.PedidoCreadoDesde creadoDesde,
            Pageable pageable
    );

    @Query("""
            SELECT
                SUM(CASE WHEN v.impreso = true THEN 1 ELSE 0 END) AS cantidadImpresos,
                SUM(CASE WHEN v.impreso = false THEN 1 ELSE 0 END) AS cantidadNoImpresos,
                COALESCE(SUM(v.precioFinalOrden), 0) AS ingresoTotal,
                COALESCE(SUM(
                    CASE
                        WHEN v.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO
                            THEN CASE WHEN v.metodoPagoSecundario IS NULL THEN v.precioFinalOrden ELSE v.pagoClientePrincipal END
                        WHEN v.metodoPagoSecundario = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO
                            THEN v.precioFinalOrden - v.pagoClientePrincipal
                        ELSE 0
                    END), 0) AS ingresoEfectivo,
                COALESCE(SUM(
                    CASE
                        WHEN v.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA
                            THEN CASE WHEN v.metodoPagoSecundario IS NULL THEN v.precioFinalOrden ELSE v.pagoClientePrincipal END
                        WHEN v.metodoPagoSecundario = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA
                            THEN v.precioFinalOrden - v.pagoClientePrincipal
                        ELSE 0
                    END), 0) AS ingresoTransferencia,
                COALESCE(SUM(
                    CASE
                        WHEN v.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TARJETA
                            THEN CASE WHEN v.metodoPagoSecundario IS NULL THEN v.precioFinalOrden ELSE v.pagoClientePrincipal END
                        WHEN v.metodoPagoSecundario = com.cocinarubi.DBConstants.MetodoPago.TARJETA
                            THEN v.precioFinalOrden - v.pagoClientePrincipal
                        ELSE 0
                    END), 0) AS ingresoTarjeta
            FROM VistaResumenPedido v
            WHERE (:desde IS NULL OR v.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR v.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR v.tipoPedido = :tipoPedido)
              AND (:creadoDesde IS NULL OR v.pedidoCreadoDesde = :creadoDesde)
            """)
    VistaResumenMetricasProjection findMetricasConFiltros(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido,
            @Param("creadoDesde") DBConstants.PedidoCreadoDesde creadoDesde
    );

    interface VistaResumenMetricasProjection {
        Long getCantidadImpresos();
        Long getCantidadNoImpresos();
        BigDecimal getIngresoTotal();
        BigDecimal getIngresoEfectivo();
        BigDecimal getIngresoTransferencia();
        BigDecimal getIngresoTarjeta();
    }
}
