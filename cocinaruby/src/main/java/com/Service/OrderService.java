package com.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.DAO.Daos.DetalleOrdenDAO;
import com.DAO.Daos.OrdenDAO;
import com.DAO.Daos.TipoPagoDAO;
import com.DAO.Daos.Orden.OrdenMostradorDAO;
import com.DAO.Daos.Orden.OrdenMesaDAO;
import com.DAO.Daos.Orden.OrdenDomicilioDAO;
import com.Model.ModeloDetalleOrden;
import com.Model.ModeloOrden;
import com.Model.ModeloTipoPago;
import com.Model.DTO.ModeloOrdenCompleta;
import com.Model.Enum.TiposClienteEnum;
import com.Model.Orden.ModeloOrdenDomicilio;
import com.Model.Orden.ModeloOrdenMesa;
import com.Model.Orden.ModeloOrdenMostrador;

public class OrderService {

    private OrdenDAO orderDaoInstance = new OrdenDAO();
    private TipoPagoDAO tipoPagoDAO = new TipoPagoDAO();

    public boolean addFullOrder(ModeloOrdenCompleta fullOrder) {
        try {
            ModeloOrden ordenHeader = fullOrder.getOrden();
            ordenHeader.setPrecioOrden(0.0);
            orderDaoInstance.create(ordenHeader);

            int orderId = ordenHeader.getIdOrden();
            if (orderId == 0) {
                System.err.println("Error: no se obtuvo id_orden luego de insertar orden base");
                return false;
            }
            String customerType = ordenHeader.getTipoCliente();

            if (TiposClienteEnum.PAGO_MOSTRADOR.equals(customerType)) {
                OrdenMostradorDAO counterOrderDao = new OrdenMostradorDAO();
                counterOrderDao.create((ModeloOrdenMostrador) ordenHeader);
            } else if (TiposClienteEnum.PAGO_MESA.equals(customerType)) {
                OrdenMesaDAO tableOrderDao = new OrdenMesaDAO();
                tableOrderDao.create((ModeloOrdenMesa) ordenHeader);
            } else if (TiposClienteEnum.PAGO_DOMICILIO.equals(customerType)) {
                OrdenDomicilioDAO deliveryOrderDao = new OrdenDomicilioDAO();
                deliveryOrderDao.create((ModeloOrdenDomicilio) ordenHeader);
            } else {
                System.err.println("Tipo de cliente no soportado: " + customerType);
                return false;
            }
            DetalleOrdenDAO orderDetailDao = new DetalleOrdenDAO();
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
     * Obtiene la lista de tipos de pago disponibles.
     * Maneja internamente la SQLException para no ensuciar el controlador.
     */
    public List<ModeloTipoPago> getTiposDePago() {
        try {
            return tipoPagoDAO.all();
        } catch (SQLException e) {
            System.err.println("Error al obtener tipos de pago en Service: " + e.getMessage());
            // Retornamos lista vacía para que la UI no se rompa, o podrías lanzar una RuntimeException personalizada
            return new ArrayList<>(); 
        }
    }
    
}
