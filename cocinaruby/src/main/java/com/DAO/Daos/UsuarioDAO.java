package com.DAO.Daos;

import com.Config.Constants;
import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICrud;
import com.Model.ModeloUsuario;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO  extends BaseDAO implements ICrud<ModeloUsuario> {


    @Override
    public ModeloUsuario create(ModeloUsuario model) throws SQLException {
        //Se modifico idRel_tipo_usuario y paso a ser idRel_tipo_usuario para que funcione la BD
        String sql = "INSERT INTO usuario (idRel_tipo_usuario, nombre_usuario, contrasena_usuario) VALUES (?, ?, ?)";
        String bcryptHashedString = BCrypt.withDefaults().hashToString(Constants.EXP_COS, model.getContrasenaUsuario().toCharArray());

        model.setContrasenaUsuario(bcryptHashedString);


        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, model.getIdRelTipoUsuario());
            ps.setString(2, model.getNombreUsuario());
            ps.setString(3, model.getContrasenaUsuario());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdUsuario(keys.getInt(1));
                }
            }
        }
        return model;
    }

    @Override
    public ModeloUsuario read(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

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
    public List<ModeloUsuario> all() throws SQLException {
        List<ModeloUsuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapRow(rs));
            }
        }
        return usuarios;
    }

    @Override
    public boolean update(int id, ModeloUsuario model) throws SQLException {
        String sql = "UPDATE usuario SET idRel_tipo_usuario = ?, nombre_usuario = ?, contrasena_usuario = ? WHERE id_usuario = ?";
        String bcryptHashedString = BCrypt.withDefaults().hashToString(Constants.EXP_COS, model.getContrasenaUsuario().toCharArray());
        model.setContrasenaUsuario(bcryptHashedString);


        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, model.getIdRelTipoUsuario());
            ps.setString(2, model.getNombreUsuario());
            ps.setString(3, model.getContrasenaUsuario());
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public ModeloUsuario findByNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE nombre_usuario = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ModeloUsuario autenticar(String nombreUsuario, String contrasena) throws SQLException {
        // Primero buscar el usuario por nombre
        String sql = "SELECT * FROM usuario WHERE nombre_usuario = ?";

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Obtener el hash almacenado en la base de datos
                    String hashAlmacenado = rs.getString("contrasena_usuario");

                    // Verificar la contraseña usando BCrypt.verifyer()
                    BCrypt.Result result = BCrypt.verifyer().verify(contrasena.toCharArray(), hashAlmacenado);

                    // Si la verificación es exitosa, retornar el usuario
                    if (result.verified) {
                        return mapRow(rs);
                    }
                }
            }
        }
        return null;
    }

    public List<ModeloUsuario> findByTipo(int tipoId) throws SQLException {
        List<ModeloUsuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario WHERE idRel_tipo_usuario = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tipoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapRow(rs));
                }
            }
        }
        return usuarios;
    }

    private ModeloUsuario mapRow(ResultSet rs) throws SQLException {
        ModeloUsuario usuario = new ModeloUsuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setIdRelTipoUsuario(rs.getInt("idRel_tipo_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setContrasenaUsuario(rs.getString("contrasena_usuario"));
        return usuario;
    }
}
