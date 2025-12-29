package com.DAO.Daos.DTOS;

import com.DAO.BaseDAO;
import com.Model.DTO.ModeloCorteCaja;
import com.Model.ModeloOrden;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para manejar operaciones de Corte de Caja.
 * Proporciona métodos para generar cortes de caja por:
 * - Rango de fechas
 * - Día actual
 * - Filtros por tipo de pago
 */
public class CorteCajaDAO extends BaseDAO {

    /**
     * Obtiene el corte de caja para un rango de fechas específico.
     * Incluye todas las órdenes y calcula el total.
     *
     * @param desde Fecha de inicio del rango
     * @param hasta Fecha de fin del rango
     * @throws SQLException 
     */
    public ModeloCorteCaja obtenerCortePorRango(Date desde, Date hasta) throws SQLException {
        ModeloCorteCaja corte = new ModeloCorteCaja();
        corte.setDesde(desde);
        corte.setHasta(hasta);

        String sql = """
            SELECT
                o.id_orden,
                o.id_rel_cliente,
                o.id_rel_tipo_pago,
                o.fecha_expedicion_orden,
                o.notas_orden,
                o.precio_orden,
                o.facturado,
                COALESCE(c.nombre_cliente, 'Cliente') AS nombre_cliente,
                tp.nombre_tipo_pago
            FROM orden o
            LEFT JOIN cliente c ON o.id_rel_cliente = c.id_cliente
            JOIN tipo_pago tp ON o.id_rel_tipo_pago = tp.id_tipo_pago
            WHERE DATE(o.fecha_expedicion_orden) BETWEEN ? AND ?
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        List<ModeloOrden> ordenes = new ArrayList<>();
        double total = 0.0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(desde.getTime()));
            ps.setDate(2, new java.sql.Date(hasta.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ModeloOrden orden = mapearOrdenCompleta(rs);
                    ordenes.add(orden);
                    total += orden.getPrecioOrden();
                }
            }
        }

        corte.setOrdenes(ordenes);
        corte.setCorte(total);

        return corte;
    }

    /**
     * Obtiene el corte de caja del día actual.
     * Simplifica la operación más común: revisar las ventas del día.
     *
     * @return ModeloCorteCaja con las órdenes del día y el total
     * @throws SQLException si hay error en la consulta
     */
    public ModeloCorteCaja obtenerCorteDelDia() throws SQLException {
        LocalDate hoy = LocalDate.now();
        Date fechaHoy = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return obtenerCortePorRango(fechaHoy, fechaHoy);
    }

    /**
     * Obtiene el corte de caja filtrado por tipo de pago en un rango de fechas.
     * Útil para saber cuánto se vendió en efectivo, tarjeta, transferencia, etc.
     *
     * @param desde Fecha de inicio del rango
     * @param hasta Fecha de fin del rango
     * @param idTipoPago ID del tipo de pago a filtrar
     * @return ModeloCorteCaja con las órdenes filtradas y el total
     * @throws SQLException si hay error en la consulta
     */
    public ModeloCorteCaja obtenerCortePorRangoYTipoPago(Date desde, Date hasta, int idTipoPago) throws SQLException {
        ModeloCorteCaja corte = new ModeloCorteCaja();
        corte.setDesde(desde);
        corte.setHasta(hasta);

        String sql = """
            SELECT
                o.id_orden,
                o.id_rel_cliente,
                o.id_rel_tipo_pago,
                o.fecha_expedicion_orden,
                o.notas_orden,
                o.precio_orden,
                o.facturado,
                COALESCE(c.nombre_cliente, 'Público General') AS nombre_cliente,
                tp.nombre_tipo_pago
            FROM orden o
            LEFT JOIN cliente c ON o.id_rel_cliente = c.id_cliente
            JOIN tipo_pago tp ON o.id_rel_tipo_pago = tp.id_tipo_pago
            WHERE DATE(o.fecha_expedicion_orden) BETWEEN ? AND ?
            AND o.id_rel_tipo_pago = ?
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        List<ModeloOrden> ordenes = new ArrayList<>();
        double total = 0.0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(desde.getTime()));
            ps.setDate(2, new java.sql.Date(hasta.getTime()));
            ps.setInt(3, idTipoPago);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ModeloOrden orden = mapearOrdenCompleta(rs);
                    ordenes.add(orden);
                    total += orden.getPrecioOrden();
                }
            }
        }

        corte.setOrdenes(ordenes);
        corte.setCorte(total);

        return corte;
    }

    /**
     * Obtiene el corte del día actual filtrado por tipo de pago.
     * Combina las dos operaciones más comunes: día actual + filtro de pago.
     *
     * @param idTipoPago ID del tipo de pago a filtrar
     */
    public ModeloCorteCaja obtenerCorteDelDiaPorTipoPago(int idTipoPago) throws SQLException {
        LocalDate hoy = LocalDate.now();
        Date fechaHoy = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return obtenerCortePorRangoYTipoPago(fechaHoy, fechaHoy, idTipoPago);
    }

    /**
     * Obtiene un resumen del corte del día agrupado por tipo de pago.
     * Retorna una lista de cortes, uno por cada tipo de pago utilizado.
     *
     */
    public List<ModeloCorteCaja> obtenerResumenDelDiaPorTipoPago() throws SQLException {
        LocalDate hoy = LocalDate.now();
        Date fechaHoy = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return obtenerResumenPorRangoYTipoPago(fechaHoy, fechaHoy);
    }

    /**
     * Obtiene un resumen del corte agrupado por tipo de pago en un rango de fechas.
     * Retorna una lista de cortes, uno por cada tipo de pago utilizado en el rango.
     *
     * @param desde Fecha de inicio del rango
     * @param hasta Fecha de fin del rango
     */
    public List<ModeloCorteCaja> obtenerResumenPorRangoYTipoPago(Date desde, Date hasta) throws SQLException {
        List<ModeloCorteCaja> cortes = new ArrayList<>();

        String sqlTiposPago = """
            SELECT DISTINCT tp.id_tipo_pago, tp.nombre_tipo_pago
            FROM orden o
            JOIN tipo_pago tp ON o.id_rel_tipo_pago = tp.id_tipo_pago
            WHERE DATE(o.fecha_expedicion_orden) BETWEEN ? AND ?
            ORDER BY tp.nombre_tipo_pago
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlTiposPago)) {

            ps.setDate(1, new java.sql.Date(desde.getTime()));
            ps.setDate(2, new java.sql.Date(hasta.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idTipoPago = rs.getInt("id_tipo_pago");
                    ModeloCorteCaja cortePorTipo = obtenerCortePorRangoYTipoPago(desde, hasta, idTipoPago);
                    cortes.add(cortePorTipo);
                }
            }
        }

        return cortes;
    }

    /**
     * Método auxiliar para mapear un ResultSet completo a ModeloOrden.
     * Incluye los datos de las relaciones (cliente y tipo de pago).
     *
     * @param rs ResultSet con los datos de la orden
     * @return ModeloOrden mapeada con todos los datos
     * @throws SQLException si hay error al leer el ResultSet
     */
    private ModeloOrden mapearOrdenCompleta(ResultSet rs) throws SQLException {
        ModeloOrden orden = new ModeloOrden();

        orden.setIdOrden(rs.getInt("id_orden"));

        int clienteId = rs.getInt("id_rel_cliente");
        if (!rs.wasNull()) {
            orden.setIdRelCliente(clienteId);
        }

        orden.setIdRelTipoPago(rs.getInt("id_rel_tipo_pago"));
        orden.setFechaExpedicionOrden(rs.getTimestamp("fecha_expedicion_orden"));
        orden.setNotasOrden(rs.getString("notas_orden"));
        orden.setPrecioOrden(rs.getDouble("precio_orden"));
        orden.setFacturado(rs.getBoolean("facturado"));

        // Datos adicionales de las relaciones
        orden.setNombreCliente(rs.getString("nombre_cliente"));
        orden.setNombreTipoPago(rs.getString("nombre_tipo_pago"));

        return orden;
    }
}
