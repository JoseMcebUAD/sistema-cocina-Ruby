package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.request.TarifaEspecialRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.entity.TarifaEspecial;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.service.TarifaEspecialService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tarifa-especial")
@Tag(name = "Tarifas Especiales", description = "CRUD básico para gestionar tarifas especiales de envío")
public class TarifaEspecialController {

    private final TarifaEspecialService tarifaEspecialService;

    public TarifaEspecialController(TarifaEspecialService tarifaEspecialService) {
        this.tarifaEspecialService = tarifaEspecialService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TarifaEspecial>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tarifas especiales obtenidas correctamente",
                tarifaEspecialService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TarifaEspecial>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tarifa especial encontrada",
                tarifaEspecialService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TarifaEspecial>> save(@Valid @RequestBody TarifaEspecialRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Tarifa especial creada correctamente",
                        tarifaEspecialService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TarifaEspecial>> update(@PathVariable int id,
                                                              @Valid @RequestBody TarifaEspecialRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tarifa especial actualizada correctamente",
                tarifaEspecialService.update(id, dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TarifaEspecial>> patch(@PathVariable int id,
                                                             @RequestBody Map<String, Object> payload) {
        if (payload.containsKey("idTarifaLluvia")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.exito(200, "Tarifa especial actualizada parcialmente",
                tarifaEspecialService.patch(id, payload)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        tarifaEspecialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
