package com.cocinarubi.presentation.controller;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.domain.service.ComboService;
import com.cocinarubi.presentation.dto.request.ComboRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.ComboResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del catálogo de {@link com.cocinarubi.domain.entity.Combo}.
 * Capa: Controller — expone CRUD de combos (promociones).
 */
@RestController
@RequestMapping("/combo")
@Tag(name = "Combo", description = "CRUD de combos (promociones)")
public class ComboController {

    private final ComboService comboService;

    public ComboController(ComboService comboService) {
        this.comboService = comboService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComboResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Combos obtenidos correctamente",
                comboService.findAll()));
    }

    @GetMapping("/paginado")
    public ResponseEntity<ApiResponse<Page<ComboResponseDTO>>> findAllPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Combos obtenidos correctamente",
                comboService.findAllPaginado(PageRequest.of(page, size))));
    }

    @GetMapping("/disponibles-paginado")
    public ResponseEntity<ApiResponse<Page<ComboResponseDTO>>> findDisponiblesPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Combos disponibles obtenidos correctamente",
                comboService.findByEstatusPaginado(Estatus.DISPONIBLE, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComboResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Combo encontrado",
                comboService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComboResponseDTO>> save(
            @Valid @RequestBody ComboRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Combo creado correctamente",
                        comboService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComboResponseDTO>> update(@PathVariable int id,
                                                                @Valid @RequestBody ComboRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Combo actualizado correctamente",
                comboService.update(id, dto)));
    }

    @PutMapping("/destacado/{id}")
    public ResponseEntity<ApiResponse<ComboResponseDTO>> toggleDestacado(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Destacado actualizado",
                comboService.toggleDestacado(id)));
    }

    @PutMapping("/estatus/{id}")
    public ResponseEntity<ApiResponse<ComboResponseDTO>> updateEstatus(
            @PathVariable int id,
            @RequestParam Estatus estatus) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Estatus actualizado",
                comboService.updateEstatus(id, estatus)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable int id,
            @RequestParam(defaultValue = "false") boolean saltarConfirmacion) {
        comboService.delete(id, saltarConfirmacion);
        return ResponseEntity.ok(ApiResponse.exito(200, "Combo eliminado correctamente", null));
    }
}
