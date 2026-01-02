package com.Model;

import java.util.Date;
/**
 * Representa la entidad 'orden' de la base de datos.
 * Esta clase se utiliza para almacenar y transportar los datos
 * Contiene la informacion de una venta de la cocina
 */
public class ModeloOrden {
    private int idOrden;
    private Integer idRelCliente; // puede ser NULL
    private int idRelTipoPago;
    private String nombreCliente;
    private String nombreTipoPago;
    private Date fechaExpedicionOrden;
    private String notasOrden; //si es media
    private double precioOrden;
    private double pagoCliente;
    private boolean facturado;

    public int getIdOrden() {
        return idOrden;
    }

    public boolean getFacturado() {
        return facturado;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public Integer getIdRelCliente() {
        return idRelCliente;
    }

    public void setIdRelCliente(Integer idRelCliente) {
        this.idRelCliente = idRelCliente;
    }

    public int getIdRelTipoPago() {
        return idRelTipoPago;
    }

    public void setIdRelTipoPago(int idRelTipoPago) {
        this.idRelTipoPago = idRelTipoPago;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getNombreTipoPago() {
        return nombreTipoPago;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setNombreTipoPago(String nombreTipoPago) {
        this.nombreTipoPago = nombreTipoPago;
    }

    public Date getFechaExpedicionOrden() {
        return fechaExpedicionOrden;
    }

    public void setFechaExpedicionOrden(Date fechaExpedicionOrden) {
        this.fechaExpedicionOrden = fechaExpedicionOrden;
    }

    public String getNotasOrden() {
        return notasOrden;
    }

    public void setNotasOrden(String notasOrden) {
        this.notasOrden = notasOrden;
    }

    public double getPrecioOrden() {
        return precioOrden;
    }

    public void setPrecioOrden(double precioOrden) {
        this.precioOrden = precioOrden;
    }
    /**
     * Actualiza una orden si ya se facturó o no
     * @param facturado 
     */
    public void setFacturado(boolean facturado) {
        this.facturado = facturado;
    }

    public double getPagoCliente(){
        return this.pagoCliente;
    }

    public void setPagoCliente(double pagoCliente){
        this.pagoCliente = pagoCliente;
    }
}
