package com.cocinarubi.domain.service;

import com.cocinarubi.Constants;
import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.EstadisticasRepository;
import com.cocinarubi.dao.PagoRepartidorRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository;
import com.cocinarubi.dao.VistaResumenPedidoRepository.VistaResumenMetricasProjection;
import com.cocinarubi.dao.estadisticas.CatalogoEstadisticasRepository;
import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.domain.service.helpers.EstadisticaHelper;
import com.cocinarubi.domain.service.helpers.EstadisticaHelper.TipoCategoriaCatalogo;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.EstadisticaRutaItemDTO;
import com.cocinarubi.presentation.dto.response.EstadisticasVentasResponseDTO;
import com.cocinarubi.presentation.dto.response.ResumenDiasSemanaEstadisticaDTO;
import com.cocinarubi.presentation.dto.response.ResumenHorarioEstadisticaDTO;
import com.cocinarubi.presentation.dto.response.TipoEstadistica;
import com.cocinarubi.presentation.dto.response.estadisticas.CatalogoEstadisticaCategoria;
import com.cocinarubi.presentation.dto.response.estadisticas.CatalogoEstadisticaDTO;
import com.cocinarubi.presentation.dto.response.estadisticas.CatalogoProductoEstadisticaDTO;
import com.cocinarubi.presentation.dto.response.estadisticas.CatalogoProductoEstadisticaProducto;
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
import java.util.stream.Collectors;

/**
 * Lógica de negocio para el módulo de estadísticas de ventas y rutas.
 * Capa: Service.
 */
@Service
public class EstadisticasService {

    private final VistaResumenPedidoRepository vistaResumenPedidoRepository;
    private final EstadisticasRepository estadisticasRepository;
    private final CatalogoEstadisticasRepository catalogoEstadisticasRepository;
    private final PagoRepartidorRepository pagoRepartidorRepository;
    private final EstadisticaHelper estadisticaHelper;

    public EstadisticasService(VistaResumenPedidoRepository vistaResumenPedidoRepository,
                               EstadisticasRepository estadisticasRepository,
                               CatalogoEstadisticasRepository catalogoEstadisticasRepository,
                               PagoRepartidorRepository pagoRepartidorRepository) {
        this.vistaResumenPedidoRepository = vistaResumenPedidoRepository;
        this.estadisticasRepository = estadisticasRepository;
        this.catalogoEstadisticasRepository = catalogoEstadisticasRepository;
        this.pagoRepartidorRepository = pagoRepartidorRepository;
        this.estadisticaHelper = new EstadisticaHelper();
    }

