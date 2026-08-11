package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.domain.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Queries de agregación para el módulo de resumen de producción diaria.
 * Devuelve conteos y agrupaciones de items pedidos en el día, con filtros opcionales
 * de tipoPedido, pedidoCreadoDesde y ruta de reparto.
 * Capa: DAO.
 */
public interface ResumenProduccionRepository extends JpaRepository<Pedido, Integer> {

    // ── Patrón de filtro de ruta (aplicado en todos los métodos) ──────────────
    // LEFT JOIN a pedidoDomicilio y pedidoDomicilioCocina para alcanzar la ruta.
    // Cuando filtrarRuta = false el servicio pasa idRutas = List.of(-1) para
    // evitar IN () vacío; la condición (false = false) cortocircuita el OR.

    /**
     * Cuenta comidas pedidas hoy (1 fila ComidaPedido = 1 unidad, sin campo cantidad).
     */
    @Query("""
            SELECT COUNT(cp)
            FROM ComidaPedido cp
            JOIN cp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            """)
    long countComidas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );

    /**
     * Cuenta desayunos pedidos hoy (1 fila DesayunoPedido = 1 unidad).
     */
    @Query("""
            SELECT COUNT(dp)
            FROM DesayunoPedido dp
            JOIN dp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            """)
    long countDesayunos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );

    /**
     * Cuenta básicos pedidos hoy (1 fila BasicoPedido = 1 unidad).
     */
    @Query("""
            SELECT COUNT(bp)
            FROM BasicoPedido bp
            JOIN bp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            """)
    long countBasicos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );

    /**
     * Suma de productos cocina agrupados por Categoria.
     * Columnas Object[]: [0] Integer idCategoria, [1] String nombreCategoria,
     * [2] Long suma de cantidad.
     */
    @Query("""
            SELECT pc.categoria.idCategoria, pc.categoria.nombre,
                   COALESCE(SUM(pcp.cantidad), 0)
            FROM ProductoCocinaPedido pcp
            JOIN pcp.productoCocina pc
            JOIN pcp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            GROUP BY pc.categoria.idCategoria, pc.categoria.nombre
            ORDER BY pc.categoria.nombre ASC
            """)
    List<Object[]> countProductosCocinaAgrupadoPorCategoria(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );

    /**
     * Detalle de comidas pedidas hoy: nombre y cantidad total (media + entera sumadas).
     * Columnas Object[]: [0] String nombreComida, [1] Long cantidad.
     */
    @Query("""
            SELECT c.nombreComida, COUNT(cp)
            FROM ComidaPedido cp
            JOIN cp.comida c
            JOIN cp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            GROUP BY c.idComida, c.nombreComida
            ORDER BY COUNT(cp) DESC
            """)
    List<Object[]> findDetalleComidas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );

    /**
     * Detalle de desayunos pedidos hoy: nombre y cantidad total.
     * Columnas Object[]: [0] String nombreDesayuno, [1] Long cantidad.
     */
    @Query("""
            SELECT d.nombreDesayuno, COUNT(dp)
            FROM DesayunoPedido dp
            JOIN dp.desayuno d
            JOIN dp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            GROUP BY d.idDesayuno, d.nombreDesayuno
            ORDER BY COUNT(dp) DESC
            """)
    List<Object[]> findDetalleDesayunos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );

    /**
     * Detalle de básicos pedidos hoy: descripción del paquete y cantidad total.
     * Columnas Object[]: [0] String descripcion, [1] Long cantidad.
     */
    @Query("""
            SELECT b.descripcion, COUNT(bp)
            FROM BasicoPedido bp
            JOIN bp.basico b
            JOIN bp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            GROUP BY b.idBasico, b.descripcion
            ORDER BY COUNT(bp) DESC
            """)
    List<Object[]> findDetalleBasicos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );

    /**
     * Detalle de productos cocina de una categoría específica: nombre y suma de cantidad.
     * Columnas Object[]: [0] String nombreProducto, [1] Long suma de cantidad.
     */
    @Query("""
            SELECT pc.nombreProducto, COALESCE(SUM(pcp.cantidad), 0)
            FROM ProductoCocinaPedido pcp
            JOIN pcp.productoCocina pc
            JOIN pcp.pedido p
            LEFT JOIN p.pedidoDomicilio pd
            LEFT JOIN pd.ruta pr
            LEFT JOIN p.pedidoDomicilioCocina pdc
            LEFT JOIN pdc.ruta pcr
            WHERE pc.categoria.idCategoria = :idCategoria
              AND p.fechaExpedicionPedido >= :inicio
              AND p.fechaExpedicionPedido < :fin
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND (:pedidoCreadoDesde IS NULL OR p.pedidoCreadoDesde = :pedidoCreadoDesde)
              AND (:filtrarRuta = false OR pr.idRuta IN :idRutas OR pcr.idRuta IN :idRutas)
            GROUP BY pc.idProductoCocina, pc.nombreProducto
            ORDER BY COALESCE(SUM(pcp.cantidad), 0) DESC
            """)
    List<Object[]> findDetalleProductosCocinaPorCategoria(
            @Param("idCategoria") Integer idCategoria,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoPedido") TipoPedido tipoPedido,
            @Param("pedidoCreadoDesde") PedidoCreadoDesde pedidoCreadoDesde,
            @Param("filtrarRuta") boolean filtrarRuta,
            @Param("idRutas") List<Integer> idRutas
    );
}
