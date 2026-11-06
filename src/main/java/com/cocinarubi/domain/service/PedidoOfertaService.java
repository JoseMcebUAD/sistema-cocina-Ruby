package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoEntidadOferta;
import com.cocinarubi.dao.OfertaRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.domain.entity.ComidaPedido;
import com.cocinarubi.domain.entity.Oferta;
import com.cocinarubi.domain.entity.Pedido;
import com.cocinarubi.domain.model.OfertaAplicacion;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cocinarubi.Constants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orquesta la detección, validación y persistencia de {@link OfertaAplicacion} dentro
 * del ciclo de vida de un {@link Pedido}.
 *
 * <p>Concentra la lógica de ofertas extraída de {@link PedidoService}, reduciendo su
 * acoplamiento con {@link OfertaMatcherService} y {@link OfertaAplicadorService}.</p>
 *
 * <p>Capa: Service — coordinación de ofertas en pedidos.</p>
 */
@Service
public class PedidoOfertaService {

    private final OfertaRepository ofertaRepository;
    private final OfertaService ofertaService;
    private final OfertaMatcherService ofertaMatcher;
    private final OfertaAplicadorService ofertaAplicador;
    private final PedidoRepository pedidoRepository;

    public PedidoOfertaService(OfertaRepository ofertaRepository,
                                OfertaService ofertaService,
                                OfertaMatcherService ofertaMatcher,
                                OfertaAplicadorService ofertaAplicador,
                                PedidoRepository pedidoRepository) {
        this.ofertaRepository = ofertaRepository;
        this.ofertaService = ofertaService;
        this.ofertaMatcher = ofertaMatcher;
        this.ofertaAplicador = ofertaAplicador;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Despacha la detección de ofertas al canal correcto.
     * COCINA: solo aplica las ofertas que el operador indicó explícitamente; lista vacía = sin ofertas.
     * WEB: auto-detección greedy sobre todas las ofertas activas del tipoPedido.
     */
    public List<OfertaAplicacion> detectar(Pedido pedido, PedidoRequestDTO dto) {
        if (PedidoCreadoDesde.COCINA.equals(dto.getPedidoCreadoDesde())) {
            return detectarCocina(pedido, dto);
        }
        return detectarWeb(pedido);
    }

    /**
     * Canal COCINA — el operador tiene control total.
     * Si no seleccionó ninguna oferta, devuelve lista vacía sin intentar auto-detectar.
     * Valida vigencia como defensa en profundidad: la capa de validación ya lo verificó,
     * pero este guard protege si en algún momento esa validación cambia o se desactiva.
     */
    private List<OfertaAplicacion> detectarCocina(Pedido pedido, PedidoRequestDTO dto) {
        if (dto.getOfertas().isEmpty()) return List.of();

        LocalDate hoy = LocalDate.now(Constants.ZONA_MERIDA);
        List<Oferta> ofertasOrdenadas = dto.getOfertas().stream()
                .map(o -> {
                    Oferta oferta = ofertaRepository.findByIdConLineas(o.getIdOferta())
                            .orElseThrow(() -> new BusinessException(
                                    "Oferta #" + o.getIdOferta() + " no encontrada",
                                    HttpStatus.NOT_FOUND));
                    if (!oferta.estaVigente(hoy)) {
                        throw new BusinessException(
                                "La oferta '" + oferta.getNombre() + "' no está vigente en la fecha actual",
                                HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
                    }
                    return oferta;
                })
                .toList();
        return ofertaMatcher.aplicarCocina(
                dto.getOfertas(),
                ofertasOrdenadas,
                pedido.getComidasPedido(),
                pedido.getProductosCocina(),
                pedido.getBasicosPedido(),
                pedido.getCombosPedido());
    }

    /**
     * Canal WEB — auto-detección greedy.
     * Carga las ofertas activas y vigentes (cacheadas en Redis con clave fecha) y aplica las que coincidan con el tipoPedido.
     */
    private List<OfertaAplicacion> detectarWeb(Pedido pedido) {
        LocalDate hoy = LocalDate.now(Constants.ZONA_MERIDA);
        // ofertaService.findActivasEntidades(): cargadas con FETCH JOIN, cacheadas en Redis con clave fecha+zona
        List<Oferta> activas = ofertaService.findActivasEntidades(hoy).stream()
                .filter(o -> o.estaDisponiblePara(pedido.getTipoPedido()))
                .toList();
        if (activas.isEmpty()) return List.of();
        return ofertaMatcher.detectar(
                pedido.getComidasPedido(),
                pedido.getProductosCocina(),
                pedido.getBasicosPedido(),
                pedido.getCombosPedido(),
                activas);
    }

    /**
     * Phase 2: persiste OfertaPedido/OfertaPedidoLinea usando los IDs reales del pedido guardado.
     * Devuelve el mismo {@code guardado} sin modificar si no hay aplicaciones.
     */
    public Pedido guardar(Pedido guardado, List<OfertaAplicacion> aplicaciones) {
        if (aplicaciones.isEmpty()) return guardado;
        
        ofertaAplicador.guardar(guardado, aplicaciones);
        return pedidoRepository.save(guardado);
    }

    public Set<ComidaPedido> extraerComidasConsumidas(List<OfertaAplicacion> aplicaciones) {
        return aplicaciones.stream()
                .flatMap(a -> a.getLineas().stream())
                .filter(lc -> lc.getTipoEntidad() == TipoEntidadOferta.COMIDA)
                .map(lc -> (ComidaPedido) lc.getLineaEntity())
                .collect(Collectors.toSet());
    }

    public BigDecimal sumarAhorros(List<OfertaAplicacion> aplicaciones) {
        return aplicaciones.stream()
                .map(OfertaAplicacion::getAhorro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
