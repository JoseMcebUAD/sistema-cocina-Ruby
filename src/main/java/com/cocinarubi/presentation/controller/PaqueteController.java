package com.cocinarubi.presentation.controller;

import com.cocinarubi.DBConstants.Estatus;
import com.cocinarubi.domain.service.PaqueteService;
import com.cocinarubi.presentation.dto.request.PaqueteRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.PaqueteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del catálogo de {@link com.cocinarubi.domain.entity.Paquete}.
 * Capa: Controller — expone CRUD de promociones.
 */
@RestController
@RequestMapping("/paquete")
@Tag(name = "Paquete", description = "CRUD de paquetes (promociones)")
public class PaqueteController {

    private final PaqueteService paqueteService;

    public PaqueteController(PaqueteService paqueteService) {
        this.paqueteService = paqueteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaqueteResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Paquetes obtenidos correctamente",
                paqueteService.findAll()));
    }

    @GetMapping("/paginado")
    public ResponseEntity<ApiResponse<Page<PaqueteResponseDTO>>> findAllPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Paquetes obtenidos correctamente",
                paqueteService.findAllPaginado(PageRequest.of(page, size))));
    }

    @GetMapping("/disponibles-paginado")
    public ResponseEntity<ApiResponse<Page<PaqueteResponseDTO>>> findDisponiblesPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Paquetes disponibles obtenidos correctamente",
                paqueteService.findByEstatusPaginado(Estatus.DISPONIBLE, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaqueteResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Paquete encontrado",
                paqueteService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaqueteResponseDTO>> save(
            @Valid @RequestBody PaqueteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Paquete creado correctamente",
                        paqueteService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaqueteResponseDTO>> update(@PathVariable int id,
                                                                  @Valid @RequestBody PaqueteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Paquete actualizado correctamente",
                paqueteService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable int id,
            @RequestParam(defaultValue = "false") boolean saltarConfirmacion) {
        paqueteService.delete(id, saltarConfirmacion);
        return ResponseEntity.ok(ApiResponse.exito(200, "Paquete eliminado correctamente", null));
    }
}
