package com.cocinarubi.domain.service;

import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ProductoRecetaRepository;
import com.cocinarubi.dao.ProductoRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Producto;
import com.cocinarubi.domain.entity.ProductoReceta;
import com.cocinarubi.domain.mapper.ProductoRecetaMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ProductoRecetaRequestDTO;
import com.cocinarubi.presentation.dto.response.ProductoRecetaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.ProductoRecetaValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gestiona los ingredientes de la receta de una {@code Comida}.
 * Capa: Service — CRUD de la tabla {@code producto_receta}, siempre acotado por idComida.
 */
@Service
public class ProductoRecetaService {

    private final ProductoRecetaRepository productoRecetaRepository;
    private final ComidaRepository comidaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoRecetaMapper productoRecetaMapper;
    private final ProductoRecetaValidationImp productoRecetaValidation;

    public ProductoRecetaService(ProductoRecetaRepository productoRecetaRepository,
                                 ComidaRepository comidaRepository,
                                 ProductoRepository productoRepository,
                                 ProductoRecetaMapper productoRecetaMapper,
                                 ProductoRecetaValidationImp productoRecetaValidation) {
        this.productoRecetaRepository = productoRecetaRepository;
        this.comidaRepository = comidaRepository;
        this.productoRepository = productoRepository;
        this.productoRecetaMapper = productoRecetaMapper;
        this.productoRecetaValidation = productoRecetaValidation;
    }

    public List<ProductoRecetaResponseDTO> findByIdComida(int idComida) {
        if (!comidaRepository.existsById(idComida)) {
            throw new BusinessException(
                    "Comida no encontrada con id: " + idComida, HttpStatus.NOT_FOUND);
        }
        return productoRecetaMapper.toResponseList(
                productoRecetaRepository.findByIdComidaWithProducto(idComida));
    }

    public ProductoRecetaResponseDTO addIngrediente(int idComida, ProductoRecetaRequestDTO dto) {
        // ProductoRecetaValidationImp: existencia de comida/producto y no duplicar.
        productoRecetaValidation.validarPost(idComida, dto);
        Comida comida = comidaRepository.findById(idComida)
                .orElseThrow(() -> new BusinessException(
                        "Comida no encontrada con id: " + idComida, HttpStatus.NOT_FOUND));
        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new BusinessException(
                        "Producto no encontrado con id: " + dto.getIdProducto(), HttpStatus.NOT_FOUND));
        ProductoReceta nuevo = productoRecetaMapper.toEntity(dto, comida, producto);
        return productoRecetaMapper.toResponse(productoRecetaRepository.save(nuevo));
    }

    public ProductoRecetaResponseDTO updateIngrediente(int idComida,
                                                       int idProductoReceta,
                                                       ProductoRecetaRequestDTO dto) {
        productoRecetaValidation.validarPut(idComida, idProductoReceta, dto);
        ProductoReceta existente = productoRecetaRepository.findById(idProductoReceta)
                .orElseThrow(() -> new BusinessException(
                        "Ingrediente de receta no encontrado con id: " + idProductoReceta,
                        HttpStatus.NOT_FOUND));
        // Blindaje: el ingrediente debe pertenecer a la comida que viene por path.
        if (existente.getComida() == null || existente.getComida().getIdComida() != idComida) {
            throw new BusinessException(
                    "El ingrediente " + idProductoReceta + " no pertenece a la receta de la comida " + idComida,
                    HttpStatus.BAD_REQUEST);
        }
        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new BusinessException(
                        "Producto no encontrado con id: " + dto.getIdProducto(), HttpStatus.NOT_FOUND));
        productoRecetaMapper.updateEntity(existente, dto, producto);
        return productoRecetaMapper.toResponse(productoRecetaRepository.save(existente));
    }

    public void deleteIngrediente(int idComida, int idProductoReceta) {
        ProductoReceta existente = productoRecetaRepository.findById(idProductoReceta)
                .orElseThrow(() -> new BusinessException(
                        "Ingrediente de receta no encontrado con id: " + idProductoReceta,
                        HttpStatus.NOT_FOUND));
        if (existente.getComida() == null || existente.getComida().getIdComida() != idComida) {
            throw new BusinessException(
                    "El ingrediente " + idProductoReceta + " no pertenece a la receta de la comida " + idComida,
                    HttpStatus.BAD_REQUEST);
        }
        productoRecetaRepository.deleteById(idProductoReceta);
    }
}
