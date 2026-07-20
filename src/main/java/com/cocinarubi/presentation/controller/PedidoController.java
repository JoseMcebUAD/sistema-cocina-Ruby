package com.cocinarubi.presentation.controller;

import com.cocinarubi.aop.SkipAudit;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedido")
@Tag(name = "Pedido", description = "Aggregate root del sistema de órdenes (manejo de líneas y domicilio por handler)")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedidos obtenidos correctamente",
                pedidoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedido encontrado",
                pedidoService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> save(
            @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Pedido creado correctamente",
                        pedidoService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> update(@PathVariable int id,
                                                                @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedido actualizado correctamente",
                pedidoService.update(id, dto)));
    }

    @SkipAudit
    @PatchMapping("/{id}/marcar-impreso")
    public ResponseEntity<Void> marcarImpreso(@PathVariable int id) {
        pedidoService.marcarImpreso(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        pedidoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
