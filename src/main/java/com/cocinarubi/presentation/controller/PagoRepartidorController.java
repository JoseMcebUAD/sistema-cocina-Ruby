package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.domain.service.PagoRepartidorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        pagoRepartidorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
