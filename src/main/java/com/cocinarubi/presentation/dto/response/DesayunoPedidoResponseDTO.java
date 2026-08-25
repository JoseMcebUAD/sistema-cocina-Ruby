package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;

public class DesayunoPedidoResponseDTO {

    private int idDesayunoPedido;
    private Integer idDesayuno;
    private String nombreDesayuno;
    private BigDecimal precio;

    public DesayunoPedidoResponseDTO() {}

    public DesayunoPedidoResponseDTO(int idDesayunoPedido, Integer idDesayuno, String nombreDesayuno, BigDecimal precio) {
        this.idDesayunoPedido = idDesayunoPedido;
        this.idDesayuno = idDesayuno;
        this.nombreDesayuno = nombreDesayuno;
        this.precio = precio;
    }

    public int getIdDesayunoPedido() { return idDesayunoPedido; }
    public void setIdDesayunoPedido(int idDesayunoPedido) { this.idDesayunoPedido = idDesayunoPedido; }

    public Integer getIdDesayuno() { return idDesayuno; }
    public void setIdDesayuno(Integer idDesayuno) { this.idDesayuno = idDesayuno; }

    public String getNombreDesayuno() { return nombreDesayuno; }
    public void setNombreDesayuno(String nombreDesayuno) { this.nombreDesayuno = nombreDesayuno; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
