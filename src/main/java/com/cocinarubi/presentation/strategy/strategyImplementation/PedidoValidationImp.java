package com.cocinarubi.presentation.strategy.strategyImplementation;

import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.BasicoPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComidaPedidoDTO;
import com.cocinarubi.presentation.dto.request.ComplementoPedidoDTO;
import com.cocinarubi.presentation.dto.request.DesayunoPedidoDTO;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.request.ProductoCocinaPedidoDTO;
import com.cocinarubi.presentation.strategy.ValidationStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PedidoValidationImp implements ValidationStrategy<PedidoRequestDTO> {

    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final BasicoRepository basicoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final ComplementoRepository complementoRepository;
    private final RutaRepository rutaRepository;

    public PedidoValidationImp(ComidaRepository comidaRepository,
                               DesayunoRepository desayunoRepository,
                               BasicoRepository basicoRepository,
                               ProductoCocinaRepository productoCocinaRepository,
                               ComplementoRepository complementoRepository,
                               RutaRepository rutaRepository) {
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.basicoRepository = basicoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.complementoRepository = complementoRepository;
        this.rutaRepository = rutaRepository;
    }

    @Override
    public void validarPost(PedidoRequestDTO dto) {
        validarAlMenosUnProducto(dto);
        validarConsistenciaDomicilio(dto);
        validarLineasComida(dto);
        validarLineasDesayuno(dto);
        validarLineasBasico(dto);
        validarLineasProductoCocina(dto);
        validarDomicilio(dto);
    }

    /** RF-025: el pedido debe tener al menos una línea de algún tipo. */
    private void validarAlMenosUnProducto(PedidoRequestDTO dto) {
        boolean tieneAlgo = !dto.getComidas().isEmpty()
                || !dto.getDesayunos().isEmpty()
                || !dto.getBasicos().isEmpty()
                || !dto.getProductosCocina().isEmpty();
        if (!tieneAlgo) {
            throw new BusinessException(
                    "El pedido debe incluir al menos un producto",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    /** Verifica coherencia entre tipoPedido y la sección "domicilio". */
    private void validarConsistenciaDomicilio(PedidoRequestDTO dto) {
        if (dto.getTipoPedido() == TipoPedido.DOMICILIO && dto.getDomicilio() == null) {
            throw new BusinessException(
                    "Un pedido a domicilio requiere los datos de entrega (campo 'domicilio')",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
        if (dto.getTipoPedido() != TipoPedido.DOMICILIO && dto.getDomicilio() != null) {
            throw new BusinessException(
                    "Solo los pedidos a domicilio pueden incluir datos de entrega",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }

    private void validarLineasComida(PedidoRequestDTO dto) {
        for (ComidaPedidoDTO linea : dto.getComidas()) {
            if (!comidaRepository.existsById(linea.getIdComida())) {
                throw new BusinessException(
                        "La comida con id " + linea.getIdComida() + " no existe",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
            for (ComplementoPedidoDTO comp : linea.getComplementos()) {
                if (!complementoRepository.existsById(comp.getIdComplemento())) {
                    throw new BusinessException(
                            "El complemento con id " + comp.getIdComplemento() + " no existe",
                            HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                }
            }
        }
    }

    private void validarLineasDesayuno(PedidoRequestDTO dto) {
        for (DesayunoPedidoDTO linea : dto.getDesayunos()) {
            if (!desayunoRepository.existsById(linea.getIdDesayuno())) {
                throw new BusinessException(
                        "El desayuno con id " + linea.getIdDesayuno() + " no existe",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarLineasBasico(PedidoRequestDTO dto) {
        for (BasicoPedidoDTO linea : dto.getBasicos()) {
            if (!basicoRepository.existsById(linea.getIdBasico())) {
                throw new BusinessException(
                        "El básico con id " + linea.getIdBasico() + " no existe",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarLineasProductoCocina(PedidoRequestDTO dto) {
        for (ProductoCocinaPedidoDTO linea : dto.getProductosCocina()) {
            if (!productoCocinaRepository.existsById(linea.getIdProductoCocina())) {
                throw new BusinessException(
                        "El producto de cocina con id " + linea.getIdProductoCocina() + " no existe",
                        HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
            }
        }
    }

    private void validarDomicilio(PedidoRequestDTO dto) {
        if (dto.getDomicilio() == null) return;
        if (!rutaRepository.existsById(dto.getDomicilio().getIdRuta())) {
            throw new BusinessException(
                    "La ruta con id " + dto.getDomicilio().getIdRuta() + " no existe",
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
    }
}
