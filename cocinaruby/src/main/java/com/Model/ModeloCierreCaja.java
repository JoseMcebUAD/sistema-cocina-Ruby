package com.Model;

import java.time.LocalDateTime;

public class ModeloCierreCaja {
    private int idCierre;
    private int idRelApertura;
    private LocalDateTime fechaCierre;
    private double montoEsperado;
    private double montoReal;
    private double diferencia;
    private String observaciones;

    public int getIdCierre() { return idCierre; }
    public void setIdCierre(int idCierre) { this.idCierre = idCierre; }

    public int getIdRelApertura() { return idRelApertura; }
    public void setIdRelApertura(int idRelApertura) { this.idRelApertura = idRelApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public double getMontoEsperado() { return montoEsperado; }
    public void setMontoEsperado(double montoEsperado) { this.montoEsperado = montoEsperado; }

    public double getMontoReal() { return montoReal; }
    public void setMontoReal(double montoReal) { this.montoReal = montoReal; }

    public double getDiferencia() { return diferencia; }
    public void setDiferencia(double diferencia) { this.diferencia = diferencia; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
