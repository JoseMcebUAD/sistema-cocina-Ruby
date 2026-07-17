package com.cocinarubi.presentation.controller;

import com.cocinarubi.DBConstants;
import com.cocinarubi.aop.SkipAudit;
import com.cocinarubi.domain.service.AuditoriaService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.AuditoriaResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@SkipAudit
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RestController
@RequestMapping("/auditoria")
@Tag(name = "Auditoría", description = "Consulta del trail de operaciones de escritura")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditoriaResponseDTO>>> findConFiltros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) DBConstants.TipoOperacion tipoOperacion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditoriaResponseDTO> resultado = auditoriaService.findConFiltros(
                desde, hasta, idUsuario, tipoOperacion, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.exito(200, "Registros de auditoría obtenidos correctamente", resultado));
    }
}
