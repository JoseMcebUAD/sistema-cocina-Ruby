package com.Model.DTO;

import java.sql.Date;

/**
 * DTO para el general el formato de una factura
 * Contiene la informacion necesaria para imprimir una factura en la impresora termica
 * 
 */
public class ModeloFactura {
    private Date fechaExpedicion;
    private ModeloOrdenCompleta orden;

    public Date getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(Date fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }
    
    public ModeloOrdenCompleta getOrden() {
        return orden;
    }

    public void setOrden(ModeloOrdenCompleta orden) {
        this.orden = orden;
    }

    public String getNombreCliente(){
        return this.orden.getOrden().getNombreCliente();
    }
    

}
