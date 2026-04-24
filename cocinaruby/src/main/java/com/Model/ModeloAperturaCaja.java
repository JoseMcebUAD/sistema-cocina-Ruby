package com.Model;

import java.time.LocalDateTime;

public class ModeloAperturaCaja {
    private int idApertura;
    private int idRelUsuario;
    private double montoInicial;
    private LocalDateTime fechaApertura;

    public int getIdApertura() { return idApertura; }
    public void setIdApertura(int idApertura) { this.idApertura = idApertura; }

    public int getIdRelUsuario() { return idRelUsuario; }
    public void setIdRelUsuario(int idRelUsuario) { this.idRelUsuario = idRelUsuario; }

    public double getMontoInicial() { return montoInicial; }
    public void setMontoInicial(double montoInicial) { this.montoInicial = montoInicial; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
}
