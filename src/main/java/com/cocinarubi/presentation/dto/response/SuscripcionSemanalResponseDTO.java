package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.OrigenSuscripcion;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SuscripcionSemanalResponseDTO {

    private int idSuscripcionSemanal;
    private String identificadorCliente;
    private String uuidCliente;
    private LocalDate semanaInicio;
    private int comidasPorDia;
    private BigDecimal precioSuscripcion;
    private MetodoPago metodoPago;
    private String codigoRecuperacion;
    private boolean activo;
    private OrigenSuscripcion origen;

    public SuscripcionSemanalResponseDTO() {}

    public SuscripcionSemanalResponseDTO(int idSuscripcionSemanal, String identificadorCliente,
                                         String uuidCliente, LocalDate semanaInicio,
                                         int comidasPorDia, BigDecimal precioSuscripcion,
                                         MetodoPago metodoPago, String codigoRecuperacion,
                                         boolean activo, OrigenSuscripcion origen) {
        this.idSuscripcionSemanal = idSuscripcionSemanal;
        this.identificadorCliente = identificadorCliente;
        this.uuidCliente = uuidCliente;
        this.semanaInicio = semanaInicio;
        this.comidasPorDia = comidasPorDia;
        this.precioSuscripcion = precioSuscripcion;
        this.metodoPago = metodoPago;
        this.codigoRecuperacion = codigoRecuperacion;
        this.activo = activo;
        this.origen = origen;
    }

    public int getIdSuscripcionSemanal() { return idSuscripcionSemanal; }
    public void setIdSuscripcionSemanal(int idSuscripcionSemanal) { this.idSuscripcionSemanal = idSuscripcionSemanal; }

    public String getIdentificadorCliente() { return identificadorCliente; }
    public void setIdentificadorCliente(String identificadorCliente) { this.identificadorCliente = identificadorCliente; }

    public String getUuidCliente() { return uuidCliente; }
    public void setUuidCliente(String uuidCliente) { this.uuidCliente = uuidCliente; }

    public LocalDate getSemanaInicio() { return semanaInicio; }
    public void setSemanaInicio(LocalDate semanaInicio) { this.semanaInicio = semanaInicio; }

    public int getComidasPorDia() { return comidasPorDia; }
    public void setComidasPorDia(int comidasPorDia) { this.comidasPorDia = comidasPorDia; }

    public BigDecimal getPrecioSuscripcion() { return precioSuscripcion; }
    public void setPrecioSuscripcion(BigDecimal precioSuscripcion) { this.precioSuscripcion = precioSuscripcion; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getCodigoRecuperacion() { return codigoRecuperacion; }
    public void setCodigoRecuperacion(String codigoRecuperacion) { this.codigoRecuperacion = codigoRecuperacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public OrigenSuscripcion getOrigen() { return origen; }
    public void setOrigen(OrigenSuscripcion origen) { this.origen = origen; }
}
