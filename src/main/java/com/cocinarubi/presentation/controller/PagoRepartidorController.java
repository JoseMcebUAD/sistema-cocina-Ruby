package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.PagoRepartidorService;
import com.cocinarubi.presentation.dto.request.PagoRepartidorRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.PagoRepartidorPorFechaResponseDTO;
import com.cocinarubi.presentation.dto.response.PagoRepartidorResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pago-repartidor")
@Tag(name = "Pagos a Repartidor", description = "Gestión de pagos diarios a repartidores por zona de reparto")
public class PagoRepartidorController {

    private final PagoRepartidorService pagoRepartidorService;

    public PagoRepartidorController(PagoRepartidorService pagoRepartidorService) {
        this.pagoRepartidorService = pagoRepartidorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoRepartidorResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pagos obtenidos correctamente",
                pagoRepartidorService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoRepartidorResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pago encontrado",
                pagoRepartidorService.findById(id)));
    }

    @GetMapping("/rango")
    public ResponseEntity<ApiResponse<Page<PagoRepartidorPorFechaResponseDTO>>> findByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PagoRepartidorPorFechaResponseDTO> resultado =
                pagoRepartidorService.findByRango(desde, hasta, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.exito(200, "Pagos en rango obtenidos correctamente", resultado));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoRepartidorResponseDTO>> save(
            @RequestBody PagoRepartidorRequestDTO pagoRepartidor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Pago creado correctamente",
                        pagoRepartidorService.save(pagoRepartidor)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PagoRepartidorResponseDTO>> update(
            @RequestBody PagoRepartidorRequestDTO pagoRepartidor) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pago actualizado correctamente",
                pagoRepartidorService.update(pagoRepartidor)));
    }
}
