package com.cocinarubi.presentation.controller;

import com.cocinarubi.DBConstants;
import com.cocinarubi.domain.service.VistaResumenPedidoService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoConMetricasResponseDTO;
import com.cocinarubi.presentation.dto.response.VistaResumenPedidoResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/vista-resumen-pedido")
@Tag(name = "Vista/resumen de Pedidos",
     description = "Vista consolidada de pedidos (todos los orígenes y tipos) con paginación, filtros y métricas agregadas")
public class VistaResumenPedidoController {

    private final VistaResumenPedidoService vistaResumenPedidoService;

    public VistaResumenPedidoController(VistaResumenPedidoService vistaResumenPedidoService) {
        this.vistaResumenPedidoService = vistaResumenPedidoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VistaResumenPedidoResponseDTO>>> findVista(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) DBConstants.TipoPedido tipoPedido,
            @RequestParam(required = false) DBConstants.PedidoCreadoDesde pedidoCreadoDesde,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<VistaResumenPedidoResponseDTO> resultado = vistaResumenPedidoService.findVista(
                desde, hasta, tipoPedido, pedidoCreadoDesde, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.exito(200, "Vista de pedidos obtenida correctamente", resultado));
    }

    @GetMapping("/metricas")
    public ResponseEntity<ApiResponse<VistaResumenPedidoConMetricasResponseDTO>> findVistaConMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) DBConstants.TipoPedido tipoPedido,
            @RequestParam(required = false) DBConstants.PedidoCreadoDesde pedidoCreadoDesde,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        VistaResumenPedidoConMetricasResponseDTO resultado = vistaResumenPedidoService.findVistaConMetricas(
                desde, hasta, tipoPedido, pedidoCreadoDesde, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.exito(200, "Vista y métricas de pedidos obtenidas correctamente", resultado));
    }
}
