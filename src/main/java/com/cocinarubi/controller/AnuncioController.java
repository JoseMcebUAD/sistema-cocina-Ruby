package com.cocinarubi.controller;

import com.cocinarubi.dto.response.ApiResponse;
import com.cocinarubi.entity.Anuncio;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.service.AnuncioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/anuncio")
@Tag(name = "Anuncios", description = "CRUD básico para gestionar anuncios")
public class AnuncioController {

    private final AnuncioService anuncioService;
    private final ObjectMapper objectMapper;

    public AnuncioController(AnuncioService anuncioService, ObjectMapper objectMapper) {
        this.anuncioService = anuncioService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Anuncio>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Anuncios obtenidos correctamente",
                anuncioService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Anuncio>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Anuncio encontrado",
                anuncioService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Anuncio>> save(@RequestBody Anuncio anuncio) {
        anuncio.setIdAnuncio(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Anuncio creado correctamente",
                        anuncioService.save(anuncio)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Anuncio>> update(@RequestBody Anuncio anuncio) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Anuncio actualizado correctamente",
                anuncioService.save(anuncio)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Anuncio>> patch(@PathVariable int id,
                                                       @RequestBody Map<String, Object> payload) {
        Anuncio existente = anuncioService.findById(id);
        if (payload.containsKey("idAnuncio")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        try {
            Anuncio actualizado = objectMapper.updateValue(existente, payload);
            return ResponseEntity.ok(ApiResponse.exito(200, "Anuncio actualizado parcialmente",
                    anuncioService.save(actualizado)));
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new BusinessException(
                    "Error al aplicar la actualización parcial: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        anuncioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
