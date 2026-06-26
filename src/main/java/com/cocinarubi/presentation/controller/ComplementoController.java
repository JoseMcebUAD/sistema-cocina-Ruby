package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.entity.Complemento;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.service.ComplementoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/complemento")
@Tag(name = "Complementos", description = "CRUD básico para gestionar complementos del menú")
public class ComplementoController {

    private final ComplementoService complementoService;
    private final ObjectMapper objectMapper;

    public ComplementoController(ComplementoService complementoService, ObjectMapper objectMapper) {
        this.complementoService = complementoService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Complemento>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Complementos obtenidos correctamente",
                complementoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Complemento>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Complemento encontrado",
                complementoService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Complemento>> save(@RequestBody Complemento complemento) {
        complemento.setIdComplemento(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Complemento creado correctamente",
                        complementoService.save(complemento)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Complemento>> update(@RequestBody Complemento complemento) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Complemento actualizado correctamente",
                complementoService.save(complemento)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Complemento>> patch(@PathVariable int id,
                                                           @RequestBody Map<String, Object> payload) {
        Complemento existente = complementoService.findById(id);
        if (payload.containsKey("idComplemento")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        try {
            Complemento actualizado = objectMapper.updateValue(existente, payload);
            return ResponseEntity.ok(ApiResponse.exito(200, "Complemento actualizado parcialmente",
                    complementoService.save(actualizado)));
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new BusinessException(
                    "Error al aplicar la actualización parcial: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        complementoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
