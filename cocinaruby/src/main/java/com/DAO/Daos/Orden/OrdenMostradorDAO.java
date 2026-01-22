package com.DAO.Daos.Orden;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.Orden.ModeloOrdenMostrador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para manejar órdenes de tipo MOSTRADOR.
 * Gestiona inserciones/actualizaciones en las tablas 'orden' y 'orden_mostrador'.
 */
public class OrdenMostradorDAO extends BaseDAO implements ICrud<ModeloOrdenMostrador> {

    public OrdenMostradorDAO() {
        super();
    }

    public OrdenMostradorDAO(com.Config.CConexion conector) {
        super(conector);
    }

    @Override
    public ModeloOrdenMostrador create(ModeloOrdenMostrador model) throws SQLException {
        String sqlMostrador = "INSERT INTO orden_mostrador (id_orden, nombre) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMostrador)) {

            ps.setInt(1, model.getIdOrden());
            ps.setString(2, model.getNombre());
            ps.executeUpdate();

            return model;
        }
    }

    @Override
    public ModeloOrdenMostrador read(int id) throws SQLException {
        String sql = """
            SELECT o.*, om.nombre
            FROM orden o
            INNER JOIN orden_mostrador om ON o.id_orden = om.id_orden
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
    public List<ModeloOrdenMostrador> all() throws SQLException {
        List<ModeloOrdenMostrador> ordenes = new ArrayList<>();
        String sql = """
            SELECT o.*, om.nombre
            FROM orden o
            INNER JOIN orden_mostrador om ON o.id_orden = om.id_orden
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
    public boolean update(int id, ModeloOrdenMostrador model) throws SQLException {
        String sqlMostrador = "UPDATE orden_mostrador SET nombre = ? WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMostrador)) {

            ps.setString(1, model.getNombre());
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        // El CASCADE en la foreign key se encarga de eliminar orden_mostrador automáticamente
        String sql = "DELETE FROM orden WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene todas las órdenes de mostrador del día actual
     */
    public List<ModeloOrdenMostrador> findToday() throws SQLException {
        List<ModeloOrdenMostrador> ordenes = new ArrayList<>();
        String sql = """
            SELECT o.*, om.nombre
            FROM orden o
            INNER JOIN orden_mostrador om ON o.id_orden = om.id_orden
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
     * Busca órdenes de mostrador por nombre
     */
    public List<ModeloOrdenMostrador> findByNombre(String nombre) throws SQLException {
        List<ModeloOrdenMostrador> ordenes = new ArrayList<>();
        String sql = """
            SELECT o.*, om.nombre
            FROM orden o
            INNER JOIN orden_mostrador om ON o.id_orden = om.id_orden
            WHERE om.nombre LIKE ?
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapRow(rs));
                }
            }
        }
        return ordenes;
    }

    /**
     * Mapea un ResultSet a ModeloOrdenMostrador
     */
    private ModeloOrdenMostrador mapRow(ResultSet rs) throws SQLException {
        ModeloOrdenMostrador orden = new ModeloOrdenMostrador();

        // Campos de la tabla 'orden'
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
        orden.setFacturado(rs.getBoolean("facturado"));

        // Campos de la tabla 'orden_mostrador'
        orden.setNombreCliente(rs.getString("nombre"));

        return orden;
    }

}
