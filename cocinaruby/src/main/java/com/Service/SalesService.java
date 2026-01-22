package com.Service;
import java.time.LocalDate;
import java.util.List;

import com.Model.DTO.VIEW.ModeloVentasView;

public class SalesService {

    private OrderService orderService = new OrderService();

       // ==================== CONSULTAS (Usan VIEW) ====================

    /**
     * Obtiene una orden completa con sus detalles.
     * Delega en OrderService.
     *
     * @param orderId ID de la orden
     * @return Modelo completo con orden + detalles
     */
    public ModeloVentasView getOrderWithDetails(int orderId) {
        return orderService.getOrderWithDetails(orderId);
    }

    /**
     * Obtiene todas las órdenes del día actual.
     * Delega en OrderService.
     *
     * @return Lista de órdenes de hoy
     */
    public List<ModeloVentasView> getTodayOrders() {
        return orderService.getTodayOrders();
    }

    /**
     * Obtiene órdenes del día con filtros opcionales.
     * Delega en OrderService.
     *
     * @param customerType Tipo: "DOMICILIO", "MESA", "MOSTRADOR" (null = todos)
     * @param paymentTypeId ID del tipo de pago (0 = todos)
     * @param customerName Nombre del cliente (null = todos)
     * @return Lista filtrada de órdenes
     */
    public List<ModeloVentasView> getTodayOrdersFiltered(String customerType, int paymentTypeId, String customerName) {
        return orderService.getTodayOrdersFiltered(customerType, paymentTypeId, customerName);
    }

    /**
     * Obtiene órdenes en un rango de fechas con filtros.
     * Delega en OrderService.
     *
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @param customerType Tipo de cliente (null = todos)
     * @param paymentTypeId ID tipo de pago (0 = todos)
     * @param customerName Nombre cliente (null = todos)
     * @return Lista de órdenes en el rango
     */
    public List<ModeloVentasView> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, String customerType, int paymentTypeId, String customerName) {
        return orderService.getOrdersByDateRange(startDate, endDate, customerType, paymentTypeId, customerName);
    }
}
