package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.VistaResumenPedidoRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository.VistaResumenMetricasProjection;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoConMetricasResponseDTO;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VistaResumenPedidoService {

    private final VistaResumenPedidoRepository repository;

    public VistaResumenPedidoService(VistaResumenPedidoRepository repository) {
        this.repository = repository;
    }

    public Page<VistaResumenPedidoResponseDTO> findVista(LocalDateTime desde,
                                                        LocalDateTime hasta,
                                                        DBConstants.TipoPedido tipoPedido,
                                                        DBConstants.PedidoCreadoDesde creadoDesde,
                                                        Pageable pageable) {
        validarRango(desde, hasta);
        return repository.findVistaConFiltros(desde, hasta, tipoPedido, creadoDesde, pageable);
    }

    public VistaResumenPedidoConMetricasResponseDTO findVistaConMetricas(LocalDateTime desde,
                                                                        LocalDateTime hasta,
                                                                        DBConstants.TipoPedido tipoPedido,
                                                                        DBConstants.PedidoCreadoDesde creadoDesde,
                                                                        Pageable pageable) {
        validarRango(desde, hasta);
        Page<VistaResumenPedidoResponseDTO> pedidos = repository.findVistaConFiltros(
                desde, hasta, tipoPedido, creadoDesde, pageable);
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
