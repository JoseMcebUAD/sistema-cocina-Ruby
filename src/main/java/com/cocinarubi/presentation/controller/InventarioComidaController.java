package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.InventarioComidaService;
import com.cocinarubi.presentation.dto.request.InventarioComidaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.InventarioComidaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventarioComida")
@Tag(name = "Inventario Comida", description = "Registro de consumo de insumos por platillo")
public class InventarioComidaController {

    private final InventarioComidaService inventarioComidaService;

    public InventarioComidaController(InventarioComidaService inventarioComidaService) {
        this.inventarioComidaService = inventarioComidaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventarioComidaResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registros de inventario obtenidos correctamente",
                inventarioComidaService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventarioComidaResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registro de inventario encontrado",
                inventarioComidaService.findById(id)));
    }

    @GetMapping("/comida/{idComida}")
    public ResponseEntity<ApiResponse<List<InventarioComidaResponseDTO>>> findByComida(
            @PathVariable int idComida) {
        return ResponseEntity.ok(ApiResponse.exito(200,
                "Historial de consumo obtenido correctamente",
                inventarioComidaService.findByComida(idComida)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventarioComidaResponseDTO>> save(
            @Valid @RequestBody InventarioComidaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Registro de inventario creado correctamente",
                        inventarioComidaService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventarioComidaResponseDTO>> update(@PathVariable int id,
                                                                          @Valid @RequestBody InventarioComidaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registro de inventario actualizado correctamente",
                inventarioComidaService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        inventarioComidaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
