package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.TipoProducto;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Servicio de dominio para la entidad ProductoCocina. Centraliza la lógica de
 * recuperación y verificación de existencia por subtipo (SNACK, CHAROLA, BEBIDA).
 */
@Service
public class ProductoCocinaService {

    private final ProductoCocinaRepository productoCocinaRepository;

    public ProductoCocinaService(ProductoCocinaRepository productoCocinaRepository) {
        this.productoCocinaRepository = productoCocinaRepository;
    }

    public ProductoCocina findById(int id) {
        // Lanza 404 si no existe el producto; usado internamente por otros servicios
        return productoCocinaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Producto de cocina no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public boolean existsByIdAndTipo(Integer id, TipoProducto tipo) {
        // Delega al repositorio la validación combinada id + tipoProducto
        return productoCocinaRepository.existsByIdProductoCocinaAndTipoProducto(id, tipo);
    }
}
