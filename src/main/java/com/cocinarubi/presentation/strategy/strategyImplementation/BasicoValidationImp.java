package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.BasicoRequestDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class BasicoValidationImp implements ValidationStrategy<BasicoRequestDTO> {

    private final ComidaRepository comidaRepository;

    public BasicoValidationImp(ComidaRepository comidaRepository) {
        this.comidaRepository = comidaRepository;
    }

    @Override
    public void validarPost(BasicoRequestDTO dto) {
        if (!comidaRepository.existsById(dto.getIdComida())) {
            throw new BusinessException(
                    "La comida con id " + dto.getIdComida() + " no existe",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDACION);
        }
    }
}
