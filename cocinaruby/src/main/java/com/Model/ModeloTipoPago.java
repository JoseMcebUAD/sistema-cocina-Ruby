package com.Model;
/**
 * Representa la entidad 'tipo_pago' de la base de datos.
 * Esta clase se utiliza para almacenar datos  
 * de un tipo de pago (transferencia,efectivo,terminal) con la funcion de mejorar el filtrado de los datos
 */
public class ModeloTipoPago {
    private int idTipoPago;
    private String nombreTipoPago;

    public int getIdTipoPago() {
        return idTipoPago;
    }

    public void setIdTipoPago(int idTipoPago) {
        this.idTipoPago = idTipoPago;
    }

    public String getNombreTipoPago() {
        return nombreTipoPago;
    }

    public void setNombreTipoPago(String nombreTipoPago) {
        this.nombreTipoPago = nombreTipoPago;
    }
}
