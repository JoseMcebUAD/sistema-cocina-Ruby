package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.EstadisticasRepository;
import com.cocinarubi.dao.PagoRepartidorRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository.VistaResumenMetricasProjection;
import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.EstadisticaRutaItemDTO;
import com.cocinarubi.presentation.dto.response.EstadisticasVentasResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lógica de negocio para el módulo de estadísticas de ventas y rutas.
 * Capa: Service.
 */
@Service
public class EstadisticasService {

    private final VistaResumenPedidoRepository vistaResumenPedidoRepository;
    private final EstadisticasRepository estadisticasRepository;
    private final PagoRepartidorRepository pagoRepartidorRepository;

    public EstadisticasService(VistaResumenPedidoRepository vistaResumenPedidoRepository,
                               EstadisticasRepository estadisticasRepository,
                               PagoRepartidorRepository pagoRepartidorRepository) {
        this.vistaResumenPedidoRepository = vistaResumenPedidoRepository;
        this.estadisticasRepository = estadisticasRepository;
        this.pagoRepartidorRepository = pagoRepartidorRepository;
    }

    /**
     * Devuelve el resumen de ingresos del período con desglose por método de pago,
     * descuento de pagos a repartidores e ingreso de tarifas de domicilio.
     */
    @Transactional(readOnly = true)
    public EstadisticasVentasResponseDTO getVentas(LocalDateTime desde,
                                                   LocalDateTime hasta,
                                                   DBConstants.TipoPedido tipoPedido) {
        validarRango(desde, hasta);

        VistaResumenMetricasProjection metricas =
                vistaResumenPedidoRepository.findMetricasConFiltros(desde, hasta, tipoPedido, null);

        BigDecimal tarifasWeb = estadisticasRepository.findSumTarifaWeb(desde, hasta, tipoPedido);
        BigDecimal tarifasCocina = estadisticasRepository.findSumTarifaCocina(desde, hasta, tipoPedido);

        BigDecimal totalPagosRepartidor = sumarPagosRepartidor(desde, hasta);

        BigDecimal ingresoTotal = nullSafe(metricas.getIngresoTotal());

        return EstadisticasVentasResponseDTO.builder()
                .ingresoTotal(ingresoTotal)
                .ingresoEfectivo(nullSafe(metricas.getIngresoEfectivo()))
                .ingresoTransferencia(nullSafe(metricas.getIngresoTransferencia()))
                .ingresoTarjeta(nullSafe(metricas.getIngresoTarjeta()))
                .ingresoTotalRepartidor(ingresoTotal.subtract(totalPagosRepartidor))
                .ingresoTarifas(nullSafe(tarifasWeb).add(nullSafe(tarifasCocina)))
                .build();
    }

    /**
     * Devuelve los ingresos agrupados por ruta, combinando pedidos WEB y COCINA.
     * Si se filtra por metodoPago, se aplica la lógica de pago dividido para
     * atribuir el monto correcto a cada método.
     */
    @Transactional(readOnly = true)
    public List<EstadisticaRutaItemDTO> getIngresosPorRuta(LocalDateTime desde,
                                                           LocalDateTime hasta,
                                                           DBConstants.MetodoPago metodoPago) {
        validarRango(desde, hasta);

        List<EstadisticaRutaItemDTO> web = estadisticasRepository.findIngresosPorRutaWeb(desde, hasta, metodoPago);
        List<EstadisticaRutaItemDTO> cocina = estadisticasRepository.findIngresosPorRutaCocina(desde, hasta, metodoPago);

        return fusionarPorRuta(web, cocina);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Suma los pagos a repartidores en el rango de fechas indicado.
     * Maneja parámetros nulos usando fechas extremas como fallback.
     */
    private BigDecimal sumarPagosRepartidor(LocalDateTime desde, LocalDateTime hasta) {
        // findByRango usa bound superior exclusivo (<), se ajusta hasta a las 00:00 del día siguiente
        LocalDateTime inicio = desde != null ? desde : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fin = hasta != null ? hasta.plusSeconds(1) : LocalDateTime.now().plusDays(1);

        return pagoRepartidorRepository.findByRango(inicio, fin)
                .stream()
                .map(PagoRepartidor::getPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Fusiona listas WEB y COCINA en un único mapa ordenado por idRuta.
     * Las rutas que aparecen en ambas listas suman sus ingresos.
     */
    private List<EstadisticaRutaItemDTO> fusionarPorRuta(List<EstadisticaRutaItemDTO> web,
                                                         List<EstadisticaRutaItemDTO> cocina) {
        // LinkedHashMap preserva el orden de inserción (web ya viene ordenado por ruta.orden)
        Map<Integer, EstadisticaRutaItemDTO> mapa = new LinkedHashMap<>();

        for (EstadisticaRutaItemDTO item : web) {
            mapa.put(item.getIdRuta(), item);
        }

        for (EstadisticaRutaItemDTO item : cocina) {
            if (mapa.containsKey(item.getIdRuta())) {
                mapa.get(item.getIdRuta()).addIngresos(item.getIngresos());
            } else {
                mapa.put(item.getIdRuta(), item);
            }
        }

        return mapa.values().stream()
                .sorted(Comparator.comparing(EstadisticaRutaItemDTO::getOrden, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
