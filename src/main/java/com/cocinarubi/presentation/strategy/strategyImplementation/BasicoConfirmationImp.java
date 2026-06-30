package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.BasicoRequestDTO;
import com.cocinarubi.presentation.strategy.ConfirmationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class BasicoConfirmationImp implements ConfirmationStrategy<BasicoRequestDTO> {

    private final ComidaRepository comidaRepository;
    private final ComplementoRepository complementoRepository;

    public BasicoConfirmationImp(ComidaRepository comidaRepository,
                                  ComplementoRepository complementoRepository) {
        this.comidaRepository = comidaRepository;
        this.complementoRepository = complementoRepository;
    }

    @Override
    public void validarPost(BasicoRequestDTO dto) {
        Comida comida = comidaRepository.findById(dto.getIdComida())
                .orElseThrow(() -> new BusinessException(
                        "La comida con id " + dto.getIdComida() + " no existe",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));

        if (comida.getEstatus() != Estatus.DISPONIBLE) {
            throw new BusinessException(
                    "La comida '" + comida.getNombreComida() + "' no está disponible",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }

        for (Integer idComplemento : dto.getIdComplementos()) {
            Complemento complemento = complementoRepository.findById(idComplemento)
                    .orElseThrow(() -> new BusinessException(
                            "El complemento con id " + idComplemento + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));

            if (complemento.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El complemento '" + complemento.getNombreComplemento() + "' no está disponible",
                        HttpStatus.CONFLICT, ErrorCode.VALIDACION);
            }
        }
    }
}
