package com.cocinarubi.domain.mapper;

import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.presentation.dto.request.PagoRepartidorRequestDTO;
import com.cocinarubi.presentation.dto.response.PagoRepartidorResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte entre {@link PagoRepartidor} y sus DTOs de request/response.
 * Capa: Mapper — mapeo manual (patrón coherente con {@code PedidoMapper}).
 */
@Component
public class PagoRepartidorMapper {

    public PagoRepartidor toEntity(PagoRepartidorRequestDTO dto) {
        return PagoRepartidor.builder()
                .idPagoRepartidor(dto.getIdPagoRepartidor())
                .pago(dto.getPago())
                .fechaPago(dto.getFechaPago())
                .build();
    }

    public PagoRepartidorResponseDTO toResponse(PagoRepartidor entity) {
        return PagoRepartidorResponseDTO.builder()
                .idPagoRepartidor(entity.getIdPagoRepartidor())
                .pago(entity.getPago())
                .fechaPago(entity.getFechaPago())
                .build();
    }

    public List<PagoRepartidorResponseDTO> toResponseList(List<PagoRepartidor> entities) {
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
