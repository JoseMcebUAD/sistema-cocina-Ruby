package com.Model.Orden;

import com.Model.ModeloOrden;

/**
 * Representa una orden de tipo MOSTRADOR.
 * Hereda los campos comunes de ModeloOrden y agrega campos específicos:
 * - nombre: Nombre del cliente para la orden de mostrador
 */
public class ModeloOrdenMostrador extends ModeloOrden {

    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String getNombreCliente() {
        return "Mostrador";
    }
}
