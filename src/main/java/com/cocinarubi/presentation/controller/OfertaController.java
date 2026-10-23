package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.OfertaPreviewService;
import com.cocinarubi.domain.service.OfertaService;
import com.cocinarubi.presentation.dto.request.OfertaPreviewRequestDTO;
import com.cocinarubi.presentation.dto.request.OfertaRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.OfertaPreviewResponseDTO;
import com.cocinarubi.presentation.dto.response.OfertaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del catálogo de {@link com.cocinarubi.domain.entity.Oferta}.
 * Capa: Controller — expone el CRUD de ofertas (precios especiales por combinación).
 */
@RestController
@RequestMapping("/oferta")
@Tag(name = "Oferta", description = "CRUD de ofertas (precios especiales por combinación de productos)")
public class OfertaController {

    private final OfertaService ofertaService;
    private final OfertaPreviewService ofertaPreviewService;

    public OfertaController(OfertaService ofertaService,
                             OfertaPreviewService ofertaPreviewService) {
        this.ofertaService = ofertaService;
        this.ofertaPreviewService = ofertaPreviewService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OfertaResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Ofertas obtenidas correctamente",
                ofertaService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfertaResponseDTO>> findById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Oferta encontrada",
                ofertaService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OfertaResponseDTO>> save(@Valid @RequestBody OfertaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Oferta creada correctamente",
                        ofertaService.save(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OfertaResponseDTO>> update(@PathVariable int id,
                                                                  @Valid @RequestBody OfertaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Oferta actualizada correctamente",
                ofertaService.update(id, dto)));
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<OfertaResponseDTO>> toggleActivo(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Estado de oferta actualizado",
                ofertaService.toggleActivo(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id,
                                       @RequestParam(defaultValue = "false") boolean saltarConfirmacion) {
        ofertaService.delete(id, saltarConfirmacion);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preview-pedido")
    public ResponseEntity<ApiResponse<List<OfertaPreviewResponseDTO>>> previewPedido(
            @RequestBody OfertaPreviewRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Preview de ofertas calculado",
                ofertaPreviewService.preview(dto)));
    }
}
