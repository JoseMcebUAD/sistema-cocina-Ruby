package com.cocinarubi.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ClienteRequestDTO {

    @JsonProperty("idRuta")
    private Integer idRuta;

    @NotBlank(message = "El uuid del cliente no puede estar vacío")
    @Size(max = 45, message = "El uuid no puede exceder 45 caracteres")
    @JsonProperty("uuidCliente")
    private String uuidCliente;

    @NotBlank(message = "El session token no puede estar vacío")
    @Size(max = 255, message = "El session token no puede exceder 255 caracteres")
    @JsonProperty("sessionToken")
    private String sessionToken;

    @JsonProperty("codigoCliente")
    private String codigoCliente;

    @JsonProperty("userAgent")
    private String userAgent;

    @Size(max = 45, message = "La IP no puede exceder 45 caracteres")
    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("ubicacionLatitud")
    private BigDecimal ubicacionLatitud;

    @JsonProperty("ubicacionLongitud")
    private BigDecimal ubicacionLongitud;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("direccionCliente")
    private String direccionCliente;

    @Size(max = 16, message = "El teléfono no puede exceder 16 caracteres")
    @JsonProperty("telefono")
    private String telefono;

    public ClienteRequestDTO() {}

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public String getUuidCliente() { return uuidCliente; }
    public void setUuidCliente(String uuidCliente) { this.uuidCliente = uuidCliente; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public BigDecimal getUbicacionLatitud() { return ubicacionLatitud; }
    public void setUbicacionLatitud(BigDecimal ubicacionLatitud) { this.ubicacionLatitud = ubicacionLatitud; }

    public BigDecimal getUbicacionLongitud() { return ubicacionLongitud; }
    public void setUbicacionLongitud(BigDecimal ubicacionLongitud) { this.ubicacionLongitud = ubicacionLongitud; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccionCliente() { return direccionCliente; }
    public void setDireccionCliente(String direccionCliente) { this.direccionCliente = direccionCliente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
