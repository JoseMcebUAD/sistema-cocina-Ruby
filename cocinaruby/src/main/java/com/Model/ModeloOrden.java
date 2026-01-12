package com.Model;

import java.util.Date;

/**
 * Representa la entidad 'orden' de la base de datos.
 * Esta clase abstracta contiene los campos comunes a todas las órdenes.
 * Las clases especializadas (ModeloOrdenMostrador, ModeloOrdenDomicilio, ModeloOrdenMesa)
 * heredan de esta y agregan campos específicos.
 */
public abstract class ModeloOrden {
    private int idOrden;
    private int idRelTipoPago;
    private String nombreTipoPago; // Usado para JOINs, no está en tabla orden
    private String tipoCliente; // ENUM en SQL: 'MOSTRADOR','DOMICILIO','MESA'
    private Date fechaExpedicionOrden;
    private double precioOrden;
    private double pagoCliente;
    private boolean facturado;

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

    public String getNombreTipoPago() {
        return nombreTipoPago;
    }

    public void setNombreTipoPago(String nombreTipoPago) {
        this.nombreTipoPago = nombreTipoPago;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public Date getFechaExpedicionOrden() {
        return fechaExpedicionOrden;
    }

    public void setFechaExpedicionOrden(Date fechaExpedicionOrden) {
        this.fechaExpedicionOrden = fechaExpedicionOrden;
    }

    public double getPrecioOrden() {
        return precioOrden;
    }

    public void setPrecioOrden(double precioOrden) {
        this.precioOrden = precioOrden;
    }

    public double getPagoCliente() {
        return this.pagoCliente;
    }

    public void setPagoCliente(double pagoCliente) {
        this.pagoCliente = pagoCliente;
    }

    public boolean getFacturado() {
        return facturado;
    }

    public void setFacturado(boolean facturado) {
        this.facturado = facturado;
    }

    public abstract String getNombreCliente();
}
