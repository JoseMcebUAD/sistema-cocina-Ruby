package com.cocinarubi.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cocinarubi.domain.service.impresion.ImpresoraService;
import com.cocinarubi.presentation.dto.request.ImpresionRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.pedido.EscPosBytesDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/impresora")
@Tag(name = "Impresora", description = "Generación de bytes ESC/POS para impresión desde el frontend vía TCP:9100")
public class ImpresoraController {

    private final ImpresoraService impresoraService;

    public ImpresoraController(ImpresoraService impresoraService) {
        this.impresoraService = impresoraService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EscPosBytesDTO>> imprimir(
            @Valid @RequestBody ImpresionRequestDTO req) {
        EscPosBytesDTO data = impresoraService.imprimir(req);
        return ResponseEntity.ok(ApiResponse.exito(
                200, "Bytes ESC/POS generados correctamente", data));
    }
}
