package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.TipoMedida;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.ProductoRequestDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Reglas de negocio del catálogo de productos. Jakarta Validation ya cubre
 * nulos y rangos básicos; aquí se refuerzan reglas condicionales.
 *
 * Capa: Strategy — validación por tipo DTO.
 */
@Component
public class ProductoValidationImp implements ValidationStrategy<ProductoRequestDTO> {

    @Override
    public void validarPost(ProductoRequestDTO dto) {
        if (dto.getTipoMedida() == TipoMedida.PIEZA_VARIABLE) {
            BigDecimal peso = dto.getPesoPromedioPieza();
            if (peso == null || peso.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(
                        "Un producto de pieza variable requiere peso_promedio_pieza > 0",
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.VALIDACION);
            }
        }
    }
}
