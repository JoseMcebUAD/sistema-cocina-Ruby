package com.Controller.order;

import com.Controller.OrderController;
import com.Model.ModeloMesa;
import com.Model.ModeloOrden;
import com.Model.Enum.OrderUIConstants;
import com.Model.Enum.TiposClienteEnum;
import com.Model.Orden.ModeloOrdenDomicilio;
import com.Model.Orden.ModeloOrdenMesa;
import com.Model.Orden.ModeloOrdenMostrador;

/**
 * Clase para manejar mejor con interfaces el cambio de tipos de orden
 */
public class OrdersSelected {
    protected OrderController orderController;

    public OrdersSelected(OrderController orderController){
        this.orderController = orderController;
    }

    /**
     * Orden de Domicilio
     */
    public static class OrderDomicilioSelected extends OrdersSelected implements OrderSelectedInterface{

        private String ClientName;
        
        private int clientId;
        private String direccion;
        private String telefono;
        private double tarifaDomicilio;
        
        
        public String getClientName() {
            return ClientName;
        }

        public void setClientName(String clientName) {
            ClientName = clientName;
        }
        public int getClientId() {
            return clientId;
        }

        public void setClientId(int clientId) {
            this.clientId = clientId;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public double getTarifaDomicilio() {
            return tarifaDomicilio;
        }

        public void setTarifaDomicilio(double tarifaDomicilio) {
            this.tarifaDomicilio = tarifaDomicilio;
        }

        public OrderDomicilioSelected(OrderController orderController) {
            super(orderController);
        }

        @Override
        public double calculateOrderTotal() {
            double productosTotal = orderController.orderItems.stream()
                .mapToDouble(item -> item.getPrecioDetalleOrden() * item.getCantidad())
                .sum();

            // Sumar tarifa de domicilio si existe
            try {
                double tarifa = Double.parseDouble(orderController.tarifaDomicilioField.getText());
                return productosTotal + tarifa;
            } catch (NumberFormatException e) {
                return productosTotal;
            }
        }

        @Override
        public String currentTypeOrder() {
            return TiposClienteEnum.PAGO_DOMICILIO;
        }

        @Override
        public void showOrderBar(boolean show) {
            orderController.clientBar.setVisible(true);
            orderController.clientBar.setManaged(true);
            orderController.TableBar.setVisible(false);
            orderController.TableBar.setManaged(false);
            orderController.DeliveryBar.setVisible(false);
            orderController.DeliveryBar.setManaged(false);
        }

        @Override
        public void barChanged() {
            // Limpiar campos de domicilio
            orderController.clientNameField.clear();
            orderController.addressField.clear();
            orderController.phoneNumberField.clear();
            orderController.tarifaDomicilioField.clear();
            orderController.currentClientId = 0;
        }

        @Override
        public MSGValidacion validateOrderRequirements() {
            if (orderController.orderItems.isEmpty()) {
                return new MSGValidacion(false,OrderUIConstants.MSG_EMPTY_ORDER.getValue(), OrderUIConstants.MSG_NO_PRODUCTS.getValue());
            }
            if (orderController.clientNameField.getText().trim().isEmpty() || 
                orderController.addressField.getText().trim().isEmpty() || 
                orderController.phoneNumberField.getText().trim().isEmpty()) {
                return new MSGValidacion(false,OrderUIConstants.MSG_INCOMPLETE_DATA.getValue(), OrderUIConstants.MSG_DELIVERY_REQUIRED.getValue());
           
            }

            return new MSGValidacion(true,"","");
        }

        @Override
        public ModeloOrden createOrden() {
            ModeloOrdenDomicilio orden = new ModeloOrdenDomicilio();
            orden.setNombreCliente(this.ClientName);
            orden.setDireccionCliente(this.direccion);
            orden.setTelefonoCliente(this.telefono);
            orden.setTarifaDomicilio(this.tarifaDomicilio);
            if (this.clientId > 0) {
                orden.setIdRelCliente(this.clientId);
            }
            return orden;
        }

        @Override
        public void setOrderAtributes() {
            this.setClientName(orderController.clientNameField.getText().trim());
            this.setClientId(orderController.currentClientId);
            this.setDireccion(orderController.addressField.getText().trim());
            this.setTelefono(orderController.phoneNumberField.getText().trim());

            try {
                String tarifaText = orderController.tarifaDomicilioField.getText();
                double tarifa = (tarifaText == null || tarifaText.trim().isEmpty()) ? 0.0 : Double.parseDouble(tarifaText);
                this.setTarifaDomicilio(tarifa);
            } catch (NumberFormatException e) {
                this.setTarifaDomicilio(0.0);
            }
        }

        @Override
        public String getConfirmOrderName() {
            return ClientName;
        }
    }

    /**
     * Orden de Mostrador (Dine-in)
     */
    public static class OrderDineinSelected extends OrdersSelected implements OrderSelectedInterface{

        private String dineinName;

