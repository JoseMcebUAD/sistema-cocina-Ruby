package com.Model.Orden;

import com.Model.ModeloOrden;

/**
 * Representa una orden de tipo DOMICILIO.
 * Hereda los campos comunes de ModeloOrden y agrega campos específicos:
 * - idRelCliente: ID del cliente (puede ser null)
 * - direccion: Dirección de entrega
 */
public class ModeloOrdenDomicilio extends ModeloOrden {

    private Integer idRelCliente;
    private String direccionCliente;
    private String nombreCliente;
    private String telefonoCliente;

    public Integer getIdRelCliente() {
        return idRelCliente;
    }

    public void setIdRelCliente(Integer idRelCliente) {
        this.idRelCliente = idRelCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public void setDireccionCliente(String direccion) {
        this.direccionCliente = direccion;
    }

    public String getDireccionCliente() {
        return direccionCliente;
    }

    public String getTelefonoCliente(){
        return telefonoCliente;
    }


    @Override
    public String getNombreCliente() {
        return this.nombreCliente;
    }
}
