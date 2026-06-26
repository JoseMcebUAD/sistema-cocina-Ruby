package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.entity.HorarioAtencion;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.service.HorarioAtencionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/horario-atencion")
@Tag(name = "Horarios de Atención", description = "CRUD básico para gestionar horarios de atención")
public class HorarioAtencionController {

    private final HorarioAtencionService horarioAtencionService;
    private final ObjectMapper objectMapper;

    public HorarioAtencionController(HorarioAtencionService horarioAtencionService, ObjectMapper objectMapper) {
        this.horarioAtencionService = horarioAtencionService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HorarioAtencion>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Horarios de atención obtenidos correctamente",
                horarioAtencionService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HorarioAtencion>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Horario de atención encontrado",
                horarioAtencionService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HorarioAtencion>> save(@RequestBody HorarioAtencion horarioAtencion) {
        horarioAtencion.setIdHorarioAtencionComidas(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Horario de atención creado correctamente",
                        horarioAtencionService.save(horarioAtencion)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<HorarioAtencion>> update(@RequestBody HorarioAtencion horarioAtencion) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Horario de atención actualizado correctamente",
                horarioAtencionService.save(horarioAtencion)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<HorarioAtencion>> patch(@PathVariable int id,
                                                               @RequestBody Map<String, Object> payload) {
        HorarioAtencion existente = horarioAtencionService.findById(id);
        if (payload.containsKey("idHorarioAtencionComidas")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        try {
            HorarioAtencion actualizado = objectMapper.updateValue(existente, payload);
            return ResponseEntity.ok(ApiResponse.exito(200, "Horario de atención actualizado parcialmente",
                    horarioAtencionService.save(actualizado)));
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new BusinessException(
                    "Error al aplicar la actualización parcial: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        horarioAtencionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
