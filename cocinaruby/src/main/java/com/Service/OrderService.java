package com.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.DAO.Daos.DetalleOrdenDAO;
import com.DAO.Daos.OrdenDAO;
import com.DAO.Daos.TipoPagoDAO;
import com.DAO.Daos.Orden.OrdenMostradorDAO;
import com.DAO.Daos.Orden.OrdenMesaDAO;
import com.DAO.Daos.Orden.OrdenDomicilioDAO;
import com.DAO.Daos.DTOS.Views.OrdenViewDAO;
import com.Model.ModeloDetalleOrden;
import com.Model.ModeloOrden;
import com.Model.ModeloTipoPago;
import com.Model.DTO.ModeloOrdenCompleta;
import com.Model.DTO.VIEW.ModeloVentasView;
import com.Model.Enum.TiposClienteEnum;
import com.Model.Orden.ModeloOrdenDomicilio;
import com.Model.Orden.ModeloOrdenMesa;
import com.Model.Orden.ModeloOrdenMostrador;

public class OrderService {

    private OrdenDAO orderDaoInstance = new OrdenDAO();
    private TipoPagoDAO tipoPagoDAO = new TipoPagoDAO();
    private final DetalleOrdenDAO orderDetailDao = new DetalleOrdenDAO();
    private final OrdenViewDAO ordenViewDAO = new OrdenViewDAO();

    public boolean addFullOrder(ModeloOrdenCompleta fullOrder) {
        try {
            ModeloOrden orderHeader = fullOrder.getOrden();
            orderHeader.setPrecioOrden(0.0);

            orderDaoInstance.create(orderHeader);
            int orderId = orderHeader.getIdOrden();

            if (orderId == 0) {
                System.err.println("Error: no se obtuvo id_orden luego de insertar orden base");
                return false;
            }
            String customerType = orderHeader.getTipoCliente();

            if (TiposClienteEnum.PAGO_MOSTRADOR.equals(customerType)) {
                OrdenMostradorDAO counterOrderDao = new OrdenMostradorDAO();
                counterOrderDao.create((ModeloOrdenMostrador) orderHeader);

            } else if (TiposClienteEnum.PAGO_MESA.equals(customerType)) {
                OrdenMesaDAO tableOrderDao = new OrdenMesaDAO();
                tableOrderDao.create((ModeloOrdenMesa) orderHeader);

            } else if (TiposClienteEnum.PAGO_DOMICILIO.equals(customerType)) {
                OrdenDomicilioDAO deliveryOrderDao = new OrdenDomicilioDAO();
                deliveryOrderDao.create((ModeloOrdenDomicilio) orderHeader);
                
            } else {
                System.err.println("Tipo de cliente no soportado: " + customerType);
                return false;
            }

            for (ModeloDetalleOrden itemDetail : fullOrder.getDetalles()) {
                itemDetail.setIdRelOrden(orderId);
                orderDetailDao.create(itemDetail);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene una orden completa con sus detalles.
     *
     * @param orderId ID de la orden
     * @return ModeloVentasView con orden y detalles parseados
     */
    public ModeloVentasView getOrderWithDetails(int orderId) {
        try {
            return ordenViewDAO.find(orderId);
        } catch (SQLException e) {
            logError("Error getting order with details for ID: " + orderId, e);
            return null;
        }
    }

    /**
     * Obtiene todas las órdenes del día actual.
     *
     * @return Lista de órdenes de hoy sin filtros
     */
    public List<ModeloVentasView> getTodayOrders() {
        return getTodayOrdersFiltered(null, 0, null);
    }

    /**
     * Obtiene órdenes del día con filtros opcionales.
     *
     * @param customerType Tipo: "DOMICILIO", "MESA", "MOSTRADOR" (null = todos)
     * @param paymentTypeId ID del tipo de pago (0 = todos)
     * @param customerName Nombre del cliente (null = todos)
     * @return Lista filtrada de órdenes
     */
    public List<ModeloVentasView> getTodayOrdersFiltered(String customerType, int paymentTypeId, String customerName) {
        try {
            return ordenViewDAO.findByCorteDelDia(customerType, paymentTypeId, customerName);
        } catch (SQLException e) {
            logError("Error al obtener órdenes del día", e);
            return List.of();
        }
    }

    /**
     * Obtiene órdenes en un rango de fechas con filtros.
     *
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @param customerType Tipo de cliente (null = todos)
     * @param paymentTypeId ID tipo de pago (0 = todos)
     * @param customerName Nombre cliente (null = todos)
     * @return Lista de órdenes en el rango
     */
    public List<ModeloVentasView> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, String customerType, int paymentTypeId, String customerName) {
        if (startDate == null || endDate == null) return List.of();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            return ordenViewDAO.findByFiltroFechas(startDateTime, endDateTime, customerType, paymentTypeId, customerName);
        } catch (SQLException e) {
            logError("Error getting orders by date range", e);
            return List.of();
        }
    }

    public double calculateDailyTotalSales() {
        try {
            return ordenViewDAO.calcularTotalVentasDelDia();
        } catch (SQLException e) {
            logError("Error calculando total del día", e);
            return 0.0;
        }
    }

    public double calculateSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0.0;
        try {
            LocalDateTime from = startDate.atStartOfDay();
            LocalDateTime until = endDate.atTime(LocalTime.MAX);
            return ordenViewDAO.calcularTotalVentas(from, until);
        } catch (SQLException e) {
            logError("Error calculando total por fechas", e);
            return 0.0;
        }
    }
    /**
     * Elimina una orden por su ID.
     * Nota: Asegúrate de que tu BD tenga ON DELETE CASCADE en los detalles
     * o maneja el borrado de detalles aquí primero.
     */
    public boolean deleteOrder(int idOrder) {
        try {
            // Verify if it exists first (optional, but good practice)
            if (orderDaoInstance.read(idOrder) == null) {
                return false;
            }
            orderDaoInstance.delete(idOrder);
            return true;
        } catch (SQLException e) {
            logError("Error deleting order ID: " + idOrder, e);
            return false;
        }
    }

