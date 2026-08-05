package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoProductoFavorito;

public class FavoritoClienteResponseDTO {

    private int idFavoritoCliente;
    private String sessionToken;
    private int idProducto;
    private TipoProductoFavorito tipoCatalogoProducto;
    private String nombreProducto;

    public FavoritoClienteResponseDTO() {}

    public FavoritoClienteResponseDTO(int idFavoritoCliente, String sessionToken, int idProducto,
                                      TipoProductoFavorito tipoCatalogoProducto, String nombreProducto) {
        this.idFavoritoCliente = idFavoritoCliente;
        this.sessionToken = sessionToken;
        this.idProducto = idProducto;
        this.tipoCatalogoProducto = tipoCatalogoProducto;
        this.nombreProducto = nombreProducto;
    }

    public int getIdFavoritoCliente() { return idFavoritoCliente; }
    public void setIdFavoritoCliente(int idFavoritoCliente) { this.idFavoritoCliente = idFavoritoCliente; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public TipoProductoFavorito getTipoCatalogoProducto() { return tipoCatalogoProducto; }
    public void setTipoCatalogoProducto(TipoProductoFavorito tipoCatalogoProducto) {
        this.tipoCatalogoProducto = tipoCatalogoProducto;
    }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
}
