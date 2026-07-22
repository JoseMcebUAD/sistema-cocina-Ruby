package com.cocinarubi.dao;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.presentation.dto.response.EstadisticaRutaItemDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Queries de agregación para el módulo de estadísticas.
 * Repositorio independiente sobre Pedido; no repite métodos de PedidoRepository.
 * Capa: DAO.
 */
public interface EstadisticasRepository extends JpaRepository<Pedido, Integer> {

    /**
     * Suma las tarifas de envío de pedidos con PedidoDomicilio (flujo WEB).
     * El JOIN a pedidoDomicilio actúa como filtro implícito de pedidos tipo DOMICILIO-WEB.
     */
    @Query("""
            SELECT COALESCE(SUM(pd.tarifa), 0)
            FROM Pedido p
            JOIN p.pedidoDomicilio pd
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            """)
    BigDecimal findSumTarifaWeb(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /**
     * Suma las tarifas de envío de pedidos con PedidoDomicilioCocina (flujo COCINA).
     * El JOIN actúa como filtro implícito de pedidos tipo DOMICILIO-COCINA.
     */
    @Query("""
            SELECT COALESCE(SUM(pdc.precioTarifa), 0)
            FROM Pedido p
            JOIN p.pedidoDomicilioCocina pdc
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            """)
    BigDecimal findSumTarifaCocina(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /**
     * Ingresos por ruta para pedidos WEB (PedidoDomicilio).
     * Cuando metodoPago es null se suma el precioFinalOrden completo de cada pedido.
     * Cuando se filtra, se aplica la lógica de pago dividido:
     *   - 1 método: precioFinalOrden completo
     *   - 2 métodos, principal coincide: pagoCliente
     *   - 2 métodos, secundario coincide: precioFinalOrden - pagoCliente
     */
    @Query("""
            SELECT new com.cocinarubi.presentation.dto.response.EstadisticaRutaItemDTO(
                r.idRuta,
                r.nombre,
                r.orden,
                COALESCE(SUM(
                    CASE
                        WHEN p.metodoPagoSecundario IS NULL THEN p.precioFinalOrden
                        WHEN p.metodoPagoPrincipal = :metodoPago THEN COALESCE(p.pagoCliente, 0)
                        WHEN p.metodoPagoSecundario = :metodoPago THEN (p.precioFinalOrden - COALESCE(p.pagoCliente, 0))
                        ELSE p.precioFinalOrden
                    END
                ), 0)
            )
            FROM Pedido p
            JOIN p.pedidoDomicilio pd
            JOIN pd.ruta r
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:metodoPago IS NULL OR p.metodoPagoPrincipal = :metodoPago OR p.metodoPagoSecundario = :metodoPago)
            GROUP BY r.idRuta, r.nombre, r.orden
            ORDER BY r.orden ASC
            """)
    List<EstadisticaRutaItemDTO> findIngresosPorRutaWeb(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("metodoPago") DBConstants.MetodoPago metodoPago
    );

    /**
     * Ingresos por ruta para pedidos COCINA (PedidoDomicilioCocina).
     * Misma lógica de pago dividido que findIngresosPorRutaWeb.
     */
    @Query("""
            SELECT new com.cocinarubi.presentation.dto.response.EstadisticaRutaItemDTO(
                r.idRuta,
                r.nombre,
                r.orden,
                COALESCE(SUM(
                    CASE
                        WHEN p.metodoPagoSecundario IS NULL THEN p.precioFinalOrden
                        WHEN p.metodoPagoPrincipal = :metodoPago THEN COALESCE(p.pagoCliente, 0)
                        WHEN p.metodoPagoSecundario = :metodoPago THEN (p.precioFinalOrden - COALESCE(p.pagoCliente, 0))
                        ELSE p.precioFinalOrden
                    END
                ), 0)
            )
            FROM Pedido p
            JOIN p.pedidoDomicilioCocina pdc
            JOIN pdc.ruta r
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:metodoPago IS NULL OR p.metodoPagoPrincipal = :metodoPago OR p.metodoPagoSecundario = :metodoPago)
            GROUP BY r.idRuta, r.nombre, r.orden
            ORDER BY r.orden ASC
            """)
    List<EstadisticaRutaItemDTO> findIngresosPorRutaCocina(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("metodoPago") DBConstants.MetodoPago metodoPago
    );

    /**
     * Cuenta pedidos e ingreso total agrupados por día de semana (DAYOFWEEK MySQL: 1=Dom, 2=Lun, …, 7=Sáb).
     * Columnas del Object[]: [0] dia_semana (Number 1-7), [1] cantidad_pedidos (Number), [2] ingreso (BigDecimal).
     */
    @Query(value = """
            SELECT DAYOFWEEK(p.fecha_expedicion_pedido) AS dia_semana,
                   COUNT(*)                              AS cantidad_pedidos,
                   SUM(p.precio_final_orden)             AS ingreso
            FROM pedido p
            WHERE (:desde IS NULL OR p.fecha_expedicion_pedido >= :desde)
              AND (:hasta IS NULL OR p.fecha_expedicion_pedido <= :hasta)
            GROUP BY dia_semana
            """, nativeQuery = true)
    List<Object[]> findVentasPorDiaSemana(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    /**
     * Cuenta pedidos agrupados por franja horaria definida por segundosRango.
     * Columnas del Object[]: [0] slot_index (Number), [1] cantidad_pedidos (Number).
     * El slot_index se convierte a hora inicio/fin en el servicio.
     */
    @Query(value = """
            SELECT FLOOR(TIME_TO_SEC(TIME(p.fecha_expedicion_pedido)) / :segundosRango) AS slot_index,
                   COUNT(*) AS cantidad_pedidos
            FROM pedido p
            WHERE (:desde IS NULL OR p.fecha_expedicion_pedido >= :desde)
              AND (:hasta IS NULL OR p.fecha_expedicion_pedido <= :hasta)
            GROUP BY slot_index
            ORDER BY slot_index
            """, nativeQuery = true)
    List<Object[]> findVentasPorHorario(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("segundosRango") int segundosRango
    );
}
