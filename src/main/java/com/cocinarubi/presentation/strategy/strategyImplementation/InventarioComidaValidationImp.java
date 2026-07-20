package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.InventarioComidaRequestDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InventarioComidaValidationImp implements ValidationStrategy<InventarioComidaRequestDTO> {

    private final ComidaRepository comidaRepository;

    public InventarioComidaValidationImp(ComidaRepository comidaRepository) {
        this.comidaRepository = comidaRepository;
    }

    @Override
    public void validarPost(InventarioComidaRequestDTO dto) {
        if (!comidaRepository.existsById(dto.getIdComida())) {
            throw new BusinessException(
                    "La comida con id " + dto.getIdComida() + " no existe",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
        // CHECK chk_inventario_no_vacio: al menos cantidad debe estar presente
        if (dto.getCantidad() == null) {
            throw new BusinessException(
                    "La cantidad es obligatoria para registrar el consumo",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }
}
