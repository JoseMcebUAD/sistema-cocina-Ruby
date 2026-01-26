package com.Model.Orden;

import com.Model.ModeloOrden;

/**
 * Representa una orden de tipo MESA.
 * Hereda los campos comunes de ModeloOrden y agrega campos específicos:
 * - idRelMesa: ID de la mesa (FK a tabla mesa)
 * - numeroMesa: Número o identificador de la mesa (calculado como "Mesa X")
 */
public class ModeloOrdenMesa extends ModeloOrden {

    private Integer idRelMesa;
    private String numeroMesa;

    public Integer getIdRelMesa() {
        return idRelMesa;
    }

    public void setIdRelMesa(Integer idRelMesa) {
        this.idRelMesa = idRelMesa;
    }

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
