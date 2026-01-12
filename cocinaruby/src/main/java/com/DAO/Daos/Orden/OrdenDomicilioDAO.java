package com.DAO.Daos.Orden;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.Orden.ModeloOrdenDomicilio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para manejar órdenes de tipo DOMICILIO.
 * Gestiona inserciones/actualizaciones en las tablas 'orden' y 'orden_domicilio'.
 */
public class OrdenDomicilioDAO extends BaseDAO implements ICrud<ModeloOrdenDomicilio> {

    public OrdenDomicilioDAO() {
        super();
    }

    public OrdenDomicilioDAO(com.Config.CConexion conector) {
        super(conector);
    }

    @Override
    public ModeloOrdenDomicilio create(ModeloOrdenDomicilio model) throws SQLException {
        String sqlDomicilio = "INSERT INTO orden_domicilio (id_orden, idRel_cliente, direccion) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDomicilio)) {

            ps.setInt(1, model.getIdOrden());

            // idRel_cliente puede ser null
            if (model.getIdRelCliente() != null) {
                ps.setInt(2, model.getIdRelCliente());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setString(3, model.getDireccion());
            ps.executeUpdate();

            return model;
        }
    }

    @Override
    public ModeloOrdenDomicilio find(int id) throws SQLException {
        String sql = """
            SELECT o.*, od.idRel_cliente, od.direccion
            FROM orden o
            INNER JOIN orden_domicilio od ON o.id_orden = od.id_orden
            WHERE o.id_orden = ?
        """;

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
    public List<ModeloOrdenDomicilio> all() throws SQLException {
        List<ModeloOrdenDomicilio> ordenes = new ArrayList<>();
        String sql = """
            SELECT o.*, od.idRel_cliente, od.direccion
            FROM orden o
            INNER JOIN orden_domicilio od ON o.id_orden = od.id_orden
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ordenes.add(mapRow(rs));
            }
        }
        return ordenes;
    }

    @Override
    public boolean update(int id, ModeloOrdenDomicilio model) throws SQLException {
        try {
            
            String sqlDomicilio = "UPDATE orden_domicilio SET idRel_cliente = ?, direccion = ? WHERE id_orden = ?";
            try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sqlDomicilio)) {
                
                if (model.getIdRelCliente() != null) {
                    ps.setInt(1, model.getIdRelCliente());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                ps.setString(2, model.getDireccion());
                ps.setInt(3, id);
                ps.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
        throw e;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        // El CASCADE en la foreign key se encarga de eliminar orden_domicilio automáticamente
        String sql = "DELETE FROM orden WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene todas las órdenes de domicilio del día actual
     */
    public List<ModeloOrdenDomicilio> findToday() throws SQLException {
        List<ModeloOrdenDomicilio> ordenes = new ArrayList<>();
        String sql = """
            SELECT o.*, od.idRel_cliente, od.direccion
            FROM orden o
            INNER JOIN orden_domicilio od ON o.id_orden = od.id_orden
            WHERE DATE(o.fecha_expedicion_orden) = CURDATE()
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ordenes.add(mapRow(rs));
            }
        }
        return ordenes;
    }

    /**
     * Busca órdenes de domicilio por cliente
     */
    public List<ModeloOrdenDomicilio> findByCliente(int clienteId) throws SQLException {
        List<ModeloOrdenDomicilio> ordenes = new ArrayList<>();
        String sql = """
            SELECT o.*, od.idRel_cliente, od.direccion
            FROM orden o
            INNER JOIN orden_domicilio od ON o.id_orden = od.id_orden
            WHERE od.idRel_cliente = ?
            ORDER BY o.fecha_expedicion_orden DESC
        """;

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

    /**
     * Busca órdenes de domicilio por dirección
     */
    public List<ModeloOrdenDomicilio> findByDireccion(String direccion) throws SQLException {
        List<ModeloOrdenDomicilio> ordenes = new ArrayList<>();
        String sql = """
            SELECT o.*, od.idRel_cliente, od.direccion
            FROM orden o
            INNER JOIN orden_domicilio od ON o.id_orden = od.id_orden
            WHERE od.direccion LIKE ?
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + direccion + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }

    /**
     * Mapea un ResultSet a ModeloOrdenDomicilio
     */
    private ModeloOrdenDomicilio mapRow(ResultSet rs) throws SQLException {
        ModeloOrdenDomicilio orden = new ModeloOrdenDomicilio();

        // Campos de la tabla 'orden'
        orden.setIdOrden(rs.getInt("id_orden"));
        orden.setIdRelTipoPago(rs.getInt("idRel_tipo_pago"));
        orden.setTipoCliente(rs.getString("tipo_cliente"));
        orden.setFechaExpedicionOrden(rs.getTimestamp("fecha_expedicion_orden"));
        orden.setPrecioOrden(rs.getDouble("precio_orden"));
        orden.setPagoCliente(rs.getDouble("pago_cliente"));
        orden.setFacturado(rs.getBoolean("facturado"));

        // Campos de la tabla 'orden_domicilio'
        int clienteId = rs.getInt("idRel_cliente");
        if (!rs.wasNull()) {
            orden.setIdRelCliente(clienteId);
        }
        orden.setDireccion(rs.getString("direccion"));

        return orden;
    }
}
