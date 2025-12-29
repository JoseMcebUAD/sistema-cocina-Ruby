package com.DAO.Daos.DTOS;

import com.DAO.BaseDAO;
import com.DAO.Daos.DetalleOrdenDAO;
import com.DAO.Daos.OrdenDAO;
import com.Model.DTO.ModeloOrdenCompleta;
import com.Model.ModeloDetalleOrden;
import com.Model.ModeloOrden;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para manejar operaciones con órdenes completas (orden + detalles).
 * Proporciona métodos de conveniencia para trabajar con órdenes
 * y sus items de forma integrada.
 */
public class OrdenCompletaDAO extends BaseDAO {

    private final OrdenDAO ordenDAO;
    private final DetalleOrdenDAO detalleDAO;

    public OrdenCompletaDAO() {
        super();
        this.ordenDAO = new OrdenDAO();
        this.detalleDAO = new DetalleOrdenDAO();
    }

    public OrdenCompletaDAO(com.Config.CConexion conector) {
        super(conector);
        this.ordenDAO = new OrdenDAO(conector);
        this.detalleDAO = new DetalleOrdenDAO(conector);
    }

    /**
     * Obtiene una orden completa con todos sus detalles.
     *
     * @param idOrden ID de la orden
     * @return OrdenCompletaDTO con la orden y sus detalles
     * @throws SQLException si hay error en la consulta
     */
    public ModeloOrdenCompleta obtenerOrdenCompleta(int idOrden) throws SQLException {
        ModeloOrden orden = ordenDAO.find(idOrden);
        if (orden == null) {
            return null;
        }

        List<ModeloDetalleOrden> detalles = detalleDAO.findByOrden(idOrden);
        return new ModeloOrdenCompleta(orden, detalles);
    }

    /**
     * Obtiene todas las órdenes completas del día actual.
     *
     * @return Lista de órdenes completas del día
     * @throws SQLException si hay error en la consulta
     */
    public List<ModeloOrdenCompleta> obtenerOrdenesCompletasDelDia() throws SQLException {
        String sql = """
            SELECT DISTINCT o.id_orden
            FROM orden o
            WHERE DATE(o.fecha_expedicion_orden) = CURDATE()
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        List<ModeloOrdenCompleta> ordenesCompletas = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int idOrden = rs.getInt("id_orden");
                ModeloOrdenCompleta ordenCompleta = obtenerOrdenCompleta(idOrden);
                if (ordenCompleta != null) {
                    ordenesCompletas.add(ordenCompleta);
                }
            }
        }

        return ordenesCompletas;
    }

    /**
     * Obtiene órdenes completas en un rango de fechas.
     *
     * @param desde Fecha de inicio
     * @param hasta Fecha de fin
     * @return Lista de órdenes completas en el rango
     * @throws SQLException si hay error en la consulta
     */
    public List<ModeloOrdenCompleta> obtenerOrdenesCompletasPorRango(Date desde, Date hasta) throws SQLException {
        String sql = """
            SELECT DISTINCT o.id_orden
            FROM orden o
            WHERE DATE(o.fecha_expedicion_orden) BETWEEN ? AND ?
            ORDER BY o.fecha_expedicion_orden DESC
        """;

        List<ModeloOrdenCompleta> ordenesCompletas = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(desde.getTime()));
            ps.setDate(2, new java.sql.Date(hasta.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idOrden = rs.getInt("id_orden");
                    ModeloOrdenCompleta ordenCompleta = obtenerOrdenCompleta(idOrden);
                    if (ordenCompleta != null) {
                        ordenesCompletas.add(ordenCompleta);
                    }
                }
            }
        }

        return ordenesCompletas;
    }

    /**
     * Crea una orden completa con sus detalles en una transacción.
     * Si hay algún error, se hace rollback de toda la operación.
     *
     * @param ordenCompleta DTO con la orden y sus detalles
     * @return La orden completa con los IDs generados
     * @throws SQLException si hay error en la transacción
     */
    public ModeloOrdenCompleta crearOrdenCompleta(ModeloOrdenCompleta ordenCompleta) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Crear la orden
            ModeloOrden orden = ordenCompleta.getOrden();
            orden.setPrecioOrden(0.0); // Se inicializa en 0, los triggers la actualizarán
            ModeloOrden ordenCreada = ordenDAO.create(orden);

            // 2. Crear cada detalle (los triggers actualizarán automáticamente el precio_orden)
            List<ModeloDetalleOrden> detallesCreados = new ArrayList<>();
            for (ModeloDetalleOrden detalle : ordenCompleta.getDetalles()) {
                detalle.setIdRelOrden(ordenCreada.getIdOrden());
                ModeloDetalleOrden detalleCreado = detalleDAO.create(detalle);
                detallesCreados.add(detalleCreado);
            }

            conn.commit();

            // 3. Retornar la orden completa creada
            return new ModeloOrdenCompleta(ordenCreada, detallesCreados);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    /**
     * Actualiza una orden completa: modifica la orden y reemplaza todos sus detalles.
     * ATENCIÓN: Esto eliminará todos los detalles anteriores y creará los nuevos.
     *
     * @param idOrden ID de la orden a actualizar
     * @param ordenCompleta Nueva información de la orden
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la transacción
     */
    public boolean actualizarOrdenCompleta(int idOrden, ModeloOrdenCompleta ordenCompleta) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Actualizar la orden (sin modificar precio_orden, se recalculará con los triggers)
            ModeloOrden orden = ordenCompleta.getOrden();
            orden.setIdOrden(idOrden);
            boolean ordenActualizada = ordenDAO.update(idOrden, orden);

            if (!ordenActualizada) {
                conn.rollback();
                return false;
            }

            // 2. Eliminar detalles antiguos
            detalleDAO.deleteByOrden(idOrden);

            // 3. Crear nuevos detalles
            for (ModeloDetalleOrden detalle : ordenCompleta.getDetalles()) {
                detalle.setIdRelOrden(idOrden);
                detalleDAO.create(detalle);
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    /**
     * Elimina una orden completa con todos sus detalles.
     * El CASCADE de la foreign key se encargará de eliminar los detalles automáticamente.
     *
     * @param idOrden ID de la orden a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error en la consulta
     */
    public boolean eliminarOrdenCompleta(int idOrden) throws SQLException {
        return ordenDAO.delete(idOrden);
    }

    /**
     * Obtiene órdenes completas de un cliente específico.
     *
     * @param idCliente ID del cliente
     * @return Lista de órdenes completas del cliente
     * @throws SQLException si hay error en la consulta
     */
    public List<ModeloOrdenCompleta> obtenerOrdenesCompletasPorCliente(int idCliente) throws SQLException {
        List<ModeloOrden> ordenes = ordenDAO.findByCliente(idCliente);
        List<ModeloOrdenCompleta> ordenesCompletas = new ArrayList<>();

        for (ModeloOrden orden : ordenes) {
            List<ModeloDetalleOrden> detalles = detalleDAO.findByOrden(orden.getIdOrden());
            ordenesCompletas.add(new ModeloOrdenCompleta(orden, detalles));
        }

        return ordenesCompletas;
    }
}
