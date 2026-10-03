package com.cocinarubi.domain.service;

import com.cocinarubi.dao.ProductoRepository;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.domain.mapper.ProductoMapper;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.ProductoRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gestiona el catálogo de productos / ingredientes de cocina.
 * Capa: Service — lógica de negocio y validaciones del catálogo.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final ProductoValidationImp productoValidation;

    public ProductoService(ProductoRepository productoRepository,
                           ProductoMapper productoMapper,
                           ProductoValidationImp productoValidation) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.productoValidation = productoValidation;
    }

    public List<ProductoResponseDTO> findAll() {
        return productoMapper.toResponseList(productoRepository.findAll());
    }

    public ProductoResponseDTO findById(int id) {
        return productoMapper.toResponse(findEntityById(id));
    }

    public ProductoResponseDTO save(ProductoRequestDTO dto) {
        // ProductoValidationImp: exige peso_promedio_pieza > 0 cuando el producto es de pieza variable.
        productoValidation.validarPost(dto);
        if (productoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new BusinessException(
                    "Ya existe un producto con el nombre '" + dto.getNombre() + "'",
                    HttpStatus.CONFLICT,
                    ErrorCode.VALIDACION);
        }
        Producto nuevo = productoMapper.toEntity(dto);
        return productoMapper.toResponse(productoRepository.save(nuevo));
    }

    public ProductoResponseDTO update(int id, ProductoRequestDTO dto) {
        productoValidation.validarPost(dto);
        Producto existente = findEntityById(id);
        // Si cambia el nombre, verifica unicidad contra otros productos.
        if (!existente.getNombre().equalsIgnoreCase(dto.getNombre())
                && productoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new BusinessException(
                    "Ya existe un producto con el nombre '" + dto.getNombre() + "'",
                    HttpStatus.CONFLICT,
                    ErrorCode.VALIDACION);
        }
        productoMapper.updateEntity(existente, dto);
        return productoMapper.toResponse(productoRepository.save(existente));
    }

    /**
     * Elimina un producto del catálogo.
     * Si está en uso en alguna receta y no se saltó la confirmación, lanza
     * {@link AdvertenciaEliminacionException} para que el front muestre un diálogo.
     */
    public void delete(int id, boolean saltarConfirmacion) {
        if (!productoRepository.existsById(id)) {
            throw new BusinessException("Producto no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (!saltarConfirmacion && productoRepository.existsEnReceta(id)) {
            throw new AdvertenciaEliminacionException(
                    "Este producto está en una o más recetas. ¿Desea continuar con la eliminación?");
        }
        productoRepository.deleteById(id);
    }

    /** Carga la entidad (uso interno desde otros services). */
    public Producto findEntityById(int id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Producto no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }
}
