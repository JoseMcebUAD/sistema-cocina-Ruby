package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.CorteCajaImpresionService;
import com.cocinarubi.presentation.dto.request.CorteCajaImpresionRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.pedido.EscPosBytesDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el endpoint de impresión del corte de caja.
 * Capa: Controller — delega toda la lógica a {@link CorteCajaImpresionService}.
 * Acceso restringido a JEFA_COCINA mediante SecurityConfig.
 */
@RestController
@RequestMapping("/corte-caja")
public class CorteCajaController {

    private final CorteCajaImpresionService corteCajaImpresionService;

    public CorteCajaController(CorteCajaImpresionService corteCajaImpresionService) {
        this.corteCajaImpresionService = corteCajaImpresionService;
    }

    /**
     * Genera el ticket ESC/POS del corte de caja del día indicado.
     * El campo {@code escposData} de la respuesta contiene los bytes en Base64;
     * el frontend los decodifica y los envía al socket TCP:9100 de la impresora.
     */
    @PostMapping("/impresion")
    public ResponseEntity<ApiResponse<EscPosBytesDTO>> imprimirCorte(
            @Valid @RequestBody CorteCajaImpresionRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.exito(200,
                "Corte de caja generado correctamente",
                corteCajaImpresionService.generarCorte(request)));
    }
}
