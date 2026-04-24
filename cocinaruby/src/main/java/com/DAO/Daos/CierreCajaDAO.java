package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.Model.ModeloCierreCaja;

import java.sql.*;
import java.time.LocalDateTime;

public class CierreCajaDAO extends BaseDAO {

    public CierreCajaDAO() {
        super();
    }

    public ModeloCierreCaja create(ModeloCierreCaja model) throws SQLException {
        String sql = """
            INSERT INTO cierre_caja
              (idRel_apertura, fecha_cierre, monto_esperado, monto_real, diferencia, observaciones)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, model.getIdRelApertura());
            ps.setTimestamp(2, Timestamp.valueOf(model.getFechaCierre()));
            ps.setDouble(3, model.getMontoEsperado());
            ps.setDouble(4, model.getMontoReal());
            ps.setDouble(5, model.getDiferencia());
            if (model.getObservaciones() != null) {
                ps.setString(6, model.getObservaciones());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdCierre(keys.getInt(1));
                }
            }
        }
        return model;
    }

    /**
     * Busca el cierre asociado a una apertura.
     */
    public ModeloCierreCaja findCierrePorApertura(int idApertura) throws SQLException {
        String sql = "SELECT * FROM cierre_caja WHERE idRel_apertura = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idApertura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private ModeloCierreCaja mapRow(ResultSet rs) throws SQLException {
        ModeloCierreCaja model = new ModeloCierreCaja();
        model.setIdCierre(rs.getInt("id_cierre"));
        model.setIdRelApertura(rs.getInt("idRel_apertura"));
        Timestamp ts = rs.getTimestamp("fecha_cierre");
        if (ts != null) model.setFechaCierre(ts.toLocalDateTime());
        model.setMontoEsperado(rs.getDouble("monto_esperado"));
        model.setMontoReal(rs.getDouble("monto_real"));
        model.setDiferencia(rs.getDouble("diferencia"));
        model.setObservaciones(rs.getString("observaciones"));
        return model;
    }
}
