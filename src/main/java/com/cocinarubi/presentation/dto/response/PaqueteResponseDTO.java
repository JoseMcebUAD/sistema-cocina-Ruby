package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.Estatus;

import java.math.BigDecimal;
import java.util.List;

public class PaqueteResponseDTO {

    private int idPaquete;
    private BigDecimal precio;
    private String descripcion;
    private Estatus estatus;
    private List<PaqueteLineaResponseDTO> productos;

    public PaqueteResponseDTO() {}

    public PaqueteResponseDTO(int idPaquete, BigDecimal precio, String descripcion,
                              Estatus estatus, List<PaqueteLineaResponseDTO> productos) {
        this.idPaquete = idPaquete;
        this.precio = precio;
        this.descripcion = descripcion;
        this.estatus = estatus;
        this.productos = productos;
    }

    public int getIdPaquete() { return idPaquete; }
    public void setIdPaquete(int idPaquete) { this.idPaquete = idPaquete; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Estatus getEstatus() { return estatus; }
    public void setEstatus(Estatus estatus) { this.estatus = estatus; }

    public List<PaqueteLineaResponseDTO> getProductos() { return productos; }
    public void setProductos(List<PaqueteLineaResponseDTO> productos) { this.productos = productos; }
}
