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
import com.cocinarubi.presentation.dto.response.ResumenDiasSemanaEstadisticaDTO;
import com.cocinarubi.presentation.dto.response.ResumenHorarioEstadisticaDTO;
import com.cocinarubi.presentation.dto.response.TipoEstadistica;
import com.cocinarubi.presentation.dto.response.graficas.DatoGrafica;
import com.cocinarubi.presentation.dto.response.graficas.DatoGraficaDia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /**
     * Devuelve pedidos e ingreso agrupados por día de semana (Lunes a Domingo).
     * Siempre incluye los 7 días; días sin pedidos llevan valores en cero.
     */
    @Transactional(readOnly = true)
    public ResumenDiasSemanaEstadisticaDTO resumenDiasSemana(LocalDate desde, LocalDate hasta) {
        validarRango(
                desde != null ? desde.atStartOfDay() : null,
                hasta != null ? hasta.atTime(23, 59, 59) : null
        );

        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;

        List<Object[]> rows = estadisticasRepository.findVentasPorDiaSemana(desdeTs, hastaTs);

        // DAYOFWEEK: 1=Dom, 2=Lun, 3=Mar, 4=Mié, 5=Jue, 6=Vie, 7=Sáb
        Map<Integer, Object[]> porDia = new HashMap<>();
        for (Object[] row : rows) {
            porDia.put(((Number) row[0]).intValue(), row);
        }

        int[] ordenLunDom = {2, 3, 4, 5, 6, 7, 1};
        String[] leyendas = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

        List<DatoGraficaDia> dias = new ArrayList<>();
        int totalPedidos = 0;
        BigDecimal ingresoTotal = BigDecimal.ZERO;

        for (int i = 0; i < ordenLunDom.length; i++) {
            Object[] row = porDia.get(ordenLunDom[i]);
            BigDecimal cantidad = row != null ? BigDecimal.valueOf(((Number) row[1]).longValue()) : BigDecimal.ZERO;
            BigDecimal ingreso = row != null ? toBigDecimal(row[2]) : BigDecimal.ZERO;

            totalPedidos += cantidad.intValue();
            ingresoTotal = ingresoTotal.add(ingreso);
            dias.add(new DatoGraficaDia(leyendas[i], cantidad, ingreso));
        }

        return ResumenDiasSemanaEstadisticaDTO.builder()
                .nombreEstadistica("Resumen por día de semana")
                .tipoEstadistica(TipoEstadistica.BARRAS)
                .totalPedidos(totalPedidos)
                .ingresoTotal(ingresoTotal)
                .dias(dias)
                .build();
    }

    /**
     * Devuelve pedidos agrupados por franjas horarias según el rango indicado.
     * Siempre incluye todos los slots del día (00:00 a 24:00); franjas vacías llevan valor cero.
     *
     * @param rango formato "NHH" o "NM" — ej. "1H", "2H", "30M", "20M"
     */
    @Transactional(readOnly = true)
    public ResumenHorarioEstadisticaDTO resumenHorario(LocalDate desde, LocalDate hasta, String rango) {
        int rangoMinutos = parsearRango(rango);

        if (rangoMinutos < 5) {
            throw new BusinessException(
                    "El rango mínimo permitido es 5 minutos",
                    HttpStatus.BAD_REQUEST);
        }

        if (desde != null && hasta != null && ChronoUnit.DAYS.between(desde, hasta) > 30) {
            throw new BusinessException(
                    "El rango de fechas no puede superar 30 días",
                    HttpStatus.BAD_REQUEST);
        }

        validarRango(
                desde != null ? desde.atStartOfDay() : null,
                hasta != null ? hasta.atTime(23, 59, 59) : null
        );

        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;
        int segundosRango = rangoMinutos * 60;

        List<Object[]> rows = estadisticasRepository.findVentasPorHorario(desdeTs, hastaTs, segundosRango);

        Map<Integer, Long> porSlot = new HashMap<>();
        for (Object[] row : rows) {
            porSlot.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        int totalSlots = (int) Math.ceil(24.0 * 60 / rangoMinutos);
        List<DatoGrafica> slots = new ArrayList<>();
        int totalPedidos = 0;

        for (int i = 0; i < totalSlots; i++) {
            long cantidad = porSlot.getOrDefault(i, 0L);
            totalPedidos += cantidad;
            slots.add(DatoGrafica.builder()
                    .leyenda(formatearSlot(i, rangoMinutos))
                    .valor(BigDecimal.valueOf(cantidad))
                    .build());
        }

        return ResumenHorarioEstadisticaDTO.builder()
                .nombreEstadistica("Resumen por horario")
                .tipoEstadistica(TipoEstadistica.BARRAS)
                .datos(slots)
                .totalPedidos(totalPedidos)
                .rango(rango)
                .build();
    }

    /**
     * Convierte el string de rango ("1H", "30M") a minutos.
     * Lanza BusinessException si el formato no es válido.
     */
    private int parsearRango(String rango) {
        if (rango == null || rango.isBlank()) {
            throw new BusinessException("El parámetro 'rango' es requerido", HttpStatus.BAD_REQUEST);
        }
        Matcher m = Pattern.compile("^(\\d+)(H|M)$", Pattern.CASE_INSENSITIVE).matcher(rango.trim());
        if (!m.matches()) {
            throw new BusinessException(
                    "Formato de rango inválido. Use NHH o NM (ej. 1H, 2H, 30M)",
                    HttpStatus.BAD_REQUEST);
        }
        int valor = Integer.parseInt(m.group(1));
        return m.group(2).equalsIgnoreCase("H") ? valor * 60 : valor;
    }

    /** Convierte índice de slot a etiqueta "HH:mm-HH:mm". */
    private String formatearSlot(int slotIndex, int rangoMinutos) {
        int inicioMin = slotIndex * rangoMinutos;
        int finMin = Math.min(inicioMin + rangoMinutos, 24 * 60);
        return String.format("%02d:%02d-%02d:%02d",
                inicioMin / 60, inicioMin % 60,
                finMin / 60, finMin % 60);
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        return new BigDecimal(obj.toString());
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
