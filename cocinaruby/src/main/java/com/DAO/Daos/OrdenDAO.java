package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.ModeloOrden;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdenDAO extends BaseDAO implements ICrud<ModeloOrden> {

    public OrdenDAO() {
        super();
    }

    public OrdenDAO(com.Config.CConexion conector) {
        super(conector);
    }

    @Override
    public ModeloOrden create(ModeloOrden model) throws SQLException {
        String sql = "INSERT INTO orden (id_rel_cliente, id_rel_tipo_pago, fecha_expedicion_orden, notas_orden, precio_orden, facturado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (model.getIdRelCliente() != null) {
                ps.setInt(1, model.getIdRelCliente());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, model.getIdRelTipoPago());
            ps.setTimestamp(3, new Timestamp(model.getFechaExpedicionOrden().getTime()));
            ps.setString(4, model.getNotasOrden());
            ps.setDouble(5, model.getPrecioOrden());
            ps.setBoolean(6, model.getFacturado());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdOrden(keys.getInt(1));
                }
            }
        }
        return model;
    }

    @Override
    public ModeloOrden find(int id) throws SQLException {
        String sql = "SELECT * FROM orden WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<ModeloOrden> all() throws SQLException {
        List<ModeloOrden> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM orden";

        try(Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql)){

                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        return ordenes;
    }

    @Override
    public boolean update(int id, ModeloOrden model) throws SQLException {
        String sql = "UPDATE orden SET id_rel_cliente = ?, id_rel_tipo_pago = ?, fecha_expedicion_orden = ?, notas_orden = ?, precio_orden = ?, facturado = ? WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (model.getIdRelCliente() != null) {
                ps.setInt(1, model.getIdRelCliente());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, model.getIdRelTipoPago());
            ps.setTimestamp(3, new Timestamp(model.getFechaExpedicionOrden().getTime()));
            ps.setString(4, model.getNotasOrden());
            ps.setDouble(5, model.getPrecioOrden());
            ps.setBoolean(6, model.getFacturado());
            ps.setInt(7, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM orden WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<ModeloOrden> findByCliente(int clienteId) throws SQLException {
        List<ModeloOrden> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM orden WHERE id_rel_cliente = ? ORDER BY fecha_expedicion_orden DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }

    public List<ModeloOrden> findByFecha(Date fecha) throws SQLException {
        List<ModeloOrden> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM orden WHERE DATE(fecha_expedicion_orden) = ? ORDER BY fecha_expedicion_orden DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(fecha.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }

    public List<ModeloOrden> findByRangoFechas(Date desde, Date hasta) throws SQLException {
        List<ModeloOrden> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM orden WHERE DATE(fecha_expedicion_orden) BETWEEN ? AND ? ORDER BY fecha_expedicion_orden DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(desde.getTime()));
            ps.setDate(2, new java.sql.Date(hasta.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }

    public List<ModeloOrden> findByTipoPago(int tipoPagoId) throws SQLException {
        List<ModeloOrden> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM orden WHERE id_rel_tipo_pago = ? ORDER BY fecha_expedicion_orden DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tipoPagoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }
    /**
     * Actualiza la orden cuando se factura
     */
    public boolean updateFacturado() throws SQLException{
        String sql = "UPDATE orden SET facturado = 1";
        try(Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
                return ps.executeUpdate() > 0;
            }
    }
    private ModeloOrden mapRow(ResultSet rs) throws SQLException {
        ModeloOrden orden = new ModeloOrden();
        orden.setIdOrden(rs.getInt("id_orden"));

        int clienteId = rs.getInt("id_rel_cliente");
        if (!rs.wasNull()) {
            orden.setIdRelCliente(clienteId);
        }

        orden.setIdRelTipoPago(rs.getInt("id_rel_tipo_pago"));
        orden.setFechaExpedicionOrden(rs.getTimestamp("fecha_expedicion_orden"));
        orden.setNotasOrden(rs.getString("notas_orden"));
        orden.setFacturado(rs.getBoolean("facturado"));
        orden.setPrecioOrden(rs.getDouble("precio_orden"));
        orden.setPagoCliente(rs.getDouble("pago_cliente"));
        return orden;
    }
}
