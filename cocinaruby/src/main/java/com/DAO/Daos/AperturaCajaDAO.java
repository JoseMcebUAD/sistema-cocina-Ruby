package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.Model.ModeloAperturaCaja;

import java.sql.*;
import java.time.LocalDateTime;

public class AperturaCajaDAO extends BaseDAO {

    public AperturaCajaDAO() {
        super();
    }

    public ModeloAperturaCaja create(ModeloAperturaCaja model) throws SQLException {
        String sql = "INSERT INTO apertura_caja (idRel_usuario, monto_inicial, fecha_apertura) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, model.getIdRelUsuario());
            ps.setDouble(2, model.getMontoInicial());
            ps.setTimestamp(3, Timestamp.valueOf(model.getFechaApertura()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdApertura(keys.getInt(1));
                }
            }
        }
        return model;
    }

    /**
     * Busca una apertura activa para hoy (sin cierre registrado).
     */
    public ModeloAperturaCaja findAperturaActivaHoy() throws SQLException {
        String sql = """
            SELECT a.* FROM apertura_caja a
            LEFT JOIN cierre_caja c ON c.idRel_apertura = a.id_apertura
            WHERE DATE(a.fecha_apertura) = CURDATE()
              AND c.id_cierre IS NULL
            ORDER BY a.fecha_apertura DESC
            LIMIT 1
            """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Busca la ultima apertura registrada hoy, tenga o no cierre.
     */
    public ModeloAperturaCaja findUltimaAperturaHoy() throws SQLException {
        String sql = """
            SELECT *
            FROM apertura_caja
            WHERE DATE(fecha_apertura) = CURDATE()
            ORDER BY fecha_apertura DESC
            LIMIT 1
            """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    private ModeloAperturaCaja mapRow(ResultSet rs) throws SQLException {
        ModeloAperturaCaja model = new ModeloAperturaCaja();
        model.setIdApertura(rs.getInt("id_apertura"));
        model.setIdRelUsuario(rs.getInt("idRel_usuario"));
        model.setMontoInicial(rs.getDouble("monto_inicial"));
        Timestamp ts = rs.getTimestamp("fecha_apertura");
        if (ts != null) model.setFechaApertura(ts.toLocalDateTime());
        return model;
    }
}
