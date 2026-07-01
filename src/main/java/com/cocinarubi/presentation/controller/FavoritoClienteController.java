package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.FavoritoClienteService;
import com.cocinarubi.presentation.dto.request.FavoritoClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.FavoritoClienteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoritoCliente")
@Tag(name = "Favorito Cliente", description = "CRUD de favoritos polimórficos del cliente (comida, desayuno, básico, snack, charola, bebida)")
public class FavoritoClienteController {

    private final FavoritoClienteService favoritoClienteService;

    public FavoritoClienteController(FavoritoClienteService favoritoClienteService) {
        this.favoritoClienteService = favoritoClienteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoritoClienteResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Favoritos obtenidos correctamente",
                favoritoClienteService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FavoritoClienteResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Favorito encontrado",
                favoritoClienteService.findById(id)));
    }

    @GetMapping("/cliente/{sessionToken}")
    public ResponseEntity<ApiResponse<List<FavoritoClienteResponseDTO>>> findBySessionToken(
            @PathVariable String sessionToken) {
        return ResponseEntity.ok(ApiResponse.exito(200,
                "Favoritos del cliente obtenidos correctamente",
                favoritoClienteService.findBySessionToken(sessionToken)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FavoritoClienteResponseDTO>> save(
            @Valid @RequestBody FavoritoClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Favorito creado correctamente",
                        favoritoClienteService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FavoritoClienteResponseDTO>> update(@PathVariable int id,
                                                                         @Valid @RequestBody FavoritoClienteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Favorito actualizado correctamente",
                favoritoClienteService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        favoritoClienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
