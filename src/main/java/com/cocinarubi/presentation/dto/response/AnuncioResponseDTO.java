package com.cocinarubi.presentation.dto.response;

import com.cocinarubi.DBConstants.PrioridadAnuncio;
import com.cocinarubi.DBConstants.TipoProductoAnuncio;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de salida de un anuncio con sus productos asociados.
 * Capa: DTO de response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnuncioResponseDTO {

    private Integer idAnuncio;
    private String descripcionAnuncio;
    private String color;
    private LocalDateTime fechaExpiracionAnuncio;
    private PrioridadAnuncio prioridad;
    private boolean activo;
    private String imagenUrl;
    private List<AnuncioProductoResponseDTO> productos;

    /** Producto asociado al anuncio con su tipo polimórfico. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnuncioProductoResponseDTO {
        private Integer idAnuncioProducto;
        private TipoProductoAnuncio tipoProducto;
        private Integer idProducto;
    }
}
