package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.Model.ModeloRetiroCaja;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RetiroCajaDAO extends BaseDAO {

    public RetiroCajaDAO() {
        super();
    }

    public ModeloRetiroCaja create(ModeloRetiroCaja model) throws SQLException {
        String sql = "INSERT INTO retiro_caja (idRel_apertura, monto_retirado, razon_retiro, fecha_retiro) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, model.getIdRelApertura());
            ps.setDouble(2, model.getMontoRetirado());
            ps.setString(3, model.getRazonRetiro());
            ps.setTimestamp(4, Timestamp.valueOf(model.getFechaRetiro()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdRetiro(keys.getInt(1));
                }
            }
        }
        return model;
    }

    /**
     * Obtiene todos los retiros de una apertura para calcular el saldo dinámico.
     */
    public List<ModeloRetiroCaja> findRetirosPorApertura(int idApertura) throws SQLException {
        String sql = "SELECT * FROM retiro_caja WHERE idRel_apertura = ? ORDER BY fecha_retiro ASC";
        List<ModeloRetiroCaja> lista = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idApertura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Suma el total retirado para una apertura.
     */
    public double sumRetirosPorApertura(int idApertura) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto_retirado), 0) FROM retiro_caja WHERE idRel_apertura = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idApertura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public List<ModeloRetiroCaja> findRetirosPorMesAnio(int mes, int anio) throws SQLException {
        String sql = "SELECT * FROM retiro_caja WHERE MONTH(fecha_retiro) = ? AND YEAR(fecha_retiro) = ? ORDER BY fecha_retiro ASC";
        List<ModeloRetiroCaja> lista = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mes);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public List<ModeloRetiroCaja> findRetirosPorAnio(int anio) throws SQLException {
        String sql = "SELECT * FROM retiro_caja WHERE YEAR(fecha_retiro) = ? ORDER BY fecha_retiro ASC";
        List<ModeloRetiroCaja> lista = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public List<Integer> getAniosDisponibles() throws SQLException {
        String sql = "SELECT DISTINCT YEAR(fecha_retiro) AS anio FROM retiro_caja ORDER BY anio DESC";
        List<Integer> anios = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) anios.add(rs.getInt("anio"));
        }
        return anios;
    }

    private ModeloRetiroCaja mapRow(ResultSet rs) throws SQLException {
        ModeloRetiroCaja model = new ModeloRetiroCaja();
        model.setIdRetiro(rs.getInt("id_retiro"));
        model.setIdRelApertura(rs.getInt("idRel_apertura"));
        model.setMontoRetirado(rs.getDouble("monto_retirado"));
        model.setRazonRetiro(rs.getString("razon_retiro"));
        Timestamp ts = rs.getTimestamp("fecha_retiro");
        if (ts != null) model.setFechaRetiro(ts.toLocalDateTime());
        return model;
    }
}
