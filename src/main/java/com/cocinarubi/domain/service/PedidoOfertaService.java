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
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final OfertaMatcherService ofertaMatcher;
    private final OfertaAplicadorService ofertaAplicador;
    private final PedidoRepository pedidoRepository;

    public PedidoOfertaService(OfertaRepository ofertaRepository,
                                OfertaMatcherService ofertaMatcher,
                                OfertaAplicadorService ofertaAplicador,
                                PedidoRepository pedidoRepository) {
        this.ofertaRepository = ofertaRepository;
        this.ofertaMatcher = ofertaMatcher;
        this.ofertaAplicador = ofertaAplicador;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Detecta o valida las ofertas según el canal.
     * WEB: auto-detección greedy sobre las ofertas activas filtradas por tipoPedido.
     * COCINA: aplica las ofertas indicadas explícitamente en el DTO y valida el matching.
     */
    public List<OfertaAplicacion> detectar(Pedido pedido, PedidoRequestDTO dto) {
        if (PedidoCreadoDesde.COCINA.equals(dto.getPedidoCreadoDesde())
                && !dto.getOfertas().isEmpty()) {
            List<Oferta> ofertasOrdenadas = dto.getOfertas().stream()
                    .map(o -> ofertaRepository.findByIdConLineas(o.getIdOferta())
                            .orElseThrow(() -> new BusinessException(
                                    "Oferta #" + o.getIdOferta() + " no encontrada",
                                    HttpStatus.NOT_FOUND)))
                    .toList();
            return ofertaMatcher.aplicarCocina(
                    dto.getOfertas(), ofertasOrdenadas,
                    pedido.getComidasPedido(), pedido.getProductosCocina(),
                    pedido.getBasicosPedido(), pedido.getCombosPedido());
        }
        List<Oferta> activas = ofertaRepository.findActivasConLineas().stream()
                .filter(o -> o.estaDisponiblePara(pedido.getTipoPedido()))
                .toList();
        if (activas.isEmpty()) return List.of();
        return ofertaMatcher.detectar(
                pedido.getComidasPedido(), pedido.getProductosCocina(),
                pedido.getBasicosPedido(), pedido.getCombosPedido(),
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
