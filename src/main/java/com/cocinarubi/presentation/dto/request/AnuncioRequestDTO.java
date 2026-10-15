package com.cocinarubi.presentation.dto.request;

import com.cocinarubi.DBConstants.PrioridadAnuncio;
import com.cocinarubi.DBConstants.TipoProductoAnuncio;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de entrada para crear o actualizar un anuncio.
 * Capa: DTO de request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnuncioRequestDTO {

    private String descripcionAnuncio;
    private String color;
    private LocalDateTime fechaExpiracionAnuncio;
    private PrioridadAnuncio prioridad;
    private Boolean activo;

    @Builder.Default
    private List<AnuncioProductoDTO> productos = new ArrayList<>();

    /** Referencia polimórfica a un producto del catálogo. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnuncioProductoDTO {
        private TipoProductoAnuncio tipoProducto;
        private Integer idProducto;
    }
}
