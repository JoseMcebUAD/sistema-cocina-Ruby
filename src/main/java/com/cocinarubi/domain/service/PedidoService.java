package com.cocinarubi.domain.service;

import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.TarifaEspecialRepository;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.TarifaEspecial;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final TarifaEspecialRepository tarifaEspecialRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoValidationImp pedidoValidation,
                         PedidoConfirmationImp pedidoConfirmation,
                         PedidoMapper pedidoMapper,
                         CatalogoPedidoService catalogoPedido,
                         TarifaEspecialRepository tarifaEspecialRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoValidation = pedidoValidation;
        this.pedidoConfirmation = pedidoConfirmation;
        this.pedidoMapper = pedidoMapper;
        this.catalogoPedido = catalogoPedido;
        this.tarifaEspecialRepository = tarifaEspecialRepository;
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
        Pedido pedido = construirPedido(dto);
        List<String> mensajesTarifas = poblarLineasYPrecio(pedido, dto);
        PedidoResponseDTO response = pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
        if (!mensajesTarifas.isEmpty()) {
            response.setTarifasAplicadas(mensajesTarifas);
        }
        return response;
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

        List<String> mensajesTarifas = poblarLineasYPrecio(existente, dto);
        PedidoResponseDTO response = pedidoMapper.toResponseDTO(pedidoRepository.save(existente));
        if (!mensajesTarifas.isEmpty()) {
            response.setTarifasAplicadas(mensajesTarifas);
        }
        return response;
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

    private Pedido construirPedido(PedidoRequestDTO dto) {
        return Pedido.builder()
                .metodoPagoPrincipal(dto.getMetodoPagoPrincipal())
                .metodoPagoSecundario(dto.getMetodoPagoSecundario())
                .tipoPedido(dto.getTipoPedido())
                .pedidoCreadoDesde(dto.getPedidoCreadoDesde())
                .pagoCliente(dto.getPagoCliente())
                .uuidCliente(dto.getUuidCliente())
                .fechaExpedicionPedido(LocalDateTime.now())
                .impreso(false)
                .build();
    }

    /**
     * Agrega las líneas de catálogo al pedido, resuelve el tipo de entrega,
     * calcula el precio final y aplica tarifas activas.
     * Devuelve los mensajes de tarifas aplicadas (vacío si no había ninguna).
     */
    private List<String> poblarLineasYPrecio(Pedido pedido, PedidoRequestDTO dto) {
        catalogoPedido.agregarComidas(pedido, dto.getComidas());
        catalogoPedido.agregarDesayunos(pedido, dto.getDesayunos());
        catalogoPedido.agregarBasicos(pedido, dto.getBasicos());
        catalogoPedido.agregarProductosCocina(pedido, dto.getProductosCocina());
        catalogoPedido.handleTipoPedido(pedido, dto);
        pedido.setPrecioFinalOrden(catalogoPedido.calcularTotal(pedido));
        return aplicarTarifasActivas(pedido);
    }

    /**
     * Suma al {@code precioFinalOrden} las tarifas especiales activas, pero solo cuando el
     * pedido es de tipo DOMICILIO (tiene {@code pedidoDomicilio} o {@code pedidoDomicilioCocina}).
     * El total se persiste en la entidad de domicilio correspondiente para trazabilidad.
     * Devuelve los mensajes descriptivos de cada tarifa aplicada; vacío si no aplica ninguna.
     */
    private List<String> aplicarTarifasActivas(Pedido pedido) {
        boolean esDomicilio = pedido.getPedidoDomicilio() != null
                || pedido.getPedidoDomicilioCocina() != null;
        if (!esDomicilio) return List.of();

        List<TarifaEspecial> activas = tarifaEspecialRepository.findByIsActiveTrue();
        if (activas.isEmpty()) return List.of();

        BigDecimal totalTarifas = BigDecimal.ZERO;
        List<String> mensajes = new java.util.ArrayList<>();
        for (TarifaEspecial tarifa : activas) {
            totalTarifas = totalTarifas.add(tarifa.getTarifa());
            mensajes.add("Se han agregado $" + tarifa.getTarifa().toPlainString()
                    + " de la tarifa " + tarifa.getNombreTarifa());
        }

        if (pedido.getPedidoDomicilio() != null) {
            pedido.getPedidoDomicilio().setTarifasEspeciales(totalTarifas);
        } else {
            pedido.getPedidoDomicilioCocina().setTarifasEspeciales(totalTarifas);
        }
        pedido.setPrecioFinalOrden(pedido.getPrecioFinalOrden().add(totalTarifas));
        return mensajes;
    }

    private Pedido findEntityById(int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Pedido no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }
}
