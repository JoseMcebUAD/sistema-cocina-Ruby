package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TarjetaTransferenciaResponseDTO {

    private Integer idTarjeta;
    private String clabe;
    private String numeroTarjeta;
    private String nombreDestinatario;
    private String banco;
    private Estatus estatus;

    public TarjetaTransferenciaResponseDTO() {}

    public TarjetaTransferenciaResponseDTO(Integer idTarjeta, String clabe, String numeroTarjeta,
                                           String nombreDestinatario, String banco, Estatus estatus) {
        this.idTarjeta = idTarjeta;
        this.clabe = clabe;
        this.numeroTarjeta = numeroTarjeta;
        this.nombreDestinatario = nombreDestinatario;
        this.banco = banco;
        this.estatus = estatus;
    }

    public Integer getIdTarjeta() { return idTarjeta; }
    public void setIdTarjeta(Integer idTarjeta) { this.idTarjeta = idTarjeta; }

    public String getClabe() { return clabe; }
    public void setClabe(String clabe) { this.clabe = clabe; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }
}
