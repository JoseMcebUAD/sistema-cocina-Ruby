package com.cocinarubi.domain.mapper;

import com.cocinarubi.DBConstants.TipoMedida;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.presentation.dto.request.ProductoRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Convierte entidades {@link Producto} a/desde sus DTOs. Calcula
 * {@code costoUnitarioBase = costoPresentacion / contenidoPresentacion} con escala 4.
 *
 * <p>Capa: Mapper — sin lógica de negocio, pero sí deriva el costo unitario base
 * aquí para que el service no se preocupe por BigDecimal al guardar.</p>
 */
@Component
public class ProductoMapper {

    private static final int SCALE_COSTO_UNITARIO = 4;
    private static final RoundingMode MODE = RoundingMode.HALF_UP;

    /** Construye una nueva entidad desde el request; no persiste. */
    public Producto toEntity(ProductoRequestDTO dto) {
        return Producto.builder()
                .nombre(dto.getNombre())
                .tipoMedida(dto.getTipoMedida())
                .unidadBase(dto.getUnidadBase())
                // Solo se guarda peso_promedio_pieza para productos de pieza variable; se ignora en el otro caso.
                .pesoPromedioPieza(dto.getTipoMedida() == TipoMedida.PIEZA_VARIABLE ? dto.getPesoPromedioPieza() : null)
                .contenidoPresentacion(dto.getContenidoPresentacion())
                .unidadPresentacion(dto.getUnidadPresentacion())
                .costoPresentacion(dto.getCostoPresentacion())
                .costoUnitarioBase(calcularCostoUnitarioBase(dto.getCostoPresentacion(), dto.getContenidoPresentacion()))
                .build();
    }

    /** Reemplaza los campos de una entidad existente con los del request y recalcula costoUnitarioBase. */
    public void updateEntity(Producto target, ProductoRequestDTO dto) {
        target.setNombre(dto.getNombre());
        target.setTipoMedida(dto.getTipoMedida());
        target.setUnidadBase(dto.getUnidadBase());
        target.setPesoPromedioPieza(dto.getTipoMedida() == TipoMedida.PIEZA_VARIABLE ? dto.getPesoPromedioPieza() : null);
        target.setContenidoPresentacion(dto.getContenidoPresentacion());
        target.setUnidadPresentacion(dto.getUnidadPresentacion());
        target.setCostoPresentacion(dto.getCostoPresentacion());
        target.setCostoUnitarioBase(calcularCostoUnitarioBase(dto.getCostoPresentacion(), dto.getContenidoPresentacion()));
    }

    public ProductoResponseDTO toResponse(Producto entity) {
        return ProductoResponseDTO.builder()
                .idProducto(entity.getIdProducto())
                .nombre(entity.getNombre())
                .tipoMedida(entity.getTipoMedida())
                .unidadBase(entity.getUnidadBase())
                .pesoPromedioPieza(entity.getPesoPromedioPieza())
                .contenidoPresentacion(entity.getContenidoPresentacion())
                .unidadPresentacion(entity.getUnidadPresentacion())
                .costoPresentacion(entity.getCostoPresentacion())
                .costoUnitarioBase(entity.getCostoUnitarioBase())
                .build();
    }

    public List<ProductoResponseDTO> toResponseList(List<Producto> entities) {
        return entities.stream().map(this::toResponse).toList();
    }

    private BigDecimal calcularCostoUnitarioBase(BigDecimal costoPresentacion, BigDecimal contenidoPresentacion) {
        return costoPresentacion.divide(contenidoPresentacion, SCALE_COSTO_UNITARIO, MODE);
    }
}
