package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.dao.FavoritoClienteRepository;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.FavoritoClienteRequestDTO;
import com.cocinarubi.presentation.strategy.ConfirmationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FavoritoClienteConfirmationImp implements ConfirmationStrategy<FavoritoClienteRequestDTO> {

    private final FavoritoClienteRepository favoritoClienteRepository;

    public FavoritoClienteConfirmationImp(FavoritoClienteRepository favoritoClienteRepository) {
        this.favoritoClienteRepository = favoritoClienteRepository;
    }

    @Override
    public void validarPost(FavoritoClienteRequestDTO dto) {
        boolean yaExiste = favoritoClienteRepository
                .existsByCliente_SessionTokenAndIdProductoAndTipoCatalogoProducto(
                        dto.getSessionToken(), dto.getIdProducto(), dto.getTipoCatalogoProducto());
        if (yaExiste) {
            throw new BusinessException(
                    "El producto ya está marcado como favorito para este cliente",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
    }
}
