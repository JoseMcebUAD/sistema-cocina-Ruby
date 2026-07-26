package com.cocinarubi.presentation.controller;

import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.cocinarubi.domain.service.ResumenProduccionService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.DetalleProduccionResponseDTO;
import com.cocinarubi.presentation.dto.response.ResumenProduccionResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de producción diaria para la cocina.
 * Devuelve cuántas unidades de cada categoría (comidas, desayunos, snacks, bebidas,
 * charolas, postres, básicos) se han pedido en el día, con filtros opcionales.
 * Capa: Controller — delega toda la lógica a ResumenProduccionService.
 */
@RestController
@RequestMapping("/produccion")
public class ResumenProduccionController {

    private final ResumenProduccionService resumenProduccionService;

    public ResumenProduccionController(ResumenProduccionService resumenProduccionService) {
        this.resumenProduccionService = resumenProduccionService;
    }

    /**
     * Devuelve los conteos totales del día por categoría de alimento.
     * Todos los parámetros son opcionales; sin filtros devuelve el total general del día.
     *
     * @param tipoPedido        filtrar por canal de entrega (PICK_UP, DOMICILIO, MOSTRADOR)
     * @param pedidoCreadoDesde filtrar por origen del pedido (WEB, COCINA)
     * @param idRutas           filtrar por una o varias rutas de reparto (solo aplica a DOMICILIO)
     */
    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<ResumenProduccionResponseDTO>> resumenProduccion(
            @RequestParam(required = false) TipoPedido tipoPedido,
            @RequestParam(required = false) PedidoCreadoDesde pedidoCreadoDesde,
            @RequestParam(required = false) List<Integer> idRutas) {

        ResumenProduccionResponseDTO data = resumenProduccionService
                .resumenProduccion(tipoPedido, pedidoCreadoDesde, idRutas);

        return ResponseEntity.ok(
                ApiResponse.exito(200, "Resumen de producción del día obtenido correctamente", data));
    }

    /**
     * Devuelve el desglose de productos de una categoría específica con sus cantidades.
     * La categoría debe ser uno de: comidas, desayunos, snacks, bebidas, charolas, postres, basicos.
     *
     * @param categoria         nombre de la categoría a desglosar
     * @param tipoPedido        filtrar por canal de entrega (opcional)
     * @param pedidoCreadoDesde filtrar por origen del pedido (opcional)
     * @param idRutas           filtrar por ruta(s) de reparto (opcional)
     */
    @GetMapping("/resumen/detalle/{categoria}")
    public ResponseEntity<ApiResponse<DetalleProduccionResponseDTO>> detalleProduccion(
            @PathVariable String categoria,
            @RequestParam(required = false) TipoPedido tipoPedido,
            @RequestParam(required = false) PedidoCreadoDesde pedidoCreadoDesde,
            @RequestParam(required = false) List<Integer> idRutas) {

        DetalleProduccionResponseDTO data = resumenProduccionService
                .detalleProduccion(categoria, tipoPedido, pedidoCreadoDesde, idRutas);

        return ResponseEntity.ok(
                ApiResponse.exito(200, "Detalle de " + categoria.toLowerCase() + " obtenido correctamente", data));
    }
}
