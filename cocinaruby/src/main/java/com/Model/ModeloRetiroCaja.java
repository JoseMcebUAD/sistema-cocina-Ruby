package com.Model;

import java.time.LocalDateTime;

public class ModeloRetiroCaja {
    private int idRetiro;
    private int idRelApertura;
    private double montoRetirado;
    private String razonRetiro;
    private LocalDateTime fechaRetiro;

    public int getIdRetiro() { return idRetiro; }
    public void setIdRetiro(int idRetiro) { this.idRetiro = idRetiro; }

    public int getIdRelApertura() { return idRelApertura; }
    public void setIdRelApertura(int idRelApertura) { this.idRelApertura = idRelApertura; }

    public double getMontoRetirado() { return montoRetirado; }
    public void setMontoRetirado(double montoRetirado) { this.montoRetirado = montoRetirado; }

    public String getRazonRetiro() { return razonRetiro; }
    public void setRazonRetiro(String razonRetiro) { this.razonRetiro = razonRetiro; }

    public LocalDateTime getFechaRetiro() { return fechaRetiro; }
    public void setFechaRetiro(LocalDateTime fechaRetiro) { this.fechaRetiro = fechaRetiro; }
}
