package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.CategoriaService;
import com.cocinarubi.presentation.dto.request.CategoriaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.CategoriaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del catálogo de categorías de {@link com.cocinarubi.domain.entity.ProductoCocina}.
 * Capa: Controller — expone CRUD + endpoint especial de árbol categoría → subcategorías.
 */
@RestController
@RequestMapping("/categoria")
@Tag(name = "Categoría", description = "CRUD de categorías del catálogo de ProductoCocina")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Categorías obtenidas correctamente",
                categoriaService.findAll()));
    }

    @GetMapping("/con-subcategorias")
    public ResponseEntity<ApiResponse<List<CategoriaResponseDTO>>> findAllConSubcategorias() {
        return ResponseEntity.ok(ApiResponse.exito(200,
                "Categorías con subcategorías obtenidas correctamente",
                categoriaService.findAllConSubcategorias()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Categoría encontrada",
                categoriaService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> save(
            @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Categoría creada correctamente",
                        categoriaService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponseDTO>> update(@PathVariable int id,
                                                                    @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Categoría actualizada correctamente",
                categoriaService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
