package com.Model.DTO.VIEW;

import java.time.LocalDateTime;
import java.util.List;

import com.Model.ModeloDetalleOrden;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DTO que representa la vista view_ventas.
 * Contiene información completa de una orden independientemente de su tipo
 * (MOSTRADOR, DOMICILIO, MESA), unificando los datos en un solo objeto,
 * incluyendo los detalles de la orden en formato JSON.
 */
public class ModeloVentasView {
    // Campos de la tabla orden
    private int idOrden;
    private int idRelTipoPago;
    private String nombreTipoPago; // Nombre del tipo de pago
    private String tipoCliente; // MOSTRADOR, DOMICILIO, MESA
    private LocalDateTime fechaExpedicionOrden;
    private double precioOrden;
    private double pagoCliente;
    private boolean facturado; // 1 = Impreso, 0 = No Impreso

    // Campos normalizados de las tablas especializadas
    private String nombreCliente; // Nombre del cliente, "Mostrador", o "Mesa X"

    // Detalles de la orden en formato JSON
    private List<ModeloDetalleOrden> detalleOrden; // JSON array con los detalles

    // Getters y Setters

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public int getIdRelTipoPago() {
        return idRelTipoPago;
    }

    public void setIdRelTipoPago(int idRelTipoPago) {
        this.idRelTipoPago = idRelTipoPago;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public LocalDateTime getFechaExpedicionOrden() {
        return fechaExpedicionOrden;
    }

    public void setFechaExpedicionOrden(LocalDateTime fechaExpedicionOrden) {
        this.fechaExpedicionOrden = fechaExpedicionOrden;
    }

    public double getPrecioOrden() {
        return precioOrden;
    }

    public void setPrecioOrden(double precioOrden) {
        this.precioOrden = precioOrden;
    }

    public double getPagoCliente() {
        return pagoCliente;
    }

    public void setPagoCliente(double pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreTipoPago() {
        return nombreTipoPago;
    }

    public void setNombreTipoPago(String nombreTipoPago) {
        this.nombreTipoPago = nombreTipoPago;
    }

    public boolean isFacturado() {
        return facturado;
    }

    public void setFacturado(boolean facturado) {
        this.facturado = facturado;
    }

    public List<ModeloDetalleOrden> getDetalleOrden() {
        return detalleOrden;
    }

    public void setDetalleOrden(List<ModeloDetalleOrden> detalleOrden) {
        this.detalleOrden = detalleOrden;
    }

    /**
     * Calcula el cambio (vuelto) del cliente.
     *
     * @return Cambio a devolver (pago_cliente - precio_orden)
     */
    public double calcularCambio() {
        return pagoCliente - precioOrden;
    }

    /**
     * Verifica si la orden está completamente pagada.
     *
     * @return true si el pago cubre el precio total
     */
    public boolean estaPagada() {
        return pagoCliente >= precioOrden;
    }

    /**
     * Obtiene el saldo pendiente si el pago no cubre el total.
     *
     * @return Saldo pendiente (0 si ya está pagado completamente)
     */
    public double getSaldoPendiente() {
        double saldo = precioOrden - pagoCliente;
        return saldo > 0 ? saldo : 0;
    }

    /*
    parsear el string del sql como objeto del detalle
     */
    public List<ModeloDetalleOrden> obtenerJsonDetalle(String detalleJson){

        if (detalleJson == null || detalleJson.isBlank()) {
            return List.of(); 
        }

        try{
            ObjectMapper mapper = new ObjectMapper();
                List<ModeloDetalleOrden> detalles =
                    mapper.readValue(detalleJson, new TypeReference<List<ModeloDetalleOrden>>() {});
                    return detalles;

        }catch(JsonProcessingException e){
            System.err.println("No se ha podido");
            e.getStackTrace();
            return List.of();
        }
    }

}
