package com.cocinarubi.presentation.dto.response.pedido;

public class EscPosBytesDTO {

    private String escposData;

    public EscPosBytesDTO(String escposData) {
        this.escposData = escposData;
    }

    public String getEscposData() { return escposData; }
    public void setEscposData(String escposData) { this.escposData = escposData; }
}
