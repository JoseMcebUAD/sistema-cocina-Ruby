package com.Model.Orden;

import com.Model.ModeloOrden;

/**
 * Representa una orden de tipo MESA.
 * Hereda los campos comunes de ModeloOrden y agrega campos específicos:
 * - numeroMesa: Número o identificador de la mesa
 */
public class ModeloOrdenMesa extends ModeloOrden {

    private String numeroMesa;

    public String getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(String numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    @Override
    public String getNombreCliente() {
        return "Mesa: " + numeroMesa;
    }
}
