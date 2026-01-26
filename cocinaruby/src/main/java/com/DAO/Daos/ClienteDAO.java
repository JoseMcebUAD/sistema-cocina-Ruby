package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.ModeloCliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO extends BaseDAO  implements ICrud<ModeloCliente> {


    @Override
    public ModeloCliente create(ModeloCliente model) throws SQLException {
        String sql = "INSERT INTO cliente (nombre_cliente, direcciones, telefono, tarifa_domicilio) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, model.getNombreCliente());
            ps.setString(2, model.getDirecciones());
            ps.setString(3, model.getTelefono());
            ps.setDouble(4, model.getTarifaDomicilio());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdCliente(keys.getInt(1));
                }
            }
        }
        return model;
    }

    @Override
    public ModeloCliente read(int id) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";

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
    public List<ModeloCliente> all() throws SQLException {
        List<ModeloCliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapRow(rs));
            }
        }
        return clientes;
    }

    @Override
    public boolean update(int id, ModeloCliente model) throws SQLException {
        String sql = "UPDATE cliente SET nombre_cliente = ?, direcciones = ?, telefono = ?, tarifa_domicilio = ? WHERE id_cliente = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, model.getNombreCliente());
            ps.setString(2, model.getDirecciones());
            ps.setString(3, model.getTelefono());
            ps.setDouble(4, model.getTarifaDomicilio());
            ps.setInt(5, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }


    private ModeloCliente mapRow(ResultSet rs) throws SQLException {
        ModeloCliente cliente = new ModeloCliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombreCliente(rs.getString("nombre_cliente"));
        cliente.setDirecciones(rs.getString("direcciones"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setTarifaDomicilio(rs.getDouble("tarifa_domicilio"));
        return cliente;
    }
}
