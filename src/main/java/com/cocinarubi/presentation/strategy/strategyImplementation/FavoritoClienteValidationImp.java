package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.FavoritoClienteRequestDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FavoritoClienteValidationImp implements ValidationStrategy<FavoritoClienteRequestDTO> {

    private final ClienteRepository clienteRepository;
    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final BasicoRepository basicoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComplementoRepository complementoRepository;

    public FavoritoClienteValidationImp(ClienteRepository clienteRepository,
                                        ComidaRepository comidaRepository,
                                        DesayunoRepository desayunoRepository,
                                        BasicoRepository basicoRepository,
                                        ProductoCocinaRepository productoCocinaRepository,
                                        ComplementoRepository complementoRepository) {
        this.clienteRepository = clienteRepository;
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.basicoRepository = basicoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.complementoRepository = complementoRepository;
    }

    @Override
    public void validarPost(FavoritoClienteRequestDTO dto) {
        if (!clienteRepository.existsBySessionToken(dto.getSessionToken())) {
            throw new BusinessException(
                    "El cliente con sessionToken '" + dto.getSessionToken() + "' no existe",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
        validarExistenciaProducto(dto.getTipoCatalogoProducto(), dto.getIdProducto());
    }

    /** Validación polimórfica: la tabla destino depende del TipoCatalogoProducto. */
    private void validarExistenciaProducto(TipoCatalogoProducto tipo, Integer idProducto) {
        boolean existe = switch (tipo) {
            case COMIDA -> comidaRepository.existsById(idProducto);
            case COMPLEMENTO -> complementoRepository.existsById(idProducto);
            case DESAYUNO -> desayunoRepository.existsById(idProducto);
            case BASICO -> basicoRepository.existsById(idProducto);
            case SNACK, CHAROLA, BEBIDA, POSTRE -> productoCocinaRepository.existsById(idProducto);
        };
        if (!existe) {
            throw new BusinessException(
                    "El producto de tipo " + tipo + " con id " + idProducto + " no existe",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }
}
