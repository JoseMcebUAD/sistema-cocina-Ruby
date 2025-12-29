package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.IRead;
import com.Model.ModeloTipoCliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoClienteDAO extends BaseDAO implements IRead<ModeloTipoCliente> {

    @Override
    public List<ModeloTipoCliente> all() throws SQLException {
        List<ModeloTipoCliente> tipos = new ArrayList<>();
        String sql = "SELECT * FROM tipo_cliente";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tipos.add(mapRow(rs));
            }
        }
        return tipos;
    }

    @Override
    public ModeloTipoCliente find(int id) throws SQLException {
        ModeloTipoCliente tipoCliente = new ModeloTipoCliente();
        String sql = "SELECT * FROM tipo_cliente where id_tipo_cliente = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);){
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
        return tipoCliente;
    }

    private ModeloTipoCliente mapRow(ResultSet rs) throws SQLException {
        ModeloTipoCliente tipo = new ModeloTipoCliente();
        tipo.setIdTipoCliente(rs.getInt("id_tipo_cliente"));
        tipo.setNombreTipoCliente(rs.getString("nombre_tipo_cliente"));
        return tipo;
    }

}
