package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.IRead;
import com.Model.ModeloTipoPago;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoPagoDAO extends BaseDAO implements IRead<ModeloTipoPago> {

    @Override
    public ModeloTipoPago read(int id) throws SQLException {
        String sql = "SELECT * FROM tipo_pago WHERE id_tipo_pago = ?";

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
    public List<ModeloTipoPago> all() throws SQLException {
        List<ModeloTipoPago> tipos = new ArrayList<>();
        String sql = "SELECT * FROM tipo_pago";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tipos.add(mapRow(rs));
            }
        }
        return tipos;
    }

    private ModeloTipoPago mapRow(ResultSet rs) throws SQLException {
        ModeloTipoPago tipo = new ModeloTipoPago();
        tipo.setIdTipoPago(rs.getInt("id_tipo_pago"));
        tipo.setNombreTipoPago(rs.getString("nombre_tipo_pago"));
        return tipo;
    }
}
