package com.Model.Orden;

import com.Model.ModeloOrden;

/**
 * Representa una orden de tipo MOSTRADOR.
 * Hereda los campos comunes de ModeloOrden y agrega campos específicos:
 * - nombrePersona: Nombre de la persona que hace la orden de mostrador
 */
public class ModeloOrdenMostrador extends ModeloOrden {

    private String nombrePersona;

    public String getNombrePersona() {
        return nombrePersona;
    }

    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }

    @Override
    public String getNombreCliente() {
        return nombrePersona;
    }
}
