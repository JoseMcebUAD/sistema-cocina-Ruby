package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.request.BasicoRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.service.BasicoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/basico")
@Tag(name = "Básicos", description = "CRUD básico para gestionar paquetes predefinidos del menú")
public class BasicoController {

    private final BasicoService basicoService;

    public BasicoController(BasicoService basicoService) {
        this.basicoService = basicoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BasicoResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Básicos obtenidos correctamente",
                basicoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BasicoResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Básico encontrado",
                basicoService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BasicoResponseDTO>> save(@Valid @RequestBody BasicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Básico creado correctamente",
                        basicoService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BasicoResponseDTO>> update(@PathVariable int id,
                                                                 @Valid @RequestBody BasicoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Básico actualizado correctamente",
                basicoService.update(id, dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BasicoResponseDTO>> patch(@PathVariable int id,
                                                                @RequestBody Map<String, Object> payload) {
        if (payload.containsKey("idBasico")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.exito(200, "Básico actualizado parcialmente",
                basicoService.patch(id, payload)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        basicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
