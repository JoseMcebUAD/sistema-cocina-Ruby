package com.cocinarubi.domain.mapper;

import com.cocinarubi.DBConstants.PrioridadAnuncio;
import com.cocinarubi.domain.entity.Anuncio;
import com.cocinarubi.domain.entity.AnuncioProducto;
import com.cocinarubi.presentation.dto.request.AnuncioRequestDTO;
import com.cocinarubi.presentation.dto.response.AnuncioResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte entre {@link Anuncio} y sus DTOs de request/response.
 * Capa: Mapper — mapeo manual coherente con {@code PagoRepartidorMapper}.
 */
@Component
public class AnuncioMapper {

    public AnuncioResponseDTO toResponse(Anuncio entity) {
        List<AnuncioResponseDTO.AnuncioProductoResponseDTO> productos = entity.getProductos() == null
                ? Collections.emptyList()
                : entity.getProductos().stream().map(this::toProductoResponse).collect(Collectors.toList());

        return AnuncioResponseDTO.builder()
                .idAnuncio(entity.getIdAnuncio())
                .descripcionAnuncio(entity.getDescripcionAnuncio())
                .color(entity.getColor())
                .fechaExpiracionAnuncio(entity.getFechaExpiracionAnuncio())
                .prioridad(entity.getPrioridad())
                .activo(entity.isActivo())
                .imagenUrl(entity.getImagenUrl())
                .productos(productos)
                .build();
    }

    /** Crea una entidad Anuncio sin imagen (la imagen se sube por endpoint separado). */
    public Anuncio toEntity(AnuncioRequestDTO dto) {
        Anuncio anuncio = Anuncio.builder()
                .descripcionAnuncio(dto.getDescripcionAnuncio())
                .color(dto.getColor())
                .fechaExpiracionAnuncio(dto.getFechaExpiracionAnuncio())
                .prioridad(dto.getPrioridad() != null ? dto.getPrioridad() : PrioridadAnuncio.MEDIA)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        if (dto.getProductos() != null) {
            List<AnuncioProducto> productos = dto.getProductos().stream()
                    .map(p -> toProductoEntity(p, anuncio))
                    .collect(Collectors.toList());
            anuncio.setProductos(productos);
        }

        return anuncio;
    }

    public List<AnuncioResponseDTO> toResponseList(List<Anuncio> entities) {
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private AnuncioResponseDTO.AnuncioProductoResponseDTO toProductoResponse(AnuncioProducto entity) {
        return AnuncioResponseDTO.AnuncioProductoResponseDTO.builder()
                .idAnuncioProducto(entity.getIdAnuncioProducto())
                .tipoProducto(entity.getTipoProducto())
                .idProducto(entity.getIdProducto())
                .build();
    }

    private AnuncioProducto toProductoEntity(AnuncioRequestDTO.AnuncioProductoDTO dto, Anuncio anuncio) {
        return AnuncioProducto.builder()
                .anuncio(anuncio)
                .tipoProducto(dto.getTipoProducto())
                .idProducto(dto.getIdProducto())
                .build();
    }
}
