package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.ProductoCocinaRequestDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductoCocinaValidationImp implements ValidationStrategy<ProductoCocinaRequestDTO> {

    @Override
    public void validarPost(ProductoCocinaRequestDTO dto) {
        if (dto.getPrecioDomicilio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "El precio a domicilio debe ser mayor a cero",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
        if (dto.getPrecioNormal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "El precio normal debe ser mayor a cero",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }
}
