package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.CodigoClienteService;
import com.cocinarubi.presentation.dto.request.CodigoClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.CodigoClienteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/codigoCliente")
@Tag(name = "Código Cliente", description = "CRUD de códigos especiales para clientes fuera de rutas estándar")
public class CodigoClienteController {

    private final CodigoClienteService codigoClienteService;

    public CodigoClienteController(CodigoClienteService codigoClienteService) {
        this.codigoClienteService = codigoClienteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CodigoClienteResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Códigos de cliente obtenidos correctamente",
                codigoClienteService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CodigoClienteResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Código de cliente encontrado",
                codigoClienteService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CodigoClienteResponseDTO>> save(
            @Valid @RequestBody CodigoClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Código de cliente creado correctamente",
                        codigoClienteService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CodigoClienteResponseDTO>> update(@PathVariable int id,
                                                                       @Valid @RequestBody CodigoClienteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Código de cliente actualizado correctamente",
                codigoClienteService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        codigoClienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
