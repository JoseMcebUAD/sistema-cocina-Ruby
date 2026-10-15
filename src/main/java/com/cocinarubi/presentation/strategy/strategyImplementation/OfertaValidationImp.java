package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.ModoMatchOferta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.OfertaLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.OfertaRequestDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Valida la integridad estructural de un {@link OfertaRequestDTO} antes de persistirlo.
 *
 * <p>Contratos validados por línea:
 * <ul>
 *   <li>{@code cantidad >= 1}.</li>
 *   <li>{@code tipoComidaFiltro} obligatorio para {@code COMIDA_POR_TIPO}.</li>
 *   <li>{@code idReferencia} obligatorio para los modos que lo requieren.</li>
 * </ul>
 *
 * <p>Capa: Strategy — validación de negocio.
 */
@Component
public class OfertaValidationImp implements ValidationStrategy<OfertaRequestDTO> {

    @Override
    public void validarPost(OfertaRequestDTO dto) {
        for (OfertaLineaRequestDTO linea : dto.getLineas()) {
            validarLinea(linea);
        }
    }

    private void validarLinea(OfertaLineaRequestDTO linea) {
        if (linea.getCantidad() < 1) {
            throw new BusinessException("La cantidad de cada línea debe ser al menos 1",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
        ModoMatchOferta modo = linea.getModoMatch();
        switch (modo) {
            case COMIDA_POR_TIPO -> {
                if (linea.getTipoComidaFiltro() == null) {
                    throw new BusinessException(
                            "El modo COMIDA_POR_TIPO requiere el campo 'tipoComidaFiltro'",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }
            }
            case COMIDA_ESPECIFICA,
                 PRODUCTO_ESPECIFICO,
                 PRODUCTO_POR_CATEGORIA,
                 PRODUCTO_POR_SUBCATEGORIA,
                 BASICO_ESPECIFICO,
                 COMBO_ESPECIFICO -> {
                if (linea.getIdReferencia() == null) {
                    throw new BusinessException(
                            "El modo " + modo + " requiere el campo 'idReferencia'",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }
            }
            default -> { /* PRODUCTO_GENERAL, BASICO_GENERAL, COMBO_GENERAL no requieren idReferencia */ }
        }
    }
}
