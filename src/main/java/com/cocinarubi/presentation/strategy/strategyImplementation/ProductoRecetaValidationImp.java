package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ProductoRecetaRepository;
import com.cocinarubi.dao.ProductoRepository;
import com.cocinarubi.domain.entity.ProductoReceta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.ProductoRecetaRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Reglas de negocio para los ingredientes de la receta de una comida:
 * - la comida y el producto deben existir
 * - no se puede duplicar el mismo producto dentro de una receta
 *
 * Capa: Strategy — se invoca desde {@code ProductoRecetaService} antes de persistir.
 */
@Component
public class ProductoRecetaValidationImp {

    private final ComidaRepository comidaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoRecetaRepository productoRecetaRepository;

    public ProductoRecetaValidationImp(ComidaRepository comidaRepository,
                                       ProductoRepository productoRepository,
                                       ProductoRecetaRepository productoRecetaRepository) {
        this.comidaRepository = comidaRepository;
        this.productoRepository = productoRepository;
        this.productoRecetaRepository = productoRecetaRepository;
    }

    public void validarPost(int idComida, ProductoRecetaRequestDTO dto) {
        validarComidaYProducto(idComida, dto.getIdProducto());
        if (productoRecetaRepository.existsByComida_IdComidaAndProducto_IdProducto(idComida, dto.getIdProducto())) {
            throw new BusinessException(
                    "El producto " + dto.getIdProducto() + " ya está en la receta de la comida " + idComida,
                    HttpStatus.CONFLICT,
                    ErrorCode.VALIDACION);
        }
    }

    public void validarPut(int idComida, int idProductoReceta, ProductoRecetaRequestDTO dto) {
        validarComidaYProducto(idComida, dto.getIdProducto());
        // Si se cambia el producto, el nuevo no puede ya existir en OTRA fila de la misma receta.
        Optional<ProductoReceta> colisionOpt = productoRecetaRepository
                .findByComida_IdComidaAndProducto_IdProducto(idComida, dto.getIdProducto());
        if (colisionOpt.isPresent() && !colisionOpt.get().getIdProductoReceta().equals(idProductoReceta)) {
            throw new BusinessException(
                    "El producto " + dto.getIdProducto() + " ya está en otra fila de la receta",
                    HttpStatus.CONFLICT,
                    ErrorCode.VALIDACION);
        }
    }

    private void validarComidaYProducto(int idComida, int idProducto) {
        if (!comidaRepository.existsById(idComida)) {
            throw new BusinessException(
                    "La comida con id " + idComida + " no existe",
                    HttpStatus.NOT_FOUND);
        }
        if (!productoRepository.existsById(idProducto)) {
            throw new BusinessException(
                    "El producto con id " + idProducto + " no existe",
                    HttpStatus.NOT_FOUND);
        }
    }
}
