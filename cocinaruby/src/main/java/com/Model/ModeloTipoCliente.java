package com.Model;

/**
 * Representa la entidad 'tipo_cliente' de la base de datos.
 * Esta clase se utiliza para almacenar y transportar los datos
 * de un tipo de cliente (mostrador,pick-up, entrega) con la funcion de mejorar el filtrado de los datos 
 */
public class ModeloTipoCliente {
    private int idTipoCliente;
    private String nombreTipoCliente;

    public int getIdTipoCliente() {
        return idTipoCliente;
    }

    public void setIdTipoCliente(int idTipoCliente) {
        this.idTipoCliente = idTipoCliente;
    }

    public String getNombreTipoCliente() {
        return nombreTipoCliente;
    }

    public void setNombreTipoCliente(String nombreTipoCliente) {
        this.nombreTipoCliente = nombreTipoCliente;
    }
}
