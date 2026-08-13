package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.SubcategoriaService;
import com.cocinarubi.presentation.dto.request.SubcategoriaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.SubcategoriaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del catálogo de subcategorías.
 * Capa: Controller — expone CRUD sobre subcategorías asociadas a una {@link com.cocinarubi.domain.entity.Categoria}.
 */
@RestController
@RequestMapping("/subcategoria")
@Tag(name = "Subcategoría", description = "CRUD de subcategorías del catálogo de ProductoCocina")
public class SubcategoriaController {

    private final SubcategoriaService subcategoriaService;

    public SubcategoriaController(SubcategoriaService subcategoriaService) {
        this.subcategoriaService = subcategoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubcategoriaResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Subcategorías obtenidas correctamente",
                subcategoriaService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubcategoriaResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Subcategoría encontrada",
                subcategoriaService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubcategoriaResponseDTO>> save(
            @Valid @RequestBody SubcategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Subcategoría creada correctamente",
                        subcategoriaService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubcategoriaResponseDTO>> update(@PathVariable int id,
                                                                       @Valid @RequestBody SubcategoriaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Subcategoría actualizada correctamente",
                subcategoriaService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        subcategoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
