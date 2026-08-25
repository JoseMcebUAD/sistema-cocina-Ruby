package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.ProductoCocinaService;
import com.cocinarubi.presentation.dto.request.ProductoCocinaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.ProductoCocinaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/producto-cocina")
@Tag(name = "Producto Cocina",
     description = "CRUD para productos de cocina clasificados por Categoria (BEBIDA, SNACK, CHAROLA, POSTRE y EXTRAS y las creadas por el usuario)")
public class ProductoCocinaController {

    private final ProductoCocinaService productoCocinaService;

    public ProductoCocinaController(ProductoCocinaService productoCocinaService) {
        this.productoCocinaService = productoCocinaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductoCocinaResponseDTO>>> findAllPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Productos de cocina obtenidos correctamente",
                productoCocinaService.findAll(PageRequest.of(page, size))));
    }

    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<ProductoCocinaResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Productos de cocina obtenidos correctamente",
                productoCocinaService.findAll()));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<ProductoCocinaResponseDTO>>> findDisponibles() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Productos de cocina disponibles obtenidos correctamente",
                productoCocinaService.findDisponibles()));
    }

    @GetMapping("/disponibles-paginado")
    public ResponseEntity<ApiResponse<Page<ProductoCocinaResponseDTO>>> findDisponiblesPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Productos de cocina disponibles obtenidos correctamente",
                productoCocinaService.findDisponibles(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoCocinaResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Producto de cocina encontrado",
                productoCocinaService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoCocinaResponseDTO>> save(
            @Valid @RequestBody ProductoCocinaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Producto de cocina creado correctamente",
                        productoCocinaService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoCocinaResponseDTO>> update(@PathVariable int id,
                                                                        @Valid @RequestBody ProductoCocinaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Producto de cocina actualizado correctamente",
                productoCocinaService.update(id, dto)));
    }

    @PutMapping("/destacado/{id}")
    public ResponseEntity<ApiResponse<ProductoCocinaResponseDTO>> toggleDestacado(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Destacado actualizado",
                productoCocinaService.toggleDestacado(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable int id,
            @RequestParam(defaultValue = "false") boolean saltarConfirmacion) {
        productoCocinaService.delete(id, saltarConfirmacion);
        return ResponseEntity.ok(ApiResponse.exito(200, "Producto de cocina eliminado correctamente", null));
    }
}
