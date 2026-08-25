package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoProductoFavorito;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FavoritoClienteRequestDTO {

    @NotBlank(message = "El session token del cliente no puede estar vacío")
    @JsonProperty("sessionToken")
    private String sessionToken;

    @NotNull(message = "El id del producto no puede ser nulo")
    @Positive(message = "El id del producto debe ser mayor a cero")
    @JsonProperty("idProducto")
    private Integer idProducto;

    @NotNull(message = "El tipo de catálogo no puede ser nulo")
    @JsonProperty("tipoCatalogoProducto")
    private TipoProductoFavorito tipoCatalogoProducto;

    @JsonProperty("saltarConfirmacion")
    private boolean saltarConfirmacion = false;

    public FavoritoClienteRequestDTO() {}

    public FavoritoClienteRequestDTO(String sessionToken, Integer idProducto,
                                     TipoProductoFavorito tipoCatalogoProducto) {
        this.sessionToken = sessionToken;
        this.idProducto = idProducto;
        this.tipoCatalogoProducto = tipoCatalogoProducto;
    }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public TipoProductoFavorito getTipoCatalogoProducto() { return tipoCatalogoProducto; }
    public void setTipoCatalogoProducto(TipoProductoFavorito tipoCatalogoProducto) {
        this.tipoCatalogoProducto = tipoCatalogoProducto;
    }

    public boolean isSaltarConfirmacion() { return saltarConfirmacion; }
    public void setSaltarConfirmacion(boolean saltarConfirmacion) { this.saltarConfirmacion = saltarConfirmacion; }
}
