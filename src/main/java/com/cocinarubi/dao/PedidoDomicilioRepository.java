package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.PedidoDomicilio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de entregas a domicilio (flujo WEB).
 * Capa: DAO — solo pedidos con {@code PedidoDomicilio}; los de cocina usan {@code PedidoDomicilioCocina}.
 */
public interface PedidoDomicilioRepository extends JpaRepository<PedidoDomicilio, Integer> {

    /**
     * Devuelve coordenadas agrupadas con conteo de pedidos para el mapa de calor.
     * Solo incluye registros con latitud y longitud capturadas.
     * Resultado: {@code Object[]{BigDecimal latitud, BigDecimal longitud, Long cantidad}}.
     */
    @Query("""
            SELECT pd.latitud, pd.longitud, COUNT(pd)
            FROM PedidoDomicilio pd
            JOIN pd.pedido p
            WHERE pd.latitud IS NOT NULL
              AND pd.longitud IS NOT NULL
              AND (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:precioMin IS NULL OR p.precioFinalOrden >= :precioMin)
              AND (:precioMax IS NULL OR p.precioFinalOrden <= :precioMax)
            GROUP BY pd.latitud, pd.longitud
            ORDER BY COUNT(pd) DESC
            """)
    List<Object[]> findPuntosMapaCalor(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax
    );
}
