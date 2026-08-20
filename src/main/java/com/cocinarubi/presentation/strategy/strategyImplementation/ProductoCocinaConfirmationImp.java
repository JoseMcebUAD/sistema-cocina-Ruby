package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.ProductoCocinaRequestDTO;
import com.cocinarubi.presentation.strategy.ConfirmationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductoCocinaConfirmationImp implements ConfirmationStrategy<ProductoCocinaRequestDTO> {

    private final ProductoCocinaRepository productoCocinaRepository;

    public ProductoCocinaConfirmationImp(ProductoCocinaRepository productoCocinaRepository) {
        this.productoCocinaRepository = productoCocinaRepository;
    }

    @Override
    public void validarEliminacion(Integer id) {
        if (productoCocinaRepository.existsEnPedidos(id)) {
            throw new AdvertenciaEliminacionException(
                    "Este producto tiene pedidos relacionados. ¿Desea continuar con la eliminación?");
        }
    }

    @Override
    public void validarPost(ProductoCocinaRequestDTO dto) {
        if (dto.getEstatus() != Estatus.DISPONIBLE && dto.isDestacado()) {
            throw new BusinessException(
                    "No se puede marcar como destacado un producto que no está disponible",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
    }
}
