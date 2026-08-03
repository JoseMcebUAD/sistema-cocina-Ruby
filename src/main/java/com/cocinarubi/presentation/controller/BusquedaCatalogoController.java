package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.BusquedaCatalogoService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.busqueda.ResultadoBusquedaResponseDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la búsqueda cross-catálogo por nombre con paginación global.
 * Capa: Controller — delega al servicio sin lógica propia.
 */
@RestController
@RequestMapping("/busqueda-catalogo")
@Tag(name = "Búsqueda de Catálogo",
     description = "Búsqueda cross-catálogo por nombre en comidas, desayunos, básicos y productos de cocina. Mínimo 3 caracteres.")
public class BusquedaCatalogoController {

    private final BusquedaCatalogoService busquedaCatalogoService;

    public BusquedaCatalogoController(BusquedaCatalogoService busquedaCatalogoService) {
        this.busquedaCatalogoService = busquedaCatalogoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultadoBusquedaResponseDTO>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Búsqueda completada",
                busquedaCatalogoService.buscar(q, page, size)));
    }
}
