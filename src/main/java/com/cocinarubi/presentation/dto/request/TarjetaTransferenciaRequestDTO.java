package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TarjetaTransferenciaRequestDTO {

    @NotBlank(message = "La CLABE no puede estar vacía")
    @Size(min = 18, max = 18, message = "La CLABE debe tener exactamente 18 dígitos")
    @JsonProperty("clabe")
    private String clabe;

    @NotBlank(message = "El número de tarjeta no puede estar vacío")
    @Size(min = 16, max = 19, message = "El número de tarjeta debe tener entre 16 y 19 caracteres")
    @JsonProperty("numeroTarjeta")
    private String numeroTarjeta;

    @NotBlank(message = "El nombre del destinatario no puede estar vacío")
    @Size(max = 100, message = "El nombre del destinatario no puede superar 100 caracteres")
    @JsonProperty("nombreDestinatario")
    private String nombreDestinatario;

    @NotBlank(message = "El banco no puede estar vacío")
    @Size(max = 60, message = "El nombre del banco no puede superar 60 caracteres")
    @JsonProperty("banco")
    private String banco;

    public TarjetaTransferenciaRequestDTO() {}

    public String getClabe() { return clabe; }
    public void setClabe(String clabe) { this.clabe = clabe; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
}
