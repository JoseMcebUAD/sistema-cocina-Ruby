package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.request.UsuarioRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.UsuarioResponseDTO;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.domain.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuario")
@Tag(name = "Usuarios", description = "CRUD básico para gestionar operadores del dashboard")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Usuarios obtenidos correctamente",
                usuarioService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Usuario encontrado",
                usuarioService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> save(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Usuario creado correctamente",
                        usuarioService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> update(@PathVariable int id,
                                                                  @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Usuario actualizado correctamente",
                usuarioService.update(id, dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> patch(@PathVariable int id,
                                                                 @RequestBody Map<String, Object> payload) {
        if (payload.containsKey("idUsuario")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.exito(200, "Usuario actualizado parcialmente",
                usuarioService.patch(id, payload)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
