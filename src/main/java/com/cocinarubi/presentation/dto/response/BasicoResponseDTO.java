package com.cocinarubi.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;

//DTO del básico para respuesta hacia el front
public class BasicoResponseDTO {

    private int idBasico;
    private String nombreComida;
    private String descripcion;
    private boolean destacado;
    private BigDecimal precioBasico;
    private List<ComplementoResponseDTO> complementos;
    private int totalComplementos;

    public BasicoResponseDTO() {}

    public BasicoResponseDTO(int idBasico, String nombreComida, String descripcion,
                              boolean destacado, BigDecimal precioBasico,
                              List<ComplementoResponseDTO> complementos) {
        this.idBasico = idBasico;
        this.nombreComida = nombreComida;
        this.descripcion = descripcion;
        this.destacado = destacado;
        this.precioBasico = precioBasico;
        this.complementos = complementos;
        this.totalComplementos = complementos != null ? complementos.size() : 0;
    }

    public int getIdBasico() { return idBasico; }
    public void setIdBasico(int idBasico) { this.idBasico = idBasico; }

    public String getNombreComida() { return nombreComida; }
    public void setNombreComida(String nombreComida) { this.nombreComida = nombreComida; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public BigDecimal getPrecioBasico() { return precioBasico; }
    public void setPrecioBasico(BigDecimal precioBasico) { this.precioBasico = precioBasico; }

    //complementos seleccionados del básico
    public List<ComplementoResponseDTO> getComplementos() { return complementos; }
    public void setComplementos(List<ComplementoResponseDTO> complementos) {
        this.complementos = complementos;
        this.totalComplementos = complementos != null ? complementos.size() : 0;
    }

    public int getTotalComplementos() { return totalComplementos; }
    public void setTotalComplementos(int totalComplementos) { this.totalComplementos = totalComplementos; }
}
