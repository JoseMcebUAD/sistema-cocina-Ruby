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
    private String direccion;
    private String nombreCliente;

    public Integer getIdRelCliente() {
        return idRelCliente;
    }

    public void setIdRelCliente(Integer idRelCliente) {
        this.idRelCliente = idRelCliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String getNombreCliente() {
        return this.nombreCliente;
    }
}
