package com.Controller.popup.order;
import com.Controller.OrderController;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import com.Model.ModeloDetalleOrden;
import com.Model.ModeloOrden;
import com.Model.DTO.ModeloOrdenCompleta;
import com.Model.Enum.TiposClienteEnum;
import com.Model.Orden.ModeloOrdenDomicilio;
import com.Model.Orden.ModeloOrdenMesa;
import com.Model.Orden.ModeloOrdenMostrador;
import com.Service.OrderService;
import com.Service.TicketServices.TicketOrderService;
import com.Service.TicketServices.TicketOrderService.PrintResultEnum;
import com.Model.DTO.ModeloRecibo;
import com.Model.DTO.VIEW.ModeloVentasView;
import com.Model.ModeloTipoPago;
import com.Model.ModeloCliente;
import com.Service.ClientService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class confirmOrderController implements Initializable {
    @FXML
    private Button backButton, confirmButton;
    @FXML
    private TextField clientNameField, orderTypeField, totalField, amountPaidField;
    @FXML
    private ComboBox<ModeloTipoPago> paymentMethodCombo;
    @FXML
    private HBox cashDetailsBox;

    private List<ModeloDetalleOrden> details;
    private double total;
    private String serviceType;
    private String clientName, clientAddress, clientPhone;
    private int clientId = 0;

    private OrderService orderService = new OrderService();
    private ClientService clientService = new ClientService();
    private TicketOrderService ticketService = new TicketOrderService();
    private OrderController parentController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpAllButtons();
        setUpPaymentConfig();
        setUpAmountValidation();
    }

    public void setParentController(OrderController parent) {
        this.parentController = parent;
    }

    public void setOrderData(List<ModeloDetalleOrden> orderDetails, double orderTotal, String orderType,  int clientId, String clientName, String clientAddress, String clientPhone) {
        this.details = orderDetails;
        this.total = orderTotal;
        this.serviceType = orderType;
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.clientPhone = clientPhone;
        this.clientId = clientId;

        if (totalField != null) {
            totalField.setText(String.format("%.2f", orderTotal));
        }
        if (orderTypeField != null) {
            orderTypeField.setText(orderType);
        }
        if (clientNameField != null) {
            clientNameField.setText(clientName.isEmpty() ? "Cliente General" : clientName);
        }
    }

    private void setUpConfirmButton() {
        confirmButton.setOnAction(e -> {
            if (paymentMethodCombo.getValue() == null) {
                showCustomAlert("WARNING", "Atención", "Seleccione un método de pago.");
                return;
            }

            ModeloOrden order = createOrderByType();

            if (order instanceof ModeloOrdenDomicilio) {
                ModeloOrdenDomicilio domOrder = (ModeloOrdenDomicilio) order;

                if (clientName != null && !clientName.isEmpty() && 
                    clientPhone != null && !clientPhone.isEmpty()) {

                    ModeloCliente tempClient = new ModeloCliente();
                    tempClient.setNombreCliente(clientName);
                    tempClient.setTelefono(clientPhone);
                    tempClient.setDirecciones(clientAddress);

                    ModeloCliente finalClient = clientService.findOrRegister(tempClient);

                    if (finalClient != null) {
                        domOrder.setIdRelCliente(finalClient.getIdCliente());
                        domOrder.setDireccionCliente(finalClient.getDirecciones()); 
                    }
                }
            }
            order.setFechaExpedicionOrden(LocalDateTime.now());
            order.setPrecioOrden(this.total);
            order.setTipoCliente(this.serviceType);
            order.setFacturado(false);

            if (!configurePayment(order)) return;

            ModeloOrdenCompleta completeOrder = new ModeloOrdenCompleta(order, this.details);
            if (orderService.addFullOrder(completeOrder)) {
                //VERIFICAR ESTA PARTE
                // Intentar imprimir el ticket
                int orderId = order.getIdOrden();
                PrintResultEnum printResult = attemptPrintTicket(orderId);
                
                // Actualizar estado de facturado según resultado de impresión
                if (printResult == PrintResultEnum.SUCCESS) {
                    order.setFacturado(true);
                    orderService.updateOrderHeader(order);
                    System.out.println("Orden marcada como facturada/impresa.");
                } else {
                    order.setFacturado(false);
                    orderService.updateOrderHeader(order);
                    System.out.println("Orden marcada como NO facturada/impresa.");
                }
                
                // Mostrar mensaje según resultado de impresión
                double cambio = 0.0;
                boolean esEfectivo = false;
                ModeloTipoPago metodo = paymentMethodCombo.getValue();
                if (metodo != null && "Efectivo".equalsIgnoreCase(metodo.getNombreTipoPago())) {
                    esEfectivo = true;
                    cambio = order.getPagoCliente() - this.total;
                }
                
                StringBuilder mensaje = new StringBuilder("Pedido registrado correctamente.");
                if (esEfectivo) {
                    mensaje.append("\n\nCambio a entregar: $").append(String.format("%.2f", cambio));
                }
                
                // Agregar información de impresión al mensaje
                if (printResult != PrintResultEnum.SUCCESS) {
                    mensaje.append("\n\n AVISO DE IMPRESIÓN: ").append(printResult.getMensaje());
                }
                
                showCustomAlert("SUCCESS", "Venta Exitosa", mensaje.toString());
                
                if (parentController != null) parentController.clearOrder();
                ((Stage) confirmButton.getScene().getWindow()).close();
            } else {
                showCustomAlert("ERROR", "Error de Base de Datos", "No se pudo guardar la venta. Verifique la conexión.");
            }
        });
    }
    
    /**
     * Intenta imprimir el ticket de la orden.
     * Maneja todos los errores posibles de impresora.
     * 
     * @param orderId ID de la orden a imprimir
     * @return PrintResultEnum indicando el resultado de la impresión
     */
    private PrintResultEnum attemptPrintTicket(int orderId) {
        try {
            // Obtener orden usando OrderService
            ModeloVentasView ventasView = orderService.getOrderWithDetails(orderId);
            
            if (ventasView == null) {
                System.err.println("Error: No se pudo obtener la orden ID: " + orderId);
                return PrintResultEnum.PRINT_ERROR;
            }
            
            // Convertir ModeloVentasView a ModeloOrdenCompleta
            ModeloOrden ordenBase = createOrderFromSale(ventasView);
            
            // Obtener detalles a través del servicio
            List<ModeloDetalleOrden> detalles = orderService.getOrderDetails(orderId);
            
            // Crear el modelo de recibo
            ModeloRecibo recibo = new ModeloRecibo();
            recibo.setOrden(new ModeloOrdenCompleta(ordenBase, detalles));
            recibo.setFechaExpedicion(ordenBase.getFechaExpedicionOrden());
            
            // Intentar imprimir
            PrintResultEnum resultado = ticketService.printOrderTicket(recibo);
            
            // Log detallado del resultado
            switch(resultado) {
                case SUCCESS:
                    System.out.println("✓ Ticket impreso exitosamente para orden ID: " + orderId);
                    break;
                case PRINTER_NOT_AVAILABLE:
                    System.err.println("✗ Impresora no disponible - Orden ID: " + orderId);
                    break;
                case NO_PAPER:
                    System.err.println("✗ Sin papel en la impresora - Orden ID: " + orderId);
                    break;
                case PRINT_ERROR:
                    System.err.println("✗ Error al imprimir - Orden ID: " + orderId);
                    break;
            }
            
            return resultado;
        } catch (Exception ex) {
            System.err.println("Excepción al intentar imprimir: " + ex.getMessage());
            ex.printStackTrace();
            return PrintResultEnum.PRINT_ERROR;
        }
    }
    
    /**
     * Crea un objeto ModeloOrden a partir de un ModeloVentasView
     */
    private ModeloOrden createOrderFromSale(com.Model.DTO.VIEW.ModeloVentasView venta) {
        ModeloOrdenMostrador orden = new ModeloOrdenMostrador();
        orden.setIdOrden(venta.getIdOrden());
        orden.setIdRelTipoPago(venta.getIdRelTipoPago());
        orden.setNombreTipoPago(venta.getNombreTipoPago());
        orden.setTipoCliente(venta.getTipoCliente());
        
        // Asignar fecha directamente ya que es LocalDateTime
        if (venta.getFechaExpedicionOrden() != null) {
            orden.setFechaExpedicionOrden(venta.getFechaExpedicionOrden());
        } else {
            orden.setFechaExpedicionOrden(LocalDateTime.now());
        }
        
        orden.setPrecioOrden(venta.getPrecioOrden());
        orden.setPagoCliente(venta.getPagoCliente());
        orden.setFacturado(venta.isFacturado());
        return orden;
    }


    private ModeloOrden createOrderByType() {
        if (TiposClienteEnum.PAGO_DOMICILIO.equals(this.serviceType)) {
            ModeloOrdenDomicilio deliveryOrder = new ModeloOrdenDomicilio();
            deliveryOrder.setNombreCliente(clientName);
            deliveryOrder.setDireccionCliente(clientAddress);
            deliveryOrder.setTelefonoCliente(clientPhone);
            if (this.clientId > 0) {
                deliveryOrder.setIdRelCliente(this.clientId);
            }
            return deliveryOrder;
        } else if (TiposClienteEnum.PAGO_MESA.equals(this.serviceType)) {
            ModeloOrdenMesa tableOrder = new ModeloOrdenMesa();
            // Si clientName viene como "Mesa X", extraer solo la X
            String numeroMesa = clientName;
            if (clientName.startsWith("Mesa ")) {
                numeroMesa = clientName.substring(5); // Quitar "Mesa "
            }
            tableOrder.setNumeroMesa(numeroMesa.isEmpty() ? "General" : numeroMesa);
            return tableOrder;
        } else {
            ModeloOrdenMostrador dineinOrder = new ModeloOrdenMostrador();
            dineinOrder.setNombreCliente("Cliente Mostrador");
            return dineinOrder;
        }
    }

    private void setUpPaymentConfig() {
        List<ModeloTipoPago> tiposPago = orderService.getPaymentTypes();
        paymentMethodCombo.getItems().setAll(tiposPago);
        paymentMethodCombo.setPromptText("Seleccionar...");
        paymentMethodCombo.setCellFactory(lv -> createCustomCell());
        paymentMethodCombo.setButtonCell(createCustomCell());
        paymentMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            boolean isCash = "Efectivo".equalsIgnoreCase(newVal.getNombreTipoPago());
            cashDetailsBox.setVisible(isCash);
            cashDetailsBox.setManaged(isCash);
            if (!isCash) amountPaidField.clear();
        });
    }

    private ListCell<ModeloTipoPago> createCustomCell() {
        return new ListCell<ModeloTipoPago>() {
            @Override
            protected void updateItem(ModeloTipoPago item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getNombreTipoPago());
                    try {
                        String path = "/com/images/" + item.getNombreTipoPago().toLowerCase() + ".png";
                        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(path)));
                        icon.setFitHeight(20);
                        icon.setFitWidth(20);
                        setGraphic(icon);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        };
    }

    /**
     * Valida que el campo de pago en efectivo solo acepte números y un punto decimal.
     */
    private void setUpAmountValidation() {
        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                amountPaidField.setText(oldValue);
            }
        });
    }
    
    private void setUpAllButtons() {
        setUpBackButton();
        setUpConfirmButton();
    }

    private void setUpBackButton() {
        backButton.setOnAction(e -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        });
    }


    /**
     * Configura el pago en la orden según el método seleccionado.
     * - Si es Efectivo, valida que el monto cubra el total.
     * - Si es Tarjeta/Transferencia, asume pago completo.
     * Retorna true si la validación fue exitosa.
     */
    private boolean configurePayment(ModeloOrden order) {
        ModeloTipoPago chosenPaymentOption = paymentMethodCombo.getValue();
        
        if (chosenPaymentOption == null) {
            showCustomAlert("WARNING", "Atención", "Seleccione un método de pago.");
            return false;
        }

        order.setIdRelTipoPago(chosenPaymentOption.getIdTipoPago());
        order.setNombreTipoPago(chosenPaymentOption.getNombreTipoPago()); 

        double precio = this.total;
        
        if ("Efectivo".equalsIgnoreCase(chosenPaymentOption.getNombreTipoPago())) {
            String amountPaidText = amountPaidField.getText();
            
            if (amountPaidText == null || amountPaidText.isBlank()) {
                showCustomAlert("WARNING", "Atención", "Ingrese el monto pagado en efectivo.");
                return false;
            }
            
            try {
                double pagado = Double.parseDouble(amountPaidText);

                if (pagado < precio) {
                    showCustomAlert("WARNING", "Pago Insuficiente", 
                        "El monto recibido ($" + pagado + ") es menor al total ($" + String.format("%.2f", precio) + ").");
                    return false;
                }
                order.setPagoCliente(pagado);
                order.setFacturado(true); 
            } catch (NumberFormatException ex) {
                showCustomAlert("WARNING", "Formato Inválido", "Por favor ingrese solo números.");
                return false;
            }
        } else {
            order.setPagoCliente(precio);
            order.setFacturado(true);
        }
        
        return true;
    } 
    /**
     * Muestra una alerta personalizada con iconos gráficos.
     * @param type Tipo de alerta: "SUCCESS", "WARNING", "ERROR"
     * @param title Título de la ventana
     * @param content Mensaje principal
     */
    private void showCustomAlert(String type, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        String iconName = "";
        switch (type.toUpperCase()) {
            case "SUCCESS":
                alert.setAlertType(Alert.AlertType.INFORMATION);
                iconName = "Check.png";
                break;
            case "WARNING":
                alert.setAlertType(Alert.AlertType.WARNING);
                iconName = "Advertencia.png";
                break;
            case "ERROR":
                alert.setAlertType(Alert.AlertType.ERROR);
                iconName = "Error.png"; 
                break;
            default:
                iconName = "";
                break;
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (!iconName.isEmpty()) {
            try {
                String path = "/com/images/" + iconName;
                Image image = new Image(getClass().getResourceAsStream(path));
                ImageView icon = new ImageView(image);
                icon.setFitHeight(48);
                icon.setFitWidth(48);
                alert.setGraphic(icon);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono: " + iconName);
            }
        }
        alert.showAndWait();
    }


}