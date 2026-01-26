package com.Model;

/**
 * Representa la entidad 'mesa' de la base de datos.
 * Esta clase se utiliza para almacenar y transportar los datos
 * de una mesa a través de todas las capas del sistema.
 */
public class ModeloMesa {
    private int idMesa;
    private String estadoMesa; // ENUM: DISPONIBLE, OCUPADO, SUSPENDIDO

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public String getEstadoMesa() {
        return estadoMesa;
    }

    public void setEstadoMesa(String estadoMesa) {
        this.estadoMesa = estadoMesa;
    }

    /**
     * Método helper para mostrar en UI.
     * @return "Mesa 1", "Mesa 2", etc. basado en el ID
     */
    public String getNumeroMesaDisplay() {
        return "Mesa " + idMesa;
    }
}
