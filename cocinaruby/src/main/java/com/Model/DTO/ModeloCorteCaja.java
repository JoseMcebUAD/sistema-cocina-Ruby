package com.Model.DTO;

import java.util.Date;
import java.util.List;

import com.Model.ModeloOrden;
/**
 * DTO para el formulario de corte de caja
 * Contiene la informacion de todos los pedidos que se hicieron en el dia, así como la cantidad de dinero que se generó
 * 
 */
public class ModeloCorteCaja {
    private double corte;
    private Date desde;
    private Date hasta;
    private List<ModeloOrden> ordenes;

     public double getCorte() {
        return corte;
    }

    public void setCorte(double corte) {
        this.corte = corte;
    }

    public Date getDesde() {
        return desde;
    }

    public void setDesde(Date desde) {
        this.desde = desde;
    }

    public Date getHasta() {
        return hasta;
    }

    public void setHasta(Date hasta) {
        this.hasta = hasta;
    }

    public List<ModeloOrden> getOrdenes() {
        return ordenes;
    }

    public void setOrdenes(List<ModeloOrden> ordenes) {
        this.ordenes = ordenes;
    }

}
