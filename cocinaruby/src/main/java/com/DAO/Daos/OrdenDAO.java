package com.DAO.Daos;

import com.DAO.BaseDAO;
import com.DAO.Interfaces.ICreate;
import com.DAO.Interfaces.IDelete;
import com.DAO.Interfaces.IRead;
import com.DAO.Interfaces.IUpdate;
import com.Model.ModeloOrden;
import com.Model.Orden.ModeloOrdenMostrador;
import com.Model.Orden.ModeloOrdenMesa;
import com.Model.Orden.ModeloOrdenDomicilio;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenDAO extends BaseDAO implements IDelete,IUpdate<ModeloOrden>,ICreate<ModeloOrden>, IRead<ModeloOrden> {

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
    public boolean updateFacturado(int idOrden) throws SQLException{
        String sql = "UPDATE orden SET facturado = 1 WHERE id_orden = ?" ;
        try(Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, idOrden);
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

    @Override
    public ModeloOrden read(int id) throws SQLException {
        String sql = "SELECT * FROM orden WHERE id_orden = ?";
        
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
    public List<ModeloOrden> all() throws SQLException {
        String sql = "SELECT * FROM orden";
        List<ModeloOrden> lista = new ArrayList<>();
        
        try (Connection conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)) {
            
            while(rs.next()){
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    // Método auxiliar para no repetir código de mapeo
    private ModeloOrden mapRow(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo_cliente");
        ModeloOrden orden;

        if (tipo != null && tipo.equalsIgnoreCase("MOSTRADOR")) {
            orden = new ModeloOrdenMostrador();
        } else if (tipo != null && tipo.equalsIgnoreCase("MESA")) {
            orden = new ModeloOrdenMesa();
        } else {
            orden = new ModeloOrdenDomicilio();
        }

        orden.setIdOrden(rs.getInt("id_orden"));
        orden.setIdRelTipoPago(rs.getInt("idRel_tipo_pago"));
        orden.setTipoCliente(tipo);

        Timestamp ts = rs.getTimestamp("fecha_expedicion_orden");
        if (ts != null) orden.setFechaExpedicionOrden(ts.toLocalDateTime());

        orden.setPrecioOrden(rs.getDouble("precio_orden"));
        orden.setPagoCliente(rs.getDouble("pago_cliente"));
        orden.setFacturado(rs.getBoolean("facturado"));

        return orden;
    }
    
}
