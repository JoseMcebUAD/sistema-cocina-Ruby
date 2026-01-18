package com.Service;
    /**
     * Este service funciona con la ventana de ventas
     */

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.DAO.Daos.DTOS.Views.OrdenViewDAO;
import com.Model.DTO.VIEW.ModeloVentasView;

public class VentasService {

    private OrdenViewDAO ventasDAO = new OrdenViewDAO();

       // ==================== CONSULTAS (Usan VIEW) ====================

    /**
     * Obtiene una orden completa con sus detalles.
     * Usa OrdenViewDAO que accede a view_ventas.
     *
     * @param idOrden ID de la orden
     * @return Modelo completo con orden + detalles en JSON
     */
    public ModeloVentasView obtenerOrden(int idOrden) {
        try {
            return ventasDAO.find(idOrden);
        } catch (SQLException e) {
            System.err.println("Error al obtener orden: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene todas las órdenes del día actual.
     *
     * @return Lista de órdenes de hoy
     */
    public List<ModeloVentasView> obtenerOrdenesDelDia() {
        try {
            // null, 0, null = sin filtros, obtener todas las órdenes del día
            return ventasDAO.findByCorteDelDia(null, 0, null);
        } catch (SQLException e) {
            System.err.println("Error al obtener órdenes del día: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obtiene órdenes del día con filtros opcionales.
     *
     * @param tipoCliente Tipo: "Domicilio", "Mesa", "Mostrador" (null = todos)
     * @param idTipoPago ID del tipo de pago (0 = todos)
     * @param nombreCliente Nombre del cliente (null = todos)
     * @return Lista filtrada de órdenes
     */
    public List<ModeloVentasView> obtenerOrdenesFiltradas(String tipoCliente, int idTipoPago, String nombreCliente) {
        try {
            return ventasDAO.findByCorteDelDia(tipoCliente, idTipoPago, nombreCliente);
        } catch (SQLException e) {
            System.err.println("Error al obtener órdenes filtradas: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obtiene órdenes en un rango de fechas con filtros.
     *
     * @param desde Fecha inicio
     * @param hasta Fecha fin
     * @param tipoCliente Tipo de cliente (null = todos)
     * @param idTipoPago ID tipo de pago (0 = todos)
     * @param nombreCliente Nombre cliente (null = todos)
     * @return Lista de órdenes en el rango
     */
    public List<ModeloVentasView> obtenerOrdenesPorFechas(LocalDateTime desde, LocalDateTime hasta, String tipoCliente, int idTipoPago, String nombreCliente) {
        try {
            return ventasDAO.findByFiltroFechas(desde, hasta, tipoCliente, idTipoPago, nombreCliente);
        } catch (SQLException e) {
            System.err.println("Error al obtener órdenes por fechas: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }


}
