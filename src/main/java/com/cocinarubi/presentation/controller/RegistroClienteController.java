package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.RegistroClienteService;
import com.cocinarubi.presentation.dto.request.RegistroClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.RegistroClienteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registro-cliente")
@Tag(name = "Registro Cliente", description = "Directorio de clientes manuales para pedidos creados desde cocina")
public class RegistroClienteController {

    private final RegistroClienteService registroClienteService;

    public RegistroClienteController(RegistroClienteService registroClienteService) {
        this.registroClienteService = registroClienteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RegistroClienteResponseDTO>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registros de cliente obtenidos correctamente",
                registroClienteService.findAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistroClienteResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registro de cliente encontrado",
                registroClienteService.findById(id)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<Page<RegistroClienteResponseDTO>>> buscarPorTelefono(
            @RequestParam String telefono,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registros de cliente encontrados",
                registroClienteService.findByTelefono(telefono, PageRequest.of(page, size))));
    }

    @GetMapping("/buscar-nombre")
    public ResponseEntity<ApiResponse<Page<RegistroClienteResponseDTO>>> buscarPorNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registros de cliente encontrados",
                registroClienteService.findByNombre(nombre, PageRequest.of(page, size))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegistroClienteResponseDTO>> save(
            @Valid @RequestBody RegistroClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Registro de cliente creado correctamente",
                        registroClienteService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistroClienteResponseDTO>> update(
            @PathVariable int id, @Valid @RequestBody RegistroClienteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Registro de cliente actualizado correctamente",
                registroClienteService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        registroClienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
