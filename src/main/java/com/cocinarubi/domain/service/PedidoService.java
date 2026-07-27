package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.event.ws.PedidoWebActualizadoEvent;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestiona el ciclo de vida transaccional del {@link Pedido}: creación, consulta,
 * actualización, marcado de impresión y eliminación.
 *
 * <p>Delega en {@link CatalogoPedidoService} la resolución de referencias de catálogo
 * y en {@link com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp}
 * la validación estructural. La confirmación de negocio puede omitirse mediante el flag
 * {@code saltarConfirmacion} para pedidos originados en canales internos de confianza (cocina).
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoValidationImp pedidoValidation;
    private final PedidoConfirmationImp pedidoConfirmation;
    private final PedidoMapper pedidoMapper;
    private final CatalogoPedidoService catalogoPedido;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoValidationImp pedidoValidation,
                         PedidoConfirmationImp pedidoConfirmation,
                         PedidoMapper pedidoMapper,
                         CatalogoPedidoService catalogoPedido,
                         ApplicationEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoValidation = pedidoValidation;
        this.pedidoConfirmation = pedidoConfirmation;
        this.pedidoMapper = pedidoMapper;
        this.catalogoPedido = catalogoPedido;
        this.eventPublisher = eventPublisher;
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
        //se contruye el pedido
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
        Pedido guardado = pedidoRepository.save(pedido);
        //llamamos a los sockets
        if (PedidoCreadoDesde.WEB.equals(guardado.getPedidoCreadoDesde())) {
            eventPublisher.publishEvent(new PedidoWebActualizadoEvent(this));
        }

        return pedidoMapper.toResponseDTO(guardado);
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
        existente.setPedidoDomicilioCocina(null);
        existente.setPedidoCocina(null);

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
        //llamamos a los sockets
        if (PedidoCreadoDesde.WEB.equals(pedido.getPedidoCreadoDesde())) {
            eventPublisher.publishEvent(new PedidoWebActualizadoEvent(this));
        }
    }

    @Transactional
    public void delete(int id) {
        Pedido pedido = findEntityById(id);
        boolean eraWebSinImprimir = PedidoCreadoDesde.WEB.equals(pedido.getPedidoCreadoDesde())
                && !pedido.isImpreso();
        pedidoRepository.deleteById(id);
        if (eraWebSinImprimir) {
            eventPublisher.publishEvent(new PedidoWebActualizadoEvent(this));
        }
    }

    //Encontrar los pedidos web sin imprimir para la lista del frontEnd
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findWebSinImprimir() {
        return pedidoRepository.findByPedidoCreadoDesdeAndImpresoFalse(PedidoCreadoDesde.WEB)
                .stream()
                .map(pedidoMapper::toResponseDTO)
                .toList();
    }

    //contar pedidos web sin imprimir para el contador del frontEnd

    @Transactional(readOnly = true)
    public long contarWebSinImprimir() {
        return pedidoRepository.countByPedidoCreadoDesdeAndImpresoFalse(PedidoCreadoDesde.WEB);
    }

    private Pedido findEntityById(int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Pedido no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }
}
