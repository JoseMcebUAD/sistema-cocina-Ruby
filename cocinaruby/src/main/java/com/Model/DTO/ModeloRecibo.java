package com.Model.DTO;
import java.time.LocalDateTime;
/**
 * DTO para el general el formato de un recibo
 * Contiene la informacion necesaria para imprimir una recibo en la impresora termica
 * 
 */
public class ModeloRecibo {
    private LocalDateTime fechaExpedicion;
    private ModeloOrdenCompleta orden;

    public LocalDateTime getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(LocalDateTime fechaExpedicion) {
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