        public String getDineinName() {
            return dineinName;
        }

        public void setDineinName(String dineinName) {
            this.dineinName = dineinName;
        }

        public OrderDineinSelected(OrderController orderController) {
            super(orderController);
        }

        @Override
        public double calculateOrderTotal() {
            return orderController.orderItems.stream()
                .mapToDouble(item -> item.getPrecioDetalleOrden() * item.getCantidad())
                .sum();
        }

        @Override
        public String currentTypeOrder() {
            return TiposClienteEnum.PAGO_MOSTRADOR;
        }

        @Override
        public void showOrderBar(boolean show) {
            orderController.clientBar.setVisible(false);
            orderController.clientBar.setManaged(false);
            orderController.TableBar.setVisible(false);
            orderController.TableBar.setManaged(false);
            orderController.DeliveryBar.setVisible(true);
            orderController.DeliveryBar.setManaged(true);
        }

        @Override
        public void barChanged() {
            // Limpiar campo de mostrador
            orderController.dineinNameField.clear();
        }

        @Override
        public MSGValidacion validateOrderRequirements() {
            if (orderController.orderItems.isEmpty()) {
                return new MSGValidacion(false,OrderUIConstants.MSG_EMPTY_ORDER.getValue(), OrderUIConstants.MSG_NO_PRODUCTS.getValue());
            }
            if(orderController.dineinNameField.getText().trim().isEmpty()){
                return new MSGValidacion(false,OrderUIConstants.MSG_EMPTY_ORDER.getValue(), OrderUIConstants.MSG_DINEINNAME_REQUIRED.getValue());
                
            }
            return new MSGValidacion(true,"", "");
        }

        @Override
        public ModeloOrden createOrden() {
            ModeloOrdenMostrador orden = new ModeloOrdenMostrador();
            orden.setNombrePersona(this.dineinName);
            return orden;
        }

        @Override
        public void setOrderAtributes() {
            this.setDineinName(orderController.dineinNameField.getText().trim());
        }

        @Override
        public String getConfirmOrderName() {
            return dineinName;
        }
    }

    /**
     * Orden de Mesa (Table)
     */
    public static class OrderTableSelected extends OrdersSelected implements OrderSelectedInterface{

        private String tableNumber;

        public String getTableNumber() {
            return tableNumber;
        }

        public void setTableNumber(String tableNumber) {
            this.tableNumber = tableNumber;
        }

        public OrderTableSelected(OrderController orderController) {
            super(orderController);
        }

        @Override
        public double calculateOrderTotal() {
            return orderController.orderItems.stream()
                .mapToDouble(item -> item.getPrecioDetalleOrden() * item.getCantidad())
                .sum();
        }

        @Override
        public String currentTypeOrder() {
            return TiposClienteEnum.PAGO_MESA;
        }

        @Override
        public void showOrderBar(boolean show) {
            orderController.clientBar.setVisible(false);
            orderController.clientBar.setManaged(false);
            orderController.TableBar.setVisible(true);
            orderController.TableBar.setManaged(true);
            orderController.DeliveryBar.setVisible(false);
            orderController.DeliveryBar.setManaged(false);
        }

        @Override
        public void barChanged() {
            // Limpiar campo de mesa
            orderController.tableComboBox.setValue(null);

            // Cargar mesas disponibles
            if (orderController.mesaService != null) {
                orderController.cargarMesasDisponibles();
            }
        }
        //valida si hay productos y si hay una mesa seleccionada
        @Override
        public MSGValidacion validateOrderRequirements() {
            if (orderController.orderItems.isEmpty()) {
                return new MSGValidacion(false,OrderUIConstants.MSG_EMPTY_ORDER.getValue(), OrderUIConstants.MSG_NO_PRODUCTS.getValue());
            } if(orderController.tableComboBox.getValue() == null){
                
                return new MSGValidacion(false,OrderUIConstants.MSG_EMPTY_ORDER.getValue(), OrderUIConstants.MSG_TABLE_REQUIRED.getValue());
            }
            return new MSGValidacion(true,"", "");
        }

        @Override
        public ModeloOrden createOrden() {
            ModeloOrdenMesa orden = new ModeloOrdenMesa();
            orden.setNumeroMesa(this.tableNumber);
            // Obtener el ID de la mesa seleccionada para el nuevo campo idRel_mesa
            ModeloMesa mesaSeleccionada = orderController.tableComboBox.getValue();
            if (mesaSeleccionada != null) {
                orden.setIdRelMesa(mesaSeleccionada.getIdMesa());
            }
            return orden;
        }

        @Override
        public void setOrderAtributes() {
            ModeloMesa mesaSeleccionada = orderController.tableComboBox.getValue();
            if (mesaSeleccionada != null) {
                this.setTableNumber(mesaSeleccionada.getNumeroMesaDisplay());
            }
        }

        @Override
        public String getConfirmOrderName() {
            return "Mesa: " + tableNumber;
        }
    }
}

