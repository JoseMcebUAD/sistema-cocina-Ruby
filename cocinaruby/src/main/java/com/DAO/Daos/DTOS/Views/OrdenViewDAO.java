package com.DAO.Daos.DTOS.Views;

import com.DAO.BaseDAO;
import com.Model.DTO.VIEW.ModeloVentasView;

import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO para consultar la vista view_ventas.
 * Esta VIEW unifica las tres tablas de órdenes especializadas
 * (mostrador, domicilio, mesa) en una sola consulta, incluyendo
 * los detalles de cada orden en formato JSON.
 *
 * NOTA: Esta es una vista de solo lectura. Para crear/modificar órdenes,
 * use los DAOs especializados (OrdenMostradorDAO, OrdenDomicilioDAO, OrdenMesaDAO).
 */
public class OrdenViewDAO extends BaseDAO {

    public OrdenViewDAO() {
        super();
    }

    public OrdenViewDAO(com.Config.CConexion conector) {
        super(conector);
    }

    /**
     * Obtiene una orden por ID desde la vista.
     *
     * @param idOrden ID de la orden
     * @return ModeloVentasView con toda la información unificada
     * @throws SQLException si hay error en la consulta
     */
    public ModeloVentasView find(int idOrden) throws SQLException {
        String sql = "SELECT * FROM view_ventas WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOrden);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene órdenes del día actual aplicando filtros opcionales.
     *
     * @param tipoCliente Tipo de cliente: "Domicilio", "Mesa", "Mostrador" (null = ignorar filtro)
     *                    Usa TiposClienteEnum.PAGO_DOMICILIO, TiposClienteEnum.PAGO_MESA, etc.
     * @param idTipoPago ID del tipo de pago (0 = ignorar filtro)
     * @param nombreCliente Nombre del cliente (null = ignorar filtro)
     */
    public List<ModeloVentasView> findByCorteDelDia(String tipoCliente, int idTipoPago, String nombreCliente) throws SQLException {
        String sql = """
            SELECT * FROM view_ventas
            WHERE DATE(fecha_expedicion_orden) = CURDATE()
                AND (? = 0 OR idRel_tipo_pago = ?)
                AND (? IS NULL OR nombre_cliente LIKE ?)
                AND (? IS NULL OR tipo_cliente = ?)
            ORDER BY fecha_expedicion_orden DESC
        """;

        List<ModeloVentasView> ordenes = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Filtro por tipo de pago
            ps.setInt(1, idTipoPago);
            ps.setInt(2, idTipoPago);

            // Filtro por nombre de cliente
            if (nombreCliente != null && !nombreCliente.trim().isEmpty()) {
                ps.setString(3, nombreCliente);
                ps.setString(4, "%" + nombreCliente + "%");
            } else {
                ps.setString(3, null);
                ps.setString(4, null);
            }

            // Filtro por tipo de cliente
            if (tipoCliente != null && !tipoCliente.trim().isEmpty()) {
                ps.setString(5, tipoCliente);
                ps.setString(6, tipoCliente);
            } else {
                ps.setString(5, null);
                ps.setString(6, null);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }

    /**
     * Obtiene órdenes en un rango de fechas aplicando filtros opcionales.
     *
     * @param tipoCliente Tipo de cliente: "Domicilio", "Mesa", "Mostrador" (null = ignorar filtro)
     *                    Usa TiposClienteEnum.PAGO_DOMICILIO, TiposClienteEnum.PAGO_MESA, etc.
     * @param idTipoPago ID del tipo de pago (0 = ignorar filtro)
     * @param nombreCliente Nombre del cliente (null = ignorar filtro)
     */
    public List<ModeloVentasView> findByFiltroFechas(LocalDateTime desde, LocalDateTime hasta, String tipoCliente, int idTipoPago, String nombreCliente) throws SQLException {
        String sql = """
            SELECT * FROM view_ventas
            WHERE DATE(fecha_expedicion_orden) BETWEEN ? AND ?
                AND (? = 0 OR idRel_tipo_pago = ?)
                AND (? IS NULL OR nombre_cliente LIKE ?)
                AND (? IS NULL OR tipo_cliente = ?)
            ORDER BY fecha_expedicion_orden DESC
        """;

        List<ModeloVentasView> ordenes = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Filtro por rango de fechas (usar Timestamp para LocalDateTime)
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));

            // Filtro por tipo de pago
            ps.setInt(3, idTipoPago);
            ps.setInt(4, idTipoPago);

            // Filtro por nombre de cliente
            if (nombreCliente != null && !nombreCliente.trim().isEmpty()) {
                ps.setString(5, nombreCliente);
                ps.setString(6, "%" + nombreCliente + "%");
            } else {
                ps.setString(5, null);
                ps.setString(6, null);
            }

            // Filtro por tipo de cliente
            if (tipoCliente != null && !tipoCliente.trim().isEmpty()) {
                ps.setString(7, tipoCliente);
                ps.setString(8, tipoCliente);
            } else {
                ps.setString(7, null);
                ps.setString(8, null);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }

    /**
     * Calcula el total de ventas en un rango de fechas.
     *
     * @param desde Fecha de inicio
     * @param hasta Fecha de fin
     * @return Total de ventas
     * @throws SQLException si hay error en la consulta
     */
    public double calcularTotalVentas(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = "SELECT SUM(precio_orden) as total FROM view_ventas WHERE DATE(fecha_expedicion_orden) BETWEEN ? AND ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Calcula el total de ventas del día actual.
     *
     * @return Total de ventas del día
     * @throws SQLException si hay error en la consulta
     */
    public double calcularTotalVentasDelDia() throws SQLException {
        String sql = "SELECT SUM(precio_orden) as total FROM view_ventas WHERE DATE(fecha_expedicion_orden) = CURDATE()";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    /**
     * Mapea un ResultSet a ModeloVentasView.
     *
     * @param rs ResultSet con los datos de la orden
     * @return ModeloVentasView con todos los datos
     * @throws SQLException si hay error al leer el ResultSet
     */
    private ModeloVentasView mapRow(ResultSet rs) throws SQLException {
        ModeloVentasView orden = new ModeloVentasView();

        // Campos de la tabla orden
        orden.setIdOrden(rs.getInt("id_orden"));
        orden.setIdRelTipoPago(rs.getInt("idRel_tipo_pago"));
        orden.setTipoCliente(rs.getString("tipo_cliente"));
        Timestamp ts = rs.getTimestamp("fecha_expedicion_orden");
        if (ts != null) {
            orden.setFechaExpedicionOrden(ts.toLocalDateTime());
        } else {
            orden.setFechaExpedicionOrden(null);
        }
        orden.setPrecioOrden(rs.getDouble("precio_orden"));
        orden.setPagoCliente(rs.getDouble("pago_cliente"));

        // Campos normalizados
        orden.setNombreCliente(rs.getString("nombre_cliente"));

        //obtener el json como objeto de los detalles de orde
        orden.setDetalleOrden(orden.obtenerJsonDetalle(rs.getString("detalle_orden")));
        
        
        return orden;
    }
}
