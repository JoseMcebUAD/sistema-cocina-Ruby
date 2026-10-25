package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.SuscripcionSemanalService;
import com.cocinarubi.presentation.dto.request.SuscripcionSemanalRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.SuscripcionSemanalResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de suscripciones semanales para el panel administrador.
 * Capa: Controller — presentación REST.
 */
@RestController
@RequestMapping("/suscripcion-semanal")
@Tag(name = "Suscripción Semanal", description = "CRUD de suscripciones semanales de comidas")
public class SuscripcionSemanalController {

    private final SuscripcionSemanalService suscripcionSemanalService;

    public SuscripcionSemanalController(SuscripcionSemanalService suscripcionSemanalService) {
        this.suscripcionSemanalService = suscripcionSemanalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SuscripcionSemanalResponseDTO>>> findAllPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Suscripciones obtenidas correctamente",
                suscripcionSemanalService.findAll(PageRequest.of(page, size))));
    }

    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<SuscripcionSemanalResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Suscripciones obtenidas correctamente",
                suscripcionSemanalService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SuscripcionSemanalResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Suscripción encontrada",
                suscripcionSemanalService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SuscripcionSemanalResponseDTO>> save(
            @Valid @RequestBody SuscripcionSemanalRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Suscripción creada correctamente",
                        suscripcionSemanalService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SuscripcionSemanalResponseDTO>> update(
            @PathVariable int id,
            @Valid @RequestBody SuscripcionSemanalRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Suscripción actualizada correctamente",
                suscripcionSemanalService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        suscripcionSemanalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
