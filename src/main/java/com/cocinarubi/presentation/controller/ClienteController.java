package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.request.ClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.entity.Cliente;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.service.ClienteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cliente")
@Tag(name = "Clientes", description = "CRUD básico para gestionar clientes del menú web")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cliente>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Clientes obtenidos correctamente",
                clienteService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cliente>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Cliente encontrado",
                clienteService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Cliente>> save(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Cliente creado correctamente",
                        clienteService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Cliente>> update(@PathVariable int id,
                                                       @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Cliente actualizado correctamente",
                clienteService.update(id, dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Cliente>> patch(@PathVariable int id,
                                                      @RequestBody Map<String, Object> payload) {
        if (payload.containsKey("idCliente")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.exito(200, "Cliente actualizado parcialmente",
                clienteService.patch(id, payload)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
