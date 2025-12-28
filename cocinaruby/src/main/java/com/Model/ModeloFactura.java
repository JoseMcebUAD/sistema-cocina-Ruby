package com.Model;

import java.sql.Date;
/**
 * DTO para el general el formato de una factura
 * Contiene la informacion necesaria para imprimir una factura 
 * 
 */
public class ModeloFactura {
    private Date fechaExpedicion;
    private ModeloCliente cliente;
    private ModeloOrden orden;

    public Date getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(Date fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public ModeloCliente getCliente() {
        return cliente;
    }

    public void setCliente(ModeloCliente cliente) {
        this.cliente = cliente;
    }

    public ModeloOrden getOrden() {
        return orden;
    }

    public void setOrden(ModeloOrden orden) {
        this.orden = orden;
    }

}
