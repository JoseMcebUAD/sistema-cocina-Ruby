package com.cocinarubi.presentation.controller;

import com.cocinarubi.presentation.dto.request.AsignarRutasOrdenDTO;
import com.cocinarubi.presentation.dto.request.RutaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.OrdenRutaResponseDTO;
import com.cocinarubi.presentation.dto.response.RutaResponseDTO;
import com.cocinarubi.presentation.dto.response.RutaSimpleResponseDTO;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.domain.service.OrdenRutaService;
import com.cocinarubi.domain.service.RutaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ruta")
@Tag(name = "Rutas", description = "CRUD básico para gestionar zonas de reparto")
public class RutaController {

    private final RutaService rutaService;
    private final OrdenRutaService ordenRutaService;

    public RutaController(RutaService rutaService, OrdenRutaService ordenRutaService) {
        this.rutaService = rutaService;
        this.ordenRutaService = ordenRutaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RutaSimpleResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas obtenidas correctamente",
                rutaService.findAllSimple()));
    }

    @GetMapping("/mapa")
    public ResponseEntity<ApiResponse<List<RutaResponseDTO>>> findAllParaMapa() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas con coordenadas obtenidas correctamente",
                rutaService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Ruta encontrada",
                rutaService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RutaResponseDTO>> save(@Valid @RequestBody RutaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Ruta creada correctamente",
                        rutaService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaResponseDTO>> update(@PathVariable int id,
                                                               @Valid @RequestBody RutaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Ruta actualizada correctamente",
                rutaService.update(id, dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaResponseDTO>> patch(@PathVariable int id,
                                                              @RequestBody Map<String, Object> payload) {
        if (payload.containsKey("idRuta")) {
            throw new BusinessException("El ID no puede ser modificado.", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.exito(200, "Ruta actualizada parcialmente",
                rutaService.patch(id, payload)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        rutaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orden/{id}")
    public ResponseEntity<ApiResponse<List<RutaSimpleResponseDTO>>> findByOrden(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas de la orden obtenidas correctamente",
                rutaService.findByOrden(id)));
    }

    @PatchMapping("/orden")
    public ResponseEntity<ApiResponse<OrdenRutaResponseDTO>> asignarRutas(
            @Valid @RequestBody AsignarRutasOrdenDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas asignadas correctamente",
                rutaService.asignarRutas(dto)));
    }

    @PatchMapping("/orden/{id}")
    public ResponseEntity<ApiResponse<OrdenRutaResponseDTO>> actualizarTiempoEstimado(
            @PathVariable int id, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tiempo estimado actualizado",
                ordenRutaService.actualizarTiempoEstimado(id, body.get("tiempoEstimadoMin"))));
    }
}
