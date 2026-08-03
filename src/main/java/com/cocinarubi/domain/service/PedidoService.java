package com.cocinarubi.domain.service;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.TarifaEspecialRepository;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.entity.TarifaEspecial;
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

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final TarifaEspecialRepository tarifaEspecialRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoValidationImp pedidoValidation,
                         PedidoConfirmationImp pedidoConfirmation,
                         PedidoMapper pedidoMapper,
                         CatalogoPedidoService catalogoPedido,
                         ApplicationEventPublisher eventPublisher,
                         TarifaEspecialRepository tarifaEspecialRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoValidation = pedidoValidation;
        this.pedidoConfirmation = pedidoConfirmation;
        this.pedidoMapper = pedidoMapper;
        this.catalogoPedido = catalogoPedido;
        this.eventPublisher = eventPublisher;
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
        if (PedidoCreadoDesde.WEB.equals(dto.getPedidoCreadoDesde())) {
            eventPublisher.publishEvent(new PedidoWebActualizadoEvent(this));
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
        existente.setComentario(dto.getComentario());

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

    /**
     * Intenta marcar el pedido como impreso de forma atómica (0→1).
     * Si dos solicitudes concurrentes llegan al mismo tiempo, solo una obtendrá
     * rowsAffected=1; la otra verá rowsAffected=0 porque el WHERE impreso=false ya no se cumple.
     * Retorna true si se otorgó el cambio, false si ya estaba impreso.
     */
    @Transactional
    public boolean marcarImpreso(int id) {
        // Verificamos que el pedido existe antes del UPDATE para poder lanzar 404 si no existe
        Pedido pedido = findEntityById(id);
        int filasAfectadas = pedidoRepository.marcarImpresoSiNoImpreso(id);
        boolean otorgado = filasAfectadas > 0;
        if (otorgado && PedidoCreadoDesde.WEB.equals(pedido.getPedidoCreadoDesde())) {
            eventPublisher.publishEvent(new PedidoWebActualizadoEvent(this));
        }
        return otorgado;
    }

    @Transactional
    public void marcarPagado(int id) {
        Pedido pedido = findEntityById(id);
        pedido.setPagado(true);
        pedidoRepository.save(pedido);
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

    //contar pedidos web sin imprimir del día actual para el contador del frontEnd

    @Transactional(readOnly = true)
    public long contarWebSinImprimir() {
        LocalDateTime inicioDia = LocalDate.now(Constants.ZONA_MERIDA).atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1).minusNanos(1);
        return pedidoRepository.countByPedidoCreadoDesdeAndImpresoFalseAndFechaExpedicionPedidoBetween(
                PedidoCreadoDesde.WEB, inicioDia, finDia);
    }

    private Pedido construirPedido(PedidoRequestDTO dto) {
        return Pedido.builder()
                .metodoPagoPrincipal(dto.getMetodoPagoPrincipal())
                .metodoPagoSecundario(dto.getMetodoPagoSecundario())
                .tipoPedido(dto.getTipoPedido())
                .pedidoCreadoDesde(dto.getPedidoCreadoDesde())
                .pagoCliente(dto.getPagoCliente())
                .uuidCliente(dto.getUuidCliente())
                .fechaExpedicionPedido(LocalDateTime.now(Constants.ZONA_MERIDA))
                .impreso(false)
                .pagado(false)
                .comentario(dto.getComentario())
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
