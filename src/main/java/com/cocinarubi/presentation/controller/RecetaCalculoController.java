package com.cocinarubi.presentation.controller;

import com.cocinarubi.aop.SkipAudit;
import com.cocinarubi.domain.service.RecetaCalculoService;
import com.cocinarubi.presentation.dto.request.CalcularFaltanteRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.CalcularFaltanteResponseDTO;
import com.cocinarubi.presentation.dto.response.RecetaCosteoResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de consulta/cálculo sobre la receta: costeo escalado y faltante dado un stock.
 * No persisten nada, por lo que llevan {@link SkipAudit} a nivel clase.
 */
@SkipAudit
@RestController
@RequestMapping("/comida/{idComida}/receta")
@Tag(name = "Receta - cálculos", description = "Costeo escalado y cálculo de faltante")
public class RecetaCalculoController {

    private final RecetaCalculoService recetaCalculoService;

    public RecetaCalculoController(RecetaCalculoService recetaCalculoService) {
        this.recetaCalculoService = recetaCalculoService;
    }

    @GetMapping("/costear")
    public ResponseEntity<ApiResponse<RecetaCosteoResponseDTO>> costear(
            @PathVariable int idComida,
            @RequestParam int porcionesDeseadas) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Receta costeada correctamente",
                recetaCalculoService.costearReceta(idComida, porcionesDeseadas)));
    }

    @PostMapping("/calcular-faltante")
    public ResponseEntity<ApiResponse<CalcularFaltanteResponseDTO>> calcularFaltante(
            @PathVariable int idComida,
            @Valid @RequestBody CalcularFaltanteRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Faltante calculado correctamente",
                recetaCalculoService.calcularFaltante(idComida, request)));
    }
}
