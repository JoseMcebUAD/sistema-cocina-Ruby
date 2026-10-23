package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.TipoPedido;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Carrito en construcción enviado al endpoint de preview de ofertas.
 * Todos los campos son opcionales: el servicio ignora IDs nulos o no encontrados
 * en catálogo. No se usa {@code @Valid} en el controlador para evitar que las
 * validaciones de los DTOs anidados fallen antes de llegar al servicio.
 * Capa: Request DTO.
 */
public class OfertaPreviewRequestDTO {

    /** Filtra las ofertas por restricción de tipo de pedido. Null = sin filtro (muestra todas). */
    @JsonProperty("tipoPedido")
    private TipoPedido tipoPedido;

    @JsonProperty("comidas")
    private List<ComidaPedidoDTO> comidas = new ArrayList<>();

    @JsonProperty("basicos")
    private List<BasicoPedidoDTO> basicos = new ArrayList<>();

    @JsonProperty("productosCocina")
    private List<ProductoCocinaPedidoDTO> productosCocina = new ArrayList<>();

    @JsonProperty("combos")
    private List<ComboPedidoDTO> combos = new ArrayList<>();

    public OfertaPreviewRequestDTO() {}

    public TipoPedido getTipoPedido() { return tipoPedido; }
    public void setTipoPedido(TipoPedido tipoPedido) { this.tipoPedido = tipoPedido; }

    public List<ComidaPedidoDTO> getComidas() { return comidas; }
    public void setComidas(List<ComidaPedidoDTO> comidas) {
        this.comidas = comidas != null ? comidas : new ArrayList<>();
    }

    public List<BasicoPedidoDTO> getBasicos() { return basicos; }
    public void setBasicos(List<BasicoPedidoDTO> basicos) {
        this.basicos = basicos != null ? basicos : new ArrayList<>();
    }

    public List<ProductoCocinaPedidoDTO> getProductosCocina() { return productosCocina; }
    public void setProductosCocina(List<ProductoCocinaPedidoDTO> productosCocina) {
        this.productosCocina = productosCocina != null ? productosCocina : new ArrayList<>();
    }

    public List<ComboPedidoDTO> getCombos() { return combos; }
    public void setCombos(List<ComboPedidoDTO> combos) {
        this.combos = combos != null ? combos : new ArrayList<>();
    }
}
