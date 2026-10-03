package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.ProductoRecetaService;
import com.cocinarubi.presentation.dto.request.ProductoRecetaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.ProductoRecetaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de los ingredientes que forman la receta de una comida.
 * Se audita como tabla {@code producto_receta} (derivado del nombre de esta clase).
 */
@RestController
@RequestMapping("/comida/{idComida}/receta")
@Tag(name = "Receta - ingredientes", description = "CRUD de los ingredientes de la receta de una comida")
public class ProductoRecetaController {

    private final ProductoRecetaService productoRecetaService;

    public ProductoRecetaController(ProductoRecetaService productoRecetaService) {
        this.productoRecetaService = productoRecetaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoRecetaResponseDTO>>> findByIdComida(
            @PathVariable int idComida) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Ingredientes de la receta obtenidos",
                productoRecetaService.findByIdComida(idComida)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoRecetaResponseDTO>> add(
            @PathVariable int idComida,
            @Valid @RequestBody ProductoRecetaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Ingrediente agregado a la receta",
                        productoRecetaService.addIngrediente(idComida, dto)));
    }

    @PutMapping("/{idProductoReceta}")
    public ResponseEntity<ApiResponse<ProductoRecetaResponseDTO>> update(
            @PathVariable int idComida,
            @PathVariable int idProductoReceta,
            @Valid @RequestBody ProductoRecetaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Ingrediente actualizado",
                productoRecetaService.updateIngrediente(idComida, idProductoReceta, dto)));
    }

    @DeleteMapping("/{idProductoReceta}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable int idComida,
            @PathVariable int idProductoReceta) {
        productoRecetaService.deleteIngrediente(idComida, idProductoReceta);
        return ResponseEntity.ok(ApiResponse.exito(200, "Ingrediente eliminado de la receta", null));
    }
}
