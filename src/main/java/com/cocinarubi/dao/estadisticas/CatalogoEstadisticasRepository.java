package com.cocinarubi.dao.estadisticas;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Queries de agregación para los endpoints /estadisticas/catalogo y /estadisticas/catalogo/productos.
 * Cada método trabaja sobre líneas de pedido (ComidaPedido, DesayunoPedido, etc.) agrupadas
 * por producto o categoría. La atribución de método de pago usa metodoPagoPrincipal del pedido.
 * Capa: DAO.
 */
public interface CatalogoEstadisticasRepository extends JpaRepository<Pedido, Integer> {

    // =========================================================================
    // Resúmenes por categoría — para /catalogo
    // Retornan Object[]: [totalVendido (Long), totalProductos (Long)]
    // =========================================================================

    /** Cuenta líneas y productos distintos de comida en el período/canal. */
    @Query("""
            SELECT COUNT(cp.idComidaPedido), COUNT(DISTINCT cp.comida.idComida)
            FROM ComidaPedido cp JOIN cp.pedido p
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            """)
    List<Object[]> findResumenComida(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Cuenta líneas y desayunos distintos vendidos en el período/canal. */
    @Query("""
            SELECT COUNT(dp.idDesayunoPedido), COUNT(DISTINCT dp.desayuno.idDesayuno)
            FROM DesayunoPedido dp JOIN dp.pedido p
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            """)
    List<Object[]> findResumenDesayuno(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Cuenta complementos vendidos (solo extras de comidas, no de básicos). */
    @Query("""
            SELECT COUNT(ccp.idComplementoComidaPedido), COUNT(DISTINCT ccp.complemento.idComplemento)
            FROM ComplementoComidaPedido ccp JOIN ccp.comidaPedido cp JOIN cp.pedido p
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            """)
    List<Object[]> findResumenComplemento(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Cuenta líneas y básicos distintos vendidos en el período/canal. */
    @Query("""
            SELECT COUNT(bp.idBasicoPedido), COUNT(DISTINCT bp.basico.idBasico)
            FROM BasicoPedido bp JOIN bp.pedido p
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            """)
    List<Object[]> findResumenBasico(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Suma unidades de paquetes y cuenta paquetes distintos vendidos. */
    @Query("""
            SELECT COALESCE(SUM(pp.cantidad), 0), COUNT(DISTINCT pp.paquete.idPaquete)
            FROM PaquetePedido pp JOIN pp.pedido p
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            """)
    List<Object[]> findResumenPaquete(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /**
     * Unidades y productos distintos de ProductoCocina agrupados por Categoría.
     * Retorna Object[]: [nombreCategoria (String), totalVendido (Long), totalProductos (Long)].
     */
    @Query("""
            SELECT cat.nombre, COALESCE(SUM(pcp.cantidad), 0), COUNT(DISTINCT pcp.productoCocina.idProductoCocina)
            FROM ProductoCocinaPedido pcp JOIN pcp.pedido p JOIN pcp.productoCocina pc JOIN pc.categoria cat
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            GROUP BY cat.idCategoria, cat.nombre
            """)
    List<Object[]> findResumenProductoCocina(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    // =========================================================================
    // Detalle de productos — para /catalogo/productos
    // Retornan List<Object[]>: [nombreProducto, totalVendido, totalVentas,
    //                           totalEfectivo, totalTransferencia, totalTarjeta]
    // La atribución de pago usa metodoPagoPrincipal (vista de rendimiento, no contable).
    // =========================================================================

    /** Ventas por comida: precio_unitario por línea, una unidad por línea. */
    @Query("""
            SELECT c.nombreComida,
                   COUNT(cp.idComidaPedido),
                   COALESCE(SUM(cp.precioUnitario), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO      THEN cp.precioUnitario ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA THEN cp.precioUnitario ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TARJETA       THEN cp.precioUnitario ELSE 0 END), 0)
            FROM ComidaPedido cp JOIN cp.pedido p JOIN cp.comida c
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            GROUP BY c.idComida, c.nombreComida
            """)
    List<Object[]> findProductosComida(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Ventas por desayuno: campo precio por línea, una unidad por línea. */
    @Query("""
            SELECT d.nombreDesayuno,
                   COUNT(dp.idDesayunoPedido),
                   COALESCE(SUM(dp.precio), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO      THEN dp.precio ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA THEN dp.precio ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TARJETA       THEN dp.precio ELSE 0 END), 0)
            FROM DesayunoPedido dp JOIN dp.pedido p JOIN dp.desayuno d
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            GROUP BY d.idDesayuno, d.nombreDesayuno
            """)
    List<Object[]> findProductosDesayuno(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Ventas por complemento (solo extras de comidas): precio_unitario por línea. */
    @Query("""
            SELECT comp.nombreComplemento,
                   COUNT(ccp.idComplementoComidaPedido),
                   COALESCE(SUM(ccp.precioUnitario), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO      THEN ccp.precioUnitario ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA THEN ccp.precioUnitario ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TARJETA       THEN ccp.precioUnitario ELSE 0 END), 0)
            FROM ComplementoComidaPedido ccp JOIN ccp.comidaPedido cp JOIN cp.pedido p JOIN ccp.complemento comp
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            GROUP BY comp.idComplemento, comp.nombreComplemento
            """)
    List<Object[]> findProductosComplemento(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Ventas por básico: nombre = "BASICO " + nombre de la comida base. */
    @Query("""
            SELECT CONCAT('BASICO ', c.nombreComida),
                   COUNT(bp.idBasicoPedido),
                   COALESCE(SUM(bp.precioUnitario), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO      THEN bp.precioUnitario ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA THEN bp.precioUnitario ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TARJETA       THEN bp.precioUnitario ELSE 0 END), 0)
            FROM BasicoPedido bp JOIN bp.pedido p JOIN bp.basico b JOIN b.comida c
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            GROUP BY b.idBasico, c.nombreComida
            """)
    List<Object[]> findProductosBasico(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /** Ventas por paquete: precio_unitario * cantidad por línea. */
    @Query("""
            SELECT paq.descripcion,
                   COALESCE(SUM(pp.cantidad), 0),
                   COALESCE(SUM(pp.precioUnitario * pp.cantidad), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO      THEN pp.precioUnitario * pp.cantidad ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA THEN pp.precioUnitario * pp.cantidad ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TARJETA       THEN pp.precioUnitario * pp.cantidad ELSE 0 END), 0)
            FROM PaquetePedido pp JOIN pp.pedido p JOIN pp.paquete paq
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
            GROUP BY paq.idPaquete, paq.descripcion
            """)
    List<Object[]> findProductosPaquete(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido
    );

    /**
     * Ventas por ProductoCocina filtradas por categoría y opcionalmente subcategoría.
     * precio_linea = precioUnitario * cantidad.
     */
    @Query("""
            SELECT pc.nombreProducto,
                   COALESCE(SUM(pcp.cantidad), 0),
                   COALESCE(SUM(pcp.precioUnitario * pcp.cantidad), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.EFECTIVO      THEN pcp.precioUnitario * pcp.cantidad ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TRANSFERENCIA THEN pcp.precioUnitario * pcp.cantidad ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN p.metodoPagoPrincipal = com.cocinarubi.DBConstants.MetodoPago.TARJETA       THEN pcp.precioUnitario * pcp.cantidad ELSE 0 END), 0)
            FROM ProductoCocinaPedido pcp JOIN pcp.pedido p JOIN pcp.productoCocina pc
            WHERE (:desde IS NULL OR p.fechaExpedicionPedido >= :desde)
              AND (:hasta IS NULL OR p.fechaExpedicionPedido <= :hasta)
              AND (:tipoPedido IS NULL OR p.tipoPedido = :tipoPedido)
              AND pc.categoria.idCategoria = :idCategoria
              AND (:idSubcategoria IS NULL OR EXISTS (
                      SELECT 1 FROM ProductoCocina pc2 JOIN pc2.subcategorias s
                      WHERE pc2.idProductoCocina = pc.idProductoCocina AND s.idSubcategoria = :idSubcategoria
                  ))
            GROUP BY pc.idProductoCocina, pc.nombreProducto
            """)
    List<Object[]> findProductosProductoCocina(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipoPedido") DBConstants.TipoPedido tipoPedido,
            @Param("idCategoria") Integer idCategoria,
            @Param("idSubcategoria") Integer idSubcategoria
    );
}
