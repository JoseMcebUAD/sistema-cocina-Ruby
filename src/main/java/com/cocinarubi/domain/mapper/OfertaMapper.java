package com.cocinarubi.domain.mapper;

import com.cocinarubi.domain.entity.Oferta;
import com.cocinarubi.domain.entity.OfertaLinea;
import com.cocinarubi.domain.entity.OfertaPedido;
import com.cocinarubi.presentation.dto.request.OfertaLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.OfertaRequestDTO;
import com.cocinarubi.presentation.dto.response.OfertaLineaResponseDTO;
import com.cocinarubi.presentation.dto.response.OfertaPedidoLineaResponseDTO;
import com.cocinarubi.presentation.dto.response.OfertaPedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.OfertaResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte entre entidades {@link Oferta}/{@link OfertaPedido} y sus DTOs de respuesta.
 * Capa: Mapper — conversión entidad ↔ DTO.
 */
@Component
public class OfertaMapper {

    /** Construye una entidad {@link Oferta} a partir del DTO de creación. */
    public Oferta toEntity(OfertaRequestDTO dto) {
        Oferta oferta = Oferta.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precioOferta(dto.getPrecioOferta())
                .prioridad(dto.getPrioridad())
                .activo(dto.isActivo())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .tiposPedidoRestriccion(dto.getTiposPedidoRestriccion() != null
                        ? new java.util.HashSet<>(dto.getTiposPedidoRestriccion())
                        : new java.util.HashSet<>())
                .build();
        for (OfertaLineaRequestDTO lineaDto : dto.getLineas()) {
            oferta.addLinea(toLineaEntity(lineaDto));
        }
        return oferta;
    }

    public OfertaLinea toLineaEntity(OfertaLineaRequestDTO dto) {
        return OfertaLinea.builder()
                .modoMatch(dto.getModoMatch())
                .idReferencia(dto.getIdReferencia())
                .tipoComidaFiltro(dto.getTipoComidaFiltro())
                .cantidad(dto.getCantidad())
                .build();
    }

    public OfertaResponseDTO toResponse(Oferta oferta) {
        OfertaResponseDTO dto = new OfertaResponseDTO();
        dto.setIdOferta(oferta.getIdOferta());
        dto.setNombre(oferta.getNombre());
        dto.setDescripcion(oferta.getDescripcion());
        dto.setPrecioOferta(oferta.getPrecioOferta());
        dto.setPrioridad(oferta.getPrioridad());
        dto.setActivo(oferta.isActivo());
        dto.setFechaInicio(oferta.getFechaInicio());
        dto.setFechaFin(oferta.getFechaFin());
        dto.setTiposPedidoRestriccion(oferta.getTiposPedidoRestriccion());
        dto.setLineas(oferta.getLineas().stream()
                .map(this::toLineaResponse)
                .collect(Collectors.toList()));
        return dto;
    }

    private OfertaLineaResponseDTO toLineaResponse(OfertaLinea linea) {
        return new OfertaLineaResponseDTO(
                linea.getIdOfertaLinea(),
                linea.getModoMatch(),
                linea.getIdReferencia(),
                linea.getTipoComidaFiltro(),
                linea.getCantidad()
        );
    }

    /** Mapea una aplicación de oferta en pedido a su DTO de respuesta. */
    public OfertaPedidoResponseDTO toOfertaPedidoResponse(OfertaPedido op) {
        OfertaPedidoResponseDTO dto = new OfertaPedidoResponseDTO();
        dto.setIdOfertaPedido(op.getIdOfertaPedido());
        if (op.getOferta() != null) {
            dto.setIdOferta(op.getOferta().getIdOferta());
            dto.setNombreOferta(op.getOferta().getNombre());
        }
        dto.setPrecioAplicado(op.getPrecioAplicado());
        dto.setAhorro(op.getAhorro());
        List<OfertaPedidoLineaResponseDTO> productos = op.getLineas().stream()
                .map(l -> new OfertaPedidoLineaResponseDTO(
                        l.getTipoEntidad(),
                        l.getIdProductoCatalogo(),
                        l.getNombreProducto(),
                        l.getCantidadConsumida(),
                        l.getPrecioNormalConsumido()))
                .collect(Collectors.toList());
        dto.setProductos(productos);
        return dto;
    }
}
