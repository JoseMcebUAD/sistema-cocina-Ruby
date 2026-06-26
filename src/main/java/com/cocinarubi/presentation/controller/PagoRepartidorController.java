package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.entity.PagoRepartidor;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.service.PagoRepartidorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pago-repartidor")
@Tag(name = "Pagos a Repartidor", description = "CRUD básico para gestionar pagos diarios a repartidores")
public class PagoRepartidorController {

    private final PagoRepartidorService pagoRepartidorService;
    private final ObjectMapper objectMapper;

    public PagoRepartidorController(PagoRepartidorService pagoRepartidorService, ObjectMapper objectMapper) {
        this.pagoRepartidorService = pagoRepartidorService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoRepartidor>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pagos obtenidos correctamente",
                pagoRepartidorService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoRepartidor>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pago encontrado",
                pagoRepartidorService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoRepartidor>> save(@RequestBody PagoRepartidor pagoRepartidor) {
        pagoRepartidor.setIdPagoRepartidor(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Pago creado correctamente",
                        pagoRepartidorService.save(pagoRepartidor)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PagoRepartidor>> update(@RequestBody PagoRepartidor pagoRepartidor) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pago actualizado correctamente",
                pagoRepartidorService.save(pagoRepartidor)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoRepartidor>> patch(@PathVariable int id,
                                                              @RequestBody Map<String, Object> payload) {
        PagoRepartidor existente = pagoRepartidorService.findById(id);
        if (payload.containsKey("idPagoRepartidor")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        try {
            PagoRepartidor actualizado = objectMapper.updateValue(existente, payload);
            return ResponseEntity.ok(ApiResponse.exito(200, "Pago actualizado parcialmente",
                    pagoRepartidorService.save(actualizado)));
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new BusinessException(
                    "Error al aplicar la actualización parcial: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        pagoRepartidorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
