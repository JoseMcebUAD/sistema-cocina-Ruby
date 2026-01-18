package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICreate;
import com.DAO.Interfaces.IDelete;
import com.DAO.Interfaces.IUpdate;
import com.Model.ModeloOrden;
import java.sql.*;
import java.time.LocalDateTime;

public class OrdenDAO extends BaseDAO implements IDelete,IUpdate<ModeloOrden>,ICreate<ModeloOrden> {

    public OrdenDAO() {
        super();
    }

    public OrdenDAO(com.Config.CConexion conector) {
        super(conector);
    }

    @Override
    public boolean update(int id, ModeloOrden model) throws SQLException {
        String sql = "UPDATE orden SET idRel_tipo_pago = ?, tipo_cliente = ?, fecha_expedicion_orden = ?, precio_orden = ?, pago_cliente = ?, facturado = ? WHERE id_orden = ?";

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, model.getIdRelTipoPago());
            ps.setString(2, model.getTipoCliente());
            LocalDateTime fecha = model.getFechaExpedicionOrden();
            if (fecha != null) {
                ps.setTimestamp(3, Timestamp.valueOf(fecha));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setDouble(4, model.getPrecioOrden());
            ps.setDouble(5, model.getPagoCliente());
            ps.setBoolean(6, model.getFacturado());
            ps.setInt(7, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM orden WHERE id_orden = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }


    /**
     * Actualiza la orden cuando se factura
     */
    public boolean updateFacturado() throws SQLException{
        String sql = "UPDATE orden SET facturado = 1";
        try(Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
                return ps.executeUpdate() > 0;
            }
    }

    @Override
    public ModeloOrden create(ModeloOrden model) throws SQLException {
        String sql = "INSERT INTO orden (idRel_tipo_pago, tipo_cliente, fecha_expedicion_orden, precio_orden, pago_cliente,facturado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, model.getIdRelTipoPago());
            ps.setString(2, model.getTipoCliente());
            LocalDateTime fecha = model.getFechaExpedicionOrden();
            if (fecha != null) {
                ps.setTimestamp(3, Timestamp.valueOf(fecha));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setDouble(4, model.getPrecioOrden());
            ps.setDouble(5, model.getPagoCliente());
            ps.setBoolean(6, model.getFacturado());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    model.setIdOrden(keys.getInt(1));
                }
            }
        }
        return model;
    }

}
