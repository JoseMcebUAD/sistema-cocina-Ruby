package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.ModeloDetalleOrden;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para manejar operaciones CRUD de detalle_orden.
 * Los triggers de la base de datos se encargan automáticamente de actualizar
 * el precio_orden total cuando se insertan, actualizan o eliminan detalles.
 */
public class DetalleOrdenDAO extends BaseDAO implements ICrud<ModeloDetalleOrden> {

    public DetalleOrdenDAO() {
        super();
    }

    public DetalleOrdenDAO(com.Config.CConexion conector) {
        super(conector);
    }

    @Override
    public ModeloDetalleOrden create(ModeloDetalleOrden model) throws SQLException {
        String sql = "INSERT INTO detalle_orden (idRel_orden, especificaciones_detalle_orden, precio_detalle_orden) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, model.getIdRelOrden());
            ps.setString(2, model.getEspecificacionesDetalleOrden());
            ps.setDouble(3, model.getPrecioDetalleOrden());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdDetalleOrden(keys.getInt(1));
                }
            }
        }
        return model;
    }

    @Override
    public ModeloDetalleOrden find(int id) throws SQLException {
        String sql = "SELECT * FROM detalle_orden WHERE id_detalle_orden = ?";

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
    public List<ModeloDetalleOrden> all() throws SQLException {
        List<ModeloDetalleOrden> detalles = new ArrayList<>();
        String sql = "SELECT * FROM detalle_orden";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                detalles.add(mapRow(rs));
            }
        }
        return detalles;
    }

    @Override
    public boolean update(int id, ModeloDetalleOrden model) throws SQLException {
        String sql = "UPDATE detalle_orden SET idRel_orden = ?, especificaciones_detalle_orden = ?, precio_detalle_orden = ? WHERE id_detalle_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, model.getIdRelOrden());
            ps.setString(2, model.getEspecificacionesDetalleOrden());
            ps.setDouble(3, model.getPrecioDetalleOrden());
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM detalle_orden WHERE id_detalle_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene todos los detalles de una orden específica.
     * Útil para mostrar el desglose completo de items de una orden.
     *
     * @param idOrden ID de la orden
     * @return Lista de detalles de la orden
     * @throws SQLException si hay error en la consulta
     */
    public List<ModeloDetalleOrden> findByOrden(int idOrden) throws SQLException {
        List<ModeloDetalleOrden> detalles = new ArrayList<>();
        String sql = "SELECT * FROM detalle_orden WHERE idRel_orden = ? ORDER BY id_detalle_orden";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOrden);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapRow(rs));
                }
            }
        }
        return detalles;
    }

    /**
     * Elimina todos los detalles de una orden.
     * El trigger automáticamente actualizará el precio_orden a 0.
     * Útil cuando se quiere resetear una orden.
     *
     * @param idOrden ID de la orden
     * @return true si se eliminó al menos un detalle
     * @throws SQLException si hay error en la consulta
     */
    public boolean deleteByOrden(int idOrden) throws SQLException {
        String sql = "DELETE FROM detalle_orden WHERE idRel_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOrden);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Calcula el total de una orden sumando sus detalles.
     * Esta función es útil para verificar la integridad de los datos,
     * aunque normalmente el precio_orden en la tabla orden debería estar sincronizado.
     *
     * @param idOrden ID de la orden
     * @return Total calculado de la orden
     * @throws SQLException si hay error en la consulta
     */
    public double calcularTotalOrden(int idOrden) throws SQLException {
        String sql = "SELECT COALESCE(SUM(precio_detalle_orden), 0) AS total FROM detalle_orden WHERE idRel_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOrden);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Cuenta cuántos items tiene una orden.
     *
     * @param idOrden ID de la orden
     * @return Cantidad de items en la orden
     * @throws SQLException si hay error en la consulta
     */
    public int contarItemsPorOrden(int idOrden) throws SQLException {
        String sql = "SELECT COUNT(*) AS cantidad FROM detalle_orden WHERE idRel_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOrden);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cantidad");
                }
            }
        }
        return 0;
    }

    /**
     * Método auxiliar para mapear un ResultSet a ModeloDetalleOrden.
     *
     * @param rs ResultSet con los datos del detalle
     * @return ModeloDetalleOrden mapeado
     * @throws SQLException si hay error al leer el ResultSet
     */
    private ModeloDetalleOrden mapRow(ResultSet rs) throws SQLException {
        ModeloDetalleOrden detalle = new ModeloDetalleOrden();
        detalle.setIdDetalleOrden(rs.getInt("id_detalle_orden"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setIdRelOrden(rs.getInt("idRel_orden"));
        detalle.setEspecificacionesDetalleOrden(rs.getString("especificaciones_detalle_orden"));
        detalle.setPrecioDetalleOrden(rs.getDouble("precio_detalle_orden"));
        return detalle;
    }
}
