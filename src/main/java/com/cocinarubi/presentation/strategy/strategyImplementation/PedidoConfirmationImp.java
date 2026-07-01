package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.domain.entity.ProductoCocina;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.BasicoPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.DesayunoPedidoDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.request.ProductoCocinaPedidoDTO;
import com.cocinarubi.presentation.strategy.ConfirmationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class PedidoConfirmationImp implements ConfirmationStrategy<PedidoRequestDTO> {

    private static final LocalTime LIMITE_DESAYUNO = LocalTime.of(11, 0);

    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final BasicoRepository basicoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComplementoRepository complementoRepository;

    public PedidoConfirmationImp(ComidaRepository comidaRepository,
                                 DesayunoRepository desayunoRepository,
                                 BasicoRepository basicoRepository,
                                 ProductoCocinaRepository productoCocinaRepository,
                                 ComplementoRepository complementoRepository) {
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.basicoRepository = basicoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.complementoRepository = complementoRepository;
    }

    @Override
    public void validarPost(PedidoRequestDTO dto) {
        validarHorarioDesayuno(dto);
        validarDisponibilidadComidas(dto);
        validarDisponibilidadDesayunos(dto);
        validarDisponibilidadBasicos(dto);
        validarDisponibilidadProductosCocina(dto);
    }

    /** RF-026: los desayunos solo se pueden ordenar antes de las 11:00 h. */
    private void validarHorarioDesayuno(PedidoRequestDTO dto) {
        if (dto.getDesayunos().isEmpty()) return;
        if (LocalTime.now().isAfter(LIMITE_DESAYUNO)) {
            throw new BusinessException(
                    "Los desayunos solo se pueden ordenar antes de las 11:00 h",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
    }

    private void validarDisponibilidadComidas(PedidoRequestDTO dto) {
        for (ComidaPedidoDTO linea : dto.getComidas()) {
            Comida comida = comidaRepository.findById(linea.getIdComida())
                    .orElseThrow(() -> new BusinessException(
                            "La comida con id " + linea.getIdComida() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (comida.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "La comida '" + comida.getNombreComida() + "' no está disponible",
                        HttpStatus.CONFLICT, ErrorCode.VALIDACION);
            }
            for (Integer idComp : linea.getIdComplementos()) {
                Complemento c = complementoRepository.findById(idComp)
                        .orElseThrow(() -> new BusinessException(
                                "El complemento con id " + idComp + " no existe",
                                HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
                if (c.getEstatus() != Estatus.DISPONIBLE) {
                    throw new BusinessException(
                            "El complemento '" + c.getNombreComplemento() + "' no está disponible",
                            HttpStatus.CONFLICT, ErrorCode.VALIDACION);
                }
            }
        }
    }

    private void validarDisponibilidadDesayunos(PedidoRequestDTO dto) {
        for (DesayunoPedidoDTO linea : dto.getDesayunos()) {
            Desayuno d = desayunoRepository.findById(linea.getIdDesayuno())
                    .orElseThrow(() -> new BusinessException(
                            "El desayuno con id " + linea.getIdDesayuno() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (d.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El desayuno '" + d.getNombreDesayuno() + "' no está disponible",
                        HttpStatus.CONFLICT, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarDisponibilidadBasicos(PedidoRequestDTO dto) {
        for (BasicoPedidoDTO linea : dto.getBasicos()) {
            Basico b = basicoRepository.findById(linea.getIdBasico())
                    .orElseThrow(() -> new BusinessException(
                            "El básico con id " + linea.getIdBasico() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (b.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El básico no está disponible",
                        HttpStatus.CONFLICT, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarDisponibilidadProductosCocina(PedidoRequestDTO dto) {
        for (ProductoCocinaPedidoDTO linea : dto.getProductosCocina()) {
            ProductoCocina p = productoCocinaRepository.findById(linea.getIdProductoCocina())
                    .orElseThrow(() -> new BusinessException(
                            "El producto de cocina con id " + linea.getIdProductoCocina() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION));
            if (p.getEstatus() != Estatus.DISPONIBLE) {
                throw new BusinessException(
                        "El producto '" + p.getNombreProducto() + "' no está disponible",
                        HttpStatus.CONFLICT, ErrorCode.VALIDACION);
            }
        }
    }
}
