package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.request.AnuncioRequestDTO;
import com.cocinarubi.presentation.dto.response.AnuncioResponseDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.domain.service.AnuncioService;
import com.cocinarubi.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/anuncio")
@Tag(name = "Anuncios", description = "Gestión de anuncios con prioridad, imagen y productos asociados")
public class AnuncioController {

    private final AnuncioService anuncioService;
    private final ObjectMapper objectMapper;

    public AnuncioController(AnuncioService anuncioService, ObjectMapper objectMapper) {
        this.anuncioService = anuncioService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnuncioResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Anuncios obtenidos correctamente",
                anuncioService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnuncioResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Anuncio encontrado",
                anuncioService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AnuncioResponseDTO>> save(
            @RequestBody AnuncioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Anuncio creado correctamente",
                        anuncioService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnuncioResponseDTO>> update(
            @PathVariable int id,
            @RequestBody AnuncioRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Anuncio actualizado correctamente",
                anuncioService.update(id, dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AnuncioResponseDTO>> patch(
            @PathVariable int id,
            @RequestBody Map<String, Object> payload) {
        if (payload.containsKey("idAnuncio")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        AnuncioResponseDTO existente = anuncioService.findById(id);
        try {
            AnuncioRequestDTO actualizado = objectMapper.updateValue(
                    objectMapper.convertValue(existente, AnuncioRequestDTO.class), payload);
            return ResponseEntity.ok(ApiResponse.exito(200, "Anuncio actualizado parcialmente",
                    anuncioService.update(id, actualizado)));
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new BusinessException(
                    "Error al aplicar la actualización parcial: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AnuncioResponseDTO>> uploadImagen(
            @PathVariable int id,
            @RequestParam("imagen") MultipartFile imagen) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Imagen subida correctamente",
                anuncioService.uploadImagen(id, imagen)));
    }

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<ApiResponse<AnuncioResponseDTO>> deleteImagen(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Imagen eliminada correctamente",
                anuncioService.deleteImagen(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        anuncioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
