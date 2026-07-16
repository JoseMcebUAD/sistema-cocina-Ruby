package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.ProductoCocinaRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoCocinaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoCocinaConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoCocinaValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductoCocinaService {

    private final ProductoCocinaRepository productoCocinaRepository;
    private final ProductoCocinaValidationImp productoCocinaValidation;
    private final ProductoCocinaConfirmationImp productoCocinaConfirmation;

    public ProductoCocinaService(ProductoCocinaRepository productoCocinaRepository,
                                 ProductoCocinaValidationImp productoCocinaValidation,
                                 ProductoCocinaConfirmationImp productoCocinaConfirmation) {
        this.productoCocinaRepository = productoCocinaRepository;
        this.productoCocinaValidation = productoCocinaValidation;
        this.productoCocinaConfirmation = productoCocinaConfirmation;
    }

    @Transactional(readOnly = true)
    public List<ProductoCocinaResponseDTO> findAll() {
        return productoCocinaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoCocinaResponseDTO> findDisponibles() {
        return productoCocinaRepository.findDisponiblesOrdenados(DBConstants.Estatus.DISPONIBLE)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoCocinaResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public ProductoCocinaResponseDTO save(ProductoCocinaRequestDTO dto) {
        productoCocinaValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            productoCocinaConfirmation.validarPost(dto);
        }
        ProductoCocina entidad = ProductoCocina.builder()
                .uuidProductoCocina(UUID.randomUUID().toString())
                .nombreProducto(dto.getNombreProducto())
                .descripcion(dto.getDescripcion())
                .precioDomicilio(dto.getPrecioDomicilio())
                .precioNormal(dto.getPrecioNormal())
                .estatus(dto.getEstatus())
                .destacado(dto.isDestacado())
                .tipoProducto(dto.getTipoProducto())
                .build();
        return toResponseDTO(productoCocinaRepository.save(entidad));
    }

    @Transactional
    public ProductoCocinaResponseDTO update(int id, ProductoCocinaRequestDTO dto) {
        productoCocinaValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            productoCocinaConfirmation.validarPost(dto);
        }
        ProductoCocina existente = findEntityById(id);
        existente.setNombreProducto(dto.getNombreProducto());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecioDomicilio(dto.getPrecioDomicilio());
        existente.setPrecioNormal(dto.getPrecioNormal());
        existente.setEstatus(dto.getEstatus());
        existente.setDestacado(dto.isDestacado());
        existente.setTipoProducto(dto.getTipoProducto());
        return toResponseDTO(productoCocinaRepository.save(existente));
    }

    public void delete(int id) {
        if (!productoCocinaRepository.existsById(id)) {
            throw new BusinessException(
                    "Producto de cocina no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        // RF-014/017: bloquear eliminación si el producto tiene pedidos asociados
        if (productoCocinaRepository.countEnPedidos(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar el producto porque tiene pedidos asociados",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
        productoCocinaRepository.deleteById(id);
    }

    public ProductoCocina findEntityById(int id) {
        return productoCocinaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Producto de cocina no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private ProductoCocinaResponseDTO toResponseDTO(ProductoCocina entidad) {
        return new ProductoCocinaResponseDTO(
                entidad.getIdProductoCocina(),
                entidad.getNombreProducto(),
                entidad.getDescripcion(),
                entidad.getPrecioDomicilio(),
                entidad.getPrecioNormal(),
                entidad.getEstatus(),
                entidad.isDestacado(),
                entidad.getTipoProducto()
        );
    }
}
