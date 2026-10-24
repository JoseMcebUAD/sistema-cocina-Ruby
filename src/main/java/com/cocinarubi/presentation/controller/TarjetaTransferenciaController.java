package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.TarjetaTransferenciaService;
import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaEstatusRequestDTO;
import com.cocinarubi.presentation.dto.request.TarjetaTransferenciaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de tarjetas de transferencia bancaria.
 * Permite al administrador registrar, cambiar el estatus y eliminar tarjetas
 * que se muestran en el checkout web para recibir pagos por transferencia.
 * Capa: Controller — expone el CRUD de tarjetas de transferencia.
 */
@RestController
@RequestMapping("/tarjeta-transferencia")
@Tag(name = "Tarjeta de transferencia", description = "Gestión de tarjetas bancarias para pagos por transferencia")
public class TarjetaTransferenciaController {

    private final TarjetaTransferenciaService tarjetaService;

    public TarjetaTransferenciaController(TarjetaTransferenciaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TarjetaTransferenciaResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tarjetas obtenidas correctamente",
                tarjetaService.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TarjetaTransferenciaResponseDTO>> save(
            @Valid @RequestBody TarjetaTransferenciaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Tarjeta registrada correctamente",
                        tarjetaService.save(dto)));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TarjetaTransferenciaResponseDTO>> update(
            @PathVariable int id,
            @Valid @RequestBody TarjetaTransferenciaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tarjeta actualizada correctamente",
                tarjetaService.update(id, dto)));
    }

    @PatchMapping("/{id}/estatus")
    public ResponseEntity<ApiResponse<TarjetaTransferenciaResponseDTO>> cambiarEstatus(
            @PathVariable int id,
            @Valid @RequestBody TarjetaTransferenciaEstatusRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Estatus actualizado correctamente",
                tarjetaService.cambiarEstatus(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        tarjetaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
