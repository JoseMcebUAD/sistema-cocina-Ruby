package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.domain.service.ComidaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comida")
@Tag(name = "Comidas", description = "CRUD básico para gestionar el menú de comidas")
public class ComidaController {

    private final ComidaService comidaService;
    private final ObjectMapper objectMapper;

    public ComidaController(ComidaService comidaService, ObjectMapper objectMapper) {
        this.comidaService = comidaService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Comida>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Comidas obtenidas correctamente",
                comidaService.findAll()));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<Comida>>> findDisponibles() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Comidas disponibles obtenidas correctamente",
                comidaService.findDisponibles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Comida>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Comida encontrada",
                comidaService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Comida>> save(@RequestBody Comida comida) {
        comida.setIdComida(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Comida creada correctamente",
                        comidaService.save(comida)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Comida>> update(@RequestBody Comida comida) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Comida actualizada correctamente",
                comidaService.save(comida)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Comida>> patch(@PathVariable int id,
                                                      @RequestBody Map<String, Object> payload) {
        Comida existente = comidaService.findById(id);
        if (payload.containsKey("idComida")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        try {
            Comida actualizada = objectMapper.updateValue(existente, payload);
            return ResponseEntity.ok(ApiResponse.exito(200, "Comida actualizada parcialmente",
                    comidaService.save(actualizada)));
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new BusinessException(
                    "Error al aplicar la actualización parcial: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        comidaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