    public boolean updateOrderHeader(ModeloOrden updatedOrder) {
        try {
            return orderDaoInstance.update(updatedOrder.getIdOrden(), updatedOrder);
        } catch (SQLException e) {
            logError("Error al actualizar orden ID: " + updatedOrder.getIdOrden(), e);
            return false;
        }
    }

    /**
     * Actualiza los detalles de una orden (productos individuales)
     * @param idOrder ID de la orden
     * @param details Lista de detalles actualizados
     * @return true si todos los detalles se actualizaron correctamente
     */
    public boolean updateOrderDetails(int idOrder, List<ModeloDetalleOrden> details) {
        try {
            for (ModeloDetalleOrden detail : details) {
                if (!orderDetailDao.update(detail.getIdDetalleOrden(), detail)) {
                    logError("Fallo al actualizar detalle: " + detail.getIdDetalleOrden(), new Exception("Update returned false"));
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            logError("Error al actualizar detalles de orden ID: " + idOrder, e);
            return false;
        }
    }
    
    /**
     * Obtiene los detalles de una orden específica.
     * @param orderId ID de la orden
     * @return Lista de detalles de la orden
     */
    public List<ModeloDetalleOrden> getOrderDetails(int orderId) {
        try {
            List<ModeloDetalleOrden> detalles = orderDetailDao.findByOrden(orderId);
            return detalles != null ? detalles : new ArrayList<>();
        } catch (SQLException e) {
            logError("Error al obtener detalles de orden ID: " + orderId, e);
            return new ArrayList<>();
        }
    }
    
    // Utility
    private void logError(String msg, Exception e) {
        System.err.println("[OrderService] " + msg + ": " + e.getMessage());
        e.printStackTrace();
    }


    /**
     * Obtiene la lista de tipos de pago disponibles.
     * Maneja internamente la SQLException para no ensuciar el controlador.
     */
    public List<ModeloTipoPago> getPaymentTypes() {
        try {
            return tipoPagoDAO.all();
        } catch (SQLException e) {
            System.err.println("Error obteniendo el tipo de pago: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
}
