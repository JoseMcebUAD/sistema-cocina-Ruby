package com.DAO.Daos;

import com.DAO.Interfaces.IRead;
import com.Config.CConexion;
import com.Model.ModeloTipoUsuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoUsuarioDAO implements IRead<ModeloTipoUsuario> {

    private CConexion conector;

    public TipoUsuarioDAO() {
        this.conector = new CConexion();
    }

    public TipoUsuarioDAO(CConexion conector) {
        this.conector = conector;
    }

    private Connection getConnection() {
        return conector.establecerConexionDb();
    }

    @Override
    public ModeloTipoUsuario find(int id) throws SQLException {
        String sql = "SELECT * FROM tipo_usuario WHERE id_tipo_usuario = ?";

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
    public List<ModeloTipoUsuario> all() throws SQLException {
        List<ModeloTipoUsuario> tipos = new ArrayList<>();
        String sql = "SELECT * FROM tipo_usuario";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tipos.add(mapRow(rs));
            }
        }
        return tipos;
    }

 
    private ModeloTipoUsuario mapRow(ResultSet rs) throws SQLException {
        ModeloTipoUsuario tipo = new ModeloTipoUsuario();
        tipo.setIdTipoUsuario(rs.getInt("id_tipo_usuario"));
        tipo.setNombreTipoUsuario(rs.getString("nombre_tipo_usuario"));
        tipo.setPermisosUsuario(rs.getString("permisos_usuario"));
        return tipo;
    }
}
