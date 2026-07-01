package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.ProductoCocinaRequestDTO;
import com.cocinarubi.presentation.strategy.ConfirmationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductoCocinaConfirmationImp implements ConfirmationStrategy<ProductoCocinaRequestDTO> {

    @Override
    public void validarPost(ProductoCocinaRequestDTO dto) {
        if (dto.getEstatus() != Estatus.DISPONIBLE && dto.isDestacado()) {
            throw new BusinessException(
                    "No se puede marcar como destacado un producto que no está disponible",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
    }
}
