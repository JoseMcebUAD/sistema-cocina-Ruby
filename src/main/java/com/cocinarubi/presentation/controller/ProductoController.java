package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.ProductoService;
import com.cocinarubi.presentation.dto.request.ProductoRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.ProductoResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/producto")
@Tag(name = "Productos", description = "CRUD del catálogo de ingredientes para recetas")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Productos obtenidos correctamente",
                productoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Producto encontrado",
                productoService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> save(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Producto creado correctamente",
                        productoService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponseDTO>> update(@PathVariable int id,
                                                                   @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Producto actualizado correctamente",
                productoService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable int id,
                                                    @RequestParam(defaultValue = "false") boolean saltarConfirmacion) {
        productoService.delete(id, saltarConfirmacion);
        return ResponseEntity.ok(ApiResponse.exito(200, "Producto eliminado correctamente", null));
    }
}