    /**
     * Devuelve el resumen de ingresos del período con desglose por método de pago,
     * descuento de pagos a repartidores e ingreso de tarifas de domicilio.
     */
    @Transactional(readOnly = true)
    public EstadisticasVentasResponseDTO getVentas(LocalDateTime desde,
                                                   LocalDateTime hasta,
                                                   DBConstants.TipoPedido tipoPedido) {
        this.estadisticaHelper.validarRango(desde, hasta);

        VistaResumenMetricasProjection metricas =
                vistaResumenPedidoRepository.findMetricasConFiltros(desde, hasta, tipoPedido, null, null);

        BigDecimal tarifasWeb = estadisticasRepository.findSumTarifaWeb(desde, hasta, tipoPedido);
        BigDecimal tarifasCocina = estadisticasRepository.findSumTarifaCocina(desde, hasta, tipoPedido);

        BigDecimal totalPagosRepartidor = sumarPagosRepartidor(desde, hasta);

        BigDecimal ingresoTotal = this.estadisticaHelper.nullSafe(metricas.getIngresoTotal());

        return EstadisticasVentasResponseDTO.builder()
                .ingresoTotal(ingresoTotal)
                .ingresoEfectivo(this.estadisticaHelper.nullSafe(metricas.getIngresoEfectivo()))
                .ingresoTransferencia(this.estadisticaHelper.nullSafe(metricas.getIngresoTransferencia()))
                .ingresoTarjeta(this.estadisticaHelper.nullSafe(metricas.getIngresoTarjeta()))
                .ingresoTotalRepartidor(ingresoTotal.subtract(totalPagosRepartidor))
                .ingresoTarifas(this.estadisticaHelper.nullSafe(tarifasWeb).add(this.estadisticaHelper.nullSafe(tarifasCocina)))
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
        this.estadisticaHelper.validarRango(desde, hasta);

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
        LocalDateTime fin = hasta != null ? hasta.plusSeconds(1) : LocalDateTime.now(Constants.ZONA_MERIDA).plusDays(1);

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
        this.estadisticaHelper.validarRango(
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
            BigDecimal ingreso = row != null ? this.estadisticaHelper.toBigDecimal(row[2]) : BigDecimal.ZERO;

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
        int rangoMinutos = this.estadisticaHelper.parsearRango(rango);

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

        this.estadisticaHelper.validarRango(
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
                    .leyenda(this.estadisticaHelper.formatearSlot(i, rangoMinutos))
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
     * Devuelve el resumen de ventas agrupado por categoría de catálogo.
     * Los totales monetarios globales se obtienen de la vista de pedidos (misma lógica que getVentas).
     * Las categorías de ProductoCocina son dinámicas; se omiten las que no tienen ventas en el período.
     */
    @Transactional(readOnly = true)
    public CatalogoEstadisticaDTO getCatalogo(LocalDateTime desde, LocalDateTime hasta,
                                              DBConstants.TipoPedido tipoPedido) {
        estadisticaHelper.validarRango(desde, hasta);

        // Totales monetarios globales: reusar la proyección de la vista de pedidos
        VistaResumenMetricasProjection metricas =
                vistaResumenPedidoRepository.findMetricasConFiltros(desde, hasta, tipoPedido, null, null);

        List<CatalogoEstadisticaCategoria> categorias = new ArrayList<>();
        long totalProductosVendidos = 0;

        totalProductosVendidos += agregarResumenCategoria("COMIDA",
                catalogoEstadisticasRepository.findResumenComida(desde, hasta, tipoPedido), categorias);
        totalProductosVendidos += agregarResumenCategoria("DESAYUNO",
                catalogoEstadisticasRepository.findResumenDesayuno(desde, hasta, tipoPedido), categorias);
        totalProductosVendidos += agregarResumenCategoria("COMPLEMENTO",
                catalogoEstadisticasRepository.findResumenComplemento(desde, hasta, tipoPedido), categorias);
        totalProductosVendidos += agregarResumenCategoria("BASICO",
                catalogoEstadisticasRepository.findResumenBasico(desde, hasta, tipoPedido), categorias);
        totalProductosVendidos += agregarResumenCategoria("PAQUETE",
                catalogoEstadisticasRepository.findResumenPaquete(desde, hasta, tipoPedido), categorias);

        // Categorías dinámicas de ProductoCocina (N categorías desde la tabla categoria)
        for (Object[] row : catalogoEstadisticasRepository.findResumenProductoCocina(desde, hasta, tipoPedido)) {
            String nombreCat = (String) row[0];
            long vendido = ((Number) row[1]).longValue();
            int productos = ((Number) row[2]).intValue();
            if (vendido > 0) {
                categorias.add(new CatalogoEstadisticaCategoria(nombreCat, vendido, productos));
                totalProductosVendidos += vendido;
            }
        }

        return new CatalogoEstadisticaDTO(
                totalProductosVendidos,
                estadisticaHelper.nullSafe(metricas.getIngresoTotal()),
                estadisticaHelper.nullSafe(metricas.getIngresoTransferencia()),
                estadisticaHelper.nullSafe(metricas.getIngresoEfectivo()),
                estadisticaHelper.nullSafe(metricas.getIngresoTarjeta()),
                categorias
        );
    }

    /**
     * Devuelve el detalle de productos vendidos dentro de una categoría de catálogo.
     * Los totales de pago por producto usan metodoPagoPrincipal (vista de rendimiento, no contable).
     *
     * @param tipoCategoria categoría obligatoria; PRODUCTO_COCINA requiere también idCategoria
     * @param idCategoria   id de la Categoría dinámica (solo PRODUCTO_COCINA)
     * @param idSubcategoria filtro opcional de subcategoría (solo PRODUCTO_COCINA)
     */
    @Transactional(readOnly = true)
    public CatalogoProductoEstadisticaDTO getCatalogoProductos(LocalDateTime desde, LocalDateTime hasta,
                                                               DBConstants.TipoPedido tipoPedido,
                                                               TipoCategoriaCatalogo tipoCategoria,
                                                               Integer idCategoria,
                                                               Integer idSubcategoria) {
        estadisticaHelper.validarRango(desde, hasta);

        if (tipoCategoria == TipoCategoriaCatalogo.PRODUCTO_COCINA && idCategoria == null) {
            throw new BusinessException(
                    "El parámetro 'idCategoria' es requerido cuando tipoCategoria es PRODUCTO_COCINA",
                    HttpStatus.BAD_REQUEST);
        }

        List<Object[]> rows = switch (tipoCategoria) {
            case COMIDA       -> catalogoEstadisticasRepository.findProductosComida(desde, hasta, tipoPedido);
            case DESAYUNO     -> catalogoEstadisticasRepository.findProductosDesayuno(desde, hasta, tipoPedido);
            case COMPLEMENTO  -> catalogoEstadisticasRepository.findProductosComplemento(desde, hasta, tipoPedido);
            case BASICO       -> catalogoEstadisticasRepository.findProductosBasico(desde, hasta, tipoPedido);
            case PAQUETE      -> catalogoEstadisticasRepository.findProductosPaquete(desde, hasta, tipoPedido);
            case PRODUCTO_COCINA -> catalogoEstadisticasRepository.findProductosProductoCocina(
                    desde, hasta, tipoPedido, idCategoria, idSubcategoria);
        };

        String nombreCategoria = tipoCategoria.name();

        // Object[]: [nombreProducto, totalVendido, totalVentas, totalEfectivo, totalTransferencia, totalTarjeta]
        List<CatalogoProductoEstadisticaProducto> productos = rows.stream()
                .map(row -> new CatalogoProductoEstadisticaProducto(
                        nombreCategoria,
                        (String) row[0],
                        estadisticaHelper.toBigDecimal(row[2]),
                        ((Number) row[1]).intValue()
                ))
                .toList();

        // Agregar totales globales sumando sobre los rows (evita segunda consulta)
        int totalProductosVendidos = rows.stream().mapToInt(r -> ((Number) r[1]).intValue()).sum();
        BigDecimal totalVentas        = rows.stream().map(r -> estadisticaHelper.toBigDecimal(r[2])).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEfectivo      = rows.stream().map(r -> estadisticaHelper.toBigDecimal(r[3])).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTransferencia = rows.stream().map(r -> estadisticaHelper.toBigDecimal(r[4])).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTarjeta       = rows.stream().map(r -> estadisticaHelper.toBigDecimal(r[5])).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CatalogoProductoEstadisticaDTO(
                totalProductosVendidos,
                totalVentas,
                totalTransferencia,
                totalTarjeta,
                totalEfectivo,
                productos
        );
    }

    /**
     * Añade una CatalogoEstadisticaCategoria a la lista si tiene ventas, y devuelve el totalVendido.
     * El resultado es List con una sola fila: row[0] = totalVendido (Long), row[1] = totalProductos (Long).
     */
    private long agregarResumenCategoria(String nombre, List<Object[]> resultado,
                                         List<CatalogoEstadisticaCategoria> categorias) {
        if (resultado == null || resultado.isEmpty()) return 0L;
        Object[] row = resultado.get(0);
        if (row == null || row[0] == null) return 0L;
        long vendido = ((Number) row[0]).longValue();
        int productos = row[1] != null ? ((Number) row[1]).intValue() : 0;
        if (vendido > 0) {
            categorias.add(new CatalogoEstadisticaCategoria(nombre, vendido, productos));
        }
        return vendido;
    }

}
