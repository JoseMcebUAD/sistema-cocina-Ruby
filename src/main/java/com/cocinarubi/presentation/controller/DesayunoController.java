package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.domain.service.DesayunoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/desayuno")
@Tag(name = "Desayunos", description = "CRUD básico para gestionar el menú de desayunos")
public class DesayunoController {

    private final DesayunoService desayunoService;
    private final ObjectMapper objectMapper;

    public DesayunoController(DesayunoService desayunoService, ObjectMapper objectMapper) {
        this.desayunoService = desayunoService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Desayuno>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Desayunos obtenidos correctamente",
                desayunoService.findAll()));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<Desayuno>>> findDisponibles() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Desayunos disponibles obtenidos correctamente",
                desayunoService.findDisponibles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Desayuno>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Desayuno encontrado",
                desayunoService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Desayuno>> save(@RequestBody Desayuno desayuno) {
        desayuno.setIdDesayuno(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Desayuno creado correctamente",
                        desayunoService.save(desayuno)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Desayuno>> update(@RequestBody Desayuno desayuno) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Desayuno actualizado correctamente",
                desayunoService.save(desayuno)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Desayuno>> patch(@PathVariable int id,
                                                        @RequestBody Map<String, Object> payload) {
        Desayuno existente = desayunoService.findById(id);
        if (payload.containsKey("idDesayuno")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        try {
            Desayuno actualizado = objectMapper.updateValue(existente, payload);
            return ResponseEntity.ok(ApiResponse.exito(200, "Desayuno actualizado parcialmente",
                    desayunoService.save(actualizado)));
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new BusinessException(
                    "Error al aplicar la actualización parcial: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        desayunoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
