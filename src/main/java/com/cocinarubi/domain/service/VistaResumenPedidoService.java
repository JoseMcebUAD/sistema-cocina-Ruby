package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository.VistaResumenMetricasProjection;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoConMetricasResponseDTO;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lógica de negocio para la vista consolidada de pedidos y sus métricas.
 * Capa: Service — coordina VistaResumenPedidoRepository (filtros planos + métricas)
 * y PedidoRepository (detalle completo para /metricas).
 */
@Service
public class VistaResumenPedidoService {

    private final VistaResumenPedidoRepository repository;
    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;

    public VistaResumenPedidoService(VistaResumenPedidoRepository repository,
                                     PedidoRepository pedidoRepository,
                                     PedidoMapper pedidoMapper) {
        this.repository = repository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    public Page<VistaResumenPedidoResponseDTO> findVista(LocalDateTime desde,
                                                        LocalDateTime hasta,
                                                        DBConstants.TipoPedido tipoPedido,
                                                        DBConstants.PedidoCreadoDesde creadoDesde,
                                                        Pageable pageable) {
        validarRango(desde, hasta);
        return repository.findVistaConFiltros(desde, hasta, tipoPedido, creadoDesde, pageable);
    }

    /**
     * Devuelve la página de pedidos en formato completo (PedidoResponseDTO con items y domicilio)
     * junto con las métricas agregadas del rango filtrado.
     * Requiere transacción para resolver las colecciones lazy de Pedido.
     */
    @Transactional(readOnly = true)
    public VistaResumenPedidoConMetricasResponseDTO findVistaConMetricas(LocalDateTime desde,
                                                                        LocalDateTime hasta,
                                                                        DBConstants.TipoPedido tipoPedido,
                                                                        DBConstants.PedidoCreadoDesde creadoDesde,
                                                                        Pageable pageable) {
        validarRango(desde, hasta);

        // PedidoRepository: pedidos completos con relaciones lazy resueltas dentro de la transacción
        Page<PedidoResponseDTO> pedidos = pedidoRepository
                .findByFiltros(desde, hasta, tipoPedido, creadoDesde, pageable)
                .map(pedidoMapper::toResponseDTO);

        VistaResumenMetricasProjection m = repository.findMetricasConFiltros(
                desde, hasta, tipoPedido, creadoDesde);

        return VistaResumenPedidoConMetricasResponseDTO.builder()
                .pedidos(pedidos)
                .cantidadTotal(pedidos.getTotalElements())
                .cantidadImpresos(nullSafeLong(m.getCantidadImpresos()))
                .cantidadNoImpresos(nullSafeLong(m.getCantidadNoImpresos()))
                .ingresoTotal(nullSafeBigDecimal(m.getIngresoTotal()))
                .ingresoEfectivo(nullSafeBigDecimal(m.getIngresoEfectivo()))
                .ingresoTransferencia(nullSafeBigDecimal(m.getIngresoTransferencia()))
                .ingresoTarjeta(nullSafeBigDecimal(m.getIngresoTarjeta()))
                .build();
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private long nullSafeLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal nullSafeBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
