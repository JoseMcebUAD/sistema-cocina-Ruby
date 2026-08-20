package com.cocinarubi.presentation.dto.response;

/**
 * Representación de un complemento predeterminado asignado a una comida.
 * Capa: DTO de salida — presentation/dto/response.
 */
public class ComplementoPredeterminadoComidaResponseDTO {

    private int idComplementoPredeterminadoComida;
    private int idComplemento;
    private String nombreComplemento;
    private int cantidad;

    public ComplementoPredeterminadoComidaResponseDTO() {}

    public ComplementoPredeterminadoComidaResponseDTO(int idComplementoPredeterminadoComida,
                                                      int idComplemento,
                                                      String nombreComplemento,
                                                      int cantidad) {
        this.idComplementoPredeterminadoComida = idComplementoPredeterminadoComida;
        this.idComplemento = idComplemento;
        this.nombreComplemento = nombreComplemento;
        this.cantidad = cantidad;
    }

    public int getIdComplementoPredeterminadoComida() { return idComplementoPredeterminadoComida; }
    public void setIdComplementoPredeterminadoComida(int idComplementoPredeterminadoComida) { this.idComplementoPredeterminadoComida = idComplementoPredeterminadoComida; }

    public int getIdComplemento() { return idComplemento; }
    public void setIdComplemento(int idComplemento) { this.idComplemento = idComplemento; }

    public String getNombreComplemento() { return nombreComplemento; }
    public void setNombreComplemento(String nombreComplemento) { this.nombreComplemento = nombreComplemento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
