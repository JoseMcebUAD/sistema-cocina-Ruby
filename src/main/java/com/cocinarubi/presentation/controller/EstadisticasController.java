package com.cocinarubi.presentation.controller;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.service.EstadisticasService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.EstadisticaRutaItemDTO;
import com.cocinarubi.presentation.dto.response.EstadisticasVentasResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Expone los endpoints de estadísticas de ventas y rutas para el dashboard.
 * Capa: Controller — delega toda la lógica a EstadisticasService.
 */
@RestController
@RequestMapping("/estadisticas")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    public EstadisticasController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    /**
     * Resumen de ingresos del período: total, por método de pago,
     * neto de repartidores y total de tarifas de domicilio.
     *
     * @param desde      fecha de inicio del período (inclusive, opcional)
     * @param hasta      fecha de fin del período (inclusive, opcional)
     * @param tipoPedido filtro por canal de venta (PICK_UP / DOMICILIO / MOSTRADOR, opcional)
     */
    @GetMapping("/ventas")
    public ResponseEntity<ApiResponse<EstadisticasVentasResponseDTO>> getVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) DBConstants.TipoPedido tipoPedido) {

        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;

        EstadisticasVentasResponseDTO datos = estadisticasService.getVentas(desdeTs, hastaTs, tipoPedido);
        return ResponseEntity.ok(ApiResponse.exito(200, "Estadísticas de ventas obtenidas", datos));
    }

    /**
     * Ingresos agrupados por ruta de reparto. Combina pedidos WEB y COCINA.
     *
     * @param desde      fecha de inicio del período (inclusive, opcional)
     * @param hasta      fecha de fin del período (inclusive, opcional)
     * @param metodoPago filtro por método de pago para desglosar ingresos (opcional)
     */
    @GetMapping("/rutas")
    public ResponseEntity<ApiResponse<List<EstadisticaRutaItemDTO>>> getIngresosPorRuta(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) DBConstants.MetodoPago metodoPago) {

        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;

        List<EstadisticaRutaItemDTO> datos = estadisticasService.getIngresosPorRuta(desdeTs, hastaTs, metodoPago);
        return ResponseEntity.ok(ApiResponse.exito(200, "Ingresos por ruta obtenidos", datos));
    }
}
