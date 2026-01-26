package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.ModeloMesa;
import com.Model.Enum.EstadoMesaEnum;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para manejar la tabla 'mesa'.
 * Gestiona CRUD de mesas y cambios de estado.
 */
public class MesaDAO extends BaseDAO implements ICrud<ModeloMesa> {

    public MesaDAO() {
        super();
    }

    public MesaDAO(com.Config.CConexion conector) {
        super(conector);
    }

    @Override
    public ModeloMesa create(ModeloMesa model) throws SQLException {
        String sql = "INSERT INTO mesa (estado_mesa) VALUES (?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, model.getEstadoMesa());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdMesa(keys.getInt(1));
                }
            }
        }
        return model;
    }

    @Override
    public ModeloMesa read(int id) throws SQLException {
        String sql = "SELECT * FROM mesa WHERE id_mesa = ?";

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
    public List<ModeloMesa> all() throws SQLException {
        List<ModeloMesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM mesa ORDER BY id_mesa ASC";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                mesas.add(mapRow(rs));
            }
        }
        return mesas;
    }

    @Override
    public boolean update(int id, ModeloMesa model) throws SQLException {
        String sql = "UPDATE mesa SET estado_mesa = ? WHERE id_mesa = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, model.getEstadoMesa());
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        // Validar que no tenga órdenes asociadas
        if (tieneOrdenesAsociadas(id)) {
            throw new SQLException("No se puede eliminar la mesa porque tiene órdenes asociadas");
        }

        String sql = "DELETE FROM mesa WHERE id_mesa = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene mesas filtradas por estado.
     */
    public List<ModeloMesa> findByEstado(String estado) throws SQLException {
        List<ModeloMesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM mesa WHERE estado_mesa = ? ORDER BY id_mesa ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estado);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mesas.add(mapRow(rs));
                }
            }
        }
        return mesas;
    }

    /**
     * Obtiene solo mesas disponibles.
     */
    public List<ModeloMesa> findDisponibles() throws SQLException {
        return findByEstado(EstadoMesaEnum.DISPONIBLE);
    }

    /**
     * Cambia el estado de una mesa.
     */
    public boolean cambiarEstado(int idMesa, String nuevoEstado) throws SQLException {
        if (!EstadoMesaEnum.esValido(nuevoEstado)) {
            throw new SQLException("Estado no válido: " + nuevoEstado);
        }

        String sql = "UPDATE mesa SET estado_mesa = ? WHERE id_mesa = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMesa);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cuenta el total de mesas.
     */
    public int contarMesas() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM mesa";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Verifica si una mesa tiene órdenes asociadas.
     */
    public boolean tieneOrdenesAsociadas(int idMesa) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM orden_mesa WHERE idRel_mesa = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        }
        return false;
    }

    /**
     * Mapea un ResultSet a ModeloMesa.
     */
    private ModeloMesa mapRow(ResultSet rs) throws SQLException {
        ModeloMesa mesa = new ModeloMesa();
        mesa.setIdMesa(rs.getInt("id_mesa"));
        mesa.setEstadoMesa(rs.getString("estado_mesa"));
        return mesa;
    }
}
