package com.cocinarubi.domain.service;

import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoValidationImp pedidoValidation;
    private final PedidoConfirmationImp pedidoConfirmation;
    private final PedidoMapper pedidoMapper;
    private final CatalogoPedidoService catalogoPedido;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoValidationImp pedidoValidation,
                         PedidoConfirmationImp pedidoConfirmation,
                         PedidoMapper pedidoMapper,
                         CatalogoPedidoService catalogoPedido) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoValidation = pedidoValidation;
        this.pedidoConfirmation = pedidoConfirmation;
        this.pedidoMapper = pedidoMapper;
        this.catalogoPedido = catalogoPedido;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findAll() {
        return pedidoRepository.findAll().stream()
                .map(pedidoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO findById(int id) {
        return pedidoMapper.toResponseDTO(findEntityById(id));
    }

    @Transactional
    public PedidoResponseDTO save(PedidoRequestDTO dto) {
        // Validación de estructura siempre se ejecuta; la confirmación de negocio
        // puede omitirse cuando el pedido proviene de un canal interno de confianza.
        pedidoValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            pedidoConfirmation.validarPost(dto);
        }
        Pedido pedido = Pedido.builder()
                .metodoPagoPrincipal(dto.getMetodoPagoPrincipal())
                .metodoPagoSecundario(dto.getMetodoPagoSecundario())
                .tipoPedido(dto.getTipoPedido())
                .pedidoCreadoDesde(dto.getPedidoCreadoDesde())
                .pagoCliente(dto.getPagoCliente())
                .uuidCliente(dto.getUuidCliente())
                .fechaExpedicionPedido(LocalDateTime.now())
                .impreso(false) // Todo pedido nuevo inicia sin imprimir
                .build();

        catalogoPedido.agregarComidas(pedido, dto.getComidas());
        catalogoPedido.agregarDesayunos(pedido, dto.getDesayunos());
        catalogoPedido.agregarBasicos(pedido, dto.getBasicos());
        catalogoPedido.agregarProductosCocina(pedido, dto.getProductosCocina());
        catalogoPedido.handleTipoPedido(pedido, dto);

        pedido.setPrecioFinalOrden(catalogoPedido.calcularTotal(pedido));
        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO update(int id, PedidoRequestDTO dto) {
        pedidoValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            pedidoConfirmation.validarPost(dto);
        }
        Pedido existente = findEntityById(id);
        existente.setMetodoPagoPrincipal(dto.getMetodoPagoPrincipal());
        existente.setMetodoPagoSecundario(dto.getMetodoPagoSecundario());
        existente.setTipoPedido(dto.getTipoPedido());
        existente.setPedidoCreadoDesde(dto.getPedidoCreadoDesde());
        existente.setPagoCliente(dto.getPagoCliente());
        existente.setUuidCliente(dto.getUuidCliente());

        // Se limpian las colecciones en lugar de mergear elemento a elemento
        // para evitar referencias huérfanas en las tablas de detalle.
        existente.getComidasPedido().clear();
        existente.getDesayunosPedido().clear();
        existente.getBasicosPedido().clear();
        existente.getProductosCocina().clear();
        existente.setPedidoDomicilio(null);

        catalogoPedido.agregarComidas(existente, dto.getComidas());
        catalogoPedido.agregarDesayunos(existente, dto.getDesayunos());
        catalogoPedido.agregarBasicos(existente, dto.getBasicos());
        catalogoPedido.agregarProductosCocina(existente, dto.getProductosCocina());
        catalogoPedido.handleTipoPedido(existente, dto);

        existente.setPrecioFinalOrden(catalogoPedido.calcularTotal(existente));
        return pedidoMapper.toResponseDTO(pedidoRepository.save(existente));
    }

    @Transactional
    public void marcarImpreso(int id) {
        Pedido pedido = findEntityById(id);
        pedido.setImpreso(true);
        pedidoRepository.save(pedido);
    }

    public void delete(int id) {
        if (!pedidoRepository.existsById(id)) {
            throw new BusinessException(
                    "Pedido no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        pedidoRepository.deleteById(id);
    }

    private Pedido findEntityById(int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Pedido no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }
}
