package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.TipoEntidadOferta;

import java.math.BigDecimal;

public class OfertaPedidoLineaResponseDTO {

    private TipoEntidadOferta tipoEntidad;
    private int idProductoCatalogo;
    private String nombreProducto;
    private int cantidadConsumida;
    private BigDecimal precioNormalConsumido;

    public OfertaPedidoLineaResponseDTO() {}

    public OfertaPedidoLineaResponseDTO(TipoEntidadOferta tipoEntidad, int idProductoCatalogo,
                                         String nombreProducto, int cantidadConsumida,
                                         BigDecimal precioNormalConsumido) {
        this.tipoEntidad = tipoEntidad;
        this.idProductoCatalogo = idProductoCatalogo;
        this.nombreProducto = nombreProducto;
        this.cantidadConsumida = cantidadConsumida;
        this.precioNormalConsumido = precioNormalConsumido;
    }

    public TipoEntidadOferta getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(TipoEntidadOferta tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public int getIdProductoCatalogo() { return idProductoCatalogo; }
    public void setIdProductoCatalogo(int idProductoCatalogo) { this.idProductoCatalogo = idProductoCatalogo; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidadConsumida() { return cantidadConsumida; }
    public void setCantidadConsumida(int cantidadConsumida) { this.cantidadConsumida = cantidadConsumida; }

    public BigDecimal getPrecioNormalConsumido() { return precioNormalConsumido; }
    public void setPrecioNormalConsumido(BigDecimal precioNormalConsumido) { this.precioNormalConsumido = precioNormalConsumido; }
}
