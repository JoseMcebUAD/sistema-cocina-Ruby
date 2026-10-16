package com.cocinarubi.presentation.dto.response;

/**
 * Proyección pública de la tarjeta activa para el checkout web.
 * No expone idTarjeta ni estatus — solo los datos necesarios para realizar la transferencia.
 */
public class TarjetaTransferenciaActivaDTO {

    private String clabe;
    private String numeroTarjeta;
    private String nombreDestinatario;
    private String banco;

    public TarjetaTransferenciaActivaDTO() {}

    public TarjetaTransferenciaActivaDTO(String clabe, String numeroTarjeta,
                                         String nombreDestinatario, String banco) {
        this.clabe = clabe;
        this.numeroTarjeta = numeroTarjeta;
        this.nombreDestinatario = nombreDestinatario;
        this.banco = banco;
    }

    public String getClabe() { return clabe; }
    public void setClabe(String clabe) { this.clabe = clabe; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
}
