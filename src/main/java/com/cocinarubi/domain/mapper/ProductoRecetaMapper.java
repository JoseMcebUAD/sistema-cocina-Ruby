package com.cocinarubi.domain.mapper;

import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.domain.entity.ProductoReceta;
import com.cocinarubi.presentation.dto.request.ProductoRecetaRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoRecetaResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Convierte entidades {@link ProductoReceta} a/desde sus DTOs. Toma el snapshot de
 * {@code costoUnitario} desde {@code Producto.costoUnitarioBase} al momento de registrar
 * y calcula {@code costoTotal = cantidadUsada * costoUnitario} con escala 2.
 */
@Component
public class ProductoRecetaMapper {

    private static final int SCALE_DINERO = 2;
    private static final RoundingMode MODE = RoundingMode.HALF_UP;

    /** Construye un ingrediente nuevo asociado a la comida/producto dados. */
    public ProductoReceta toEntity(ProductoRecetaRequestDTO dto, Comida comida, Producto producto) {
        BigDecimal snapshotCostoUnitario = producto.getCostoUnitarioBase();
        BigDecimal costoTotal = dto.getCantidadUsada()
                .multiply(snapshotCostoUnitario)
                .setScale(SCALE_DINERO, MODE);

        return ProductoReceta.builder()
                .comida(comida)
                .producto(producto)
                .cantidadUsada(dto.getCantidadUsada())
                .unidadUsada(dto.getUnidadUsada())
                .esAproximado(Boolean.TRUE.equals(dto.getEsAproximado()))
                .costoUnitario(snapshotCostoUnitario)
                .costoTotal(costoTotal)
                .build();
    }

    /** Actualiza un ingrediente existente con el request; recalcula snapshot si cambió producto o cantidad. */
    public void updateEntity(ProductoReceta target, ProductoRecetaRequestDTO dto, Producto producto) {
        BigDecimal snapshotCostoUnitario = producto.getCostoUnitarioBase();
        BigDecimal costoTotal = dto.getCantidadUsada()
                .multiply(snapshotCostoUnitario)
                .setScale(SCALE_DINERO, MODE);

        target.setProducto(producto);
        target.setCantidadUsada(dto.getCantidadUsada());
        target.setUnidadUsada(dto.getUnidadUsada());
        target.setEsAproximado(Boolean.TRUE.equals(dto.getEsAproximado()));
        target.setCostoUnitario(snapshotCostoUnitario);
        target.setCostoTotal(costoTotal);
    }

    public ProductoRecetaResponseDTO toResponse(ProductoReceta entity) {
        Producto p = entity.getProducto();
        return ProductoRecetaResponseDTO.builder()
                .idProductoReceta(entity.getIdProductoReceta())
                .idComida(entity.getComida() != null ? entity.getComida().getIdComida() : null)
                .idProducto(p.getIdProducto())
                .nombreProducto(p.getNombre())
                .tipoMedida(p.getTipoMedida())
                .cantidadUsada(entity.getCantidadUsada())
                .unidadUsada(entity.getUnidadUsada())
                .esAproximado(entity.isEsAproximado())
                .costoUnitarioSnapshot(entity.getCostoUnitario())
                .costoTotalSnapshot(entity.getCostoTotal())
                .build();
    }

    public List<ProductoRecetaResponseDTO> toResponseList(List<ProductoReceta> entities) {
        return entities.stream().map(this::toResponse).toList();
    }
}
