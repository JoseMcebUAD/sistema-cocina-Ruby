package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.service.MenuDigitalService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el endpoint para generar el texto del menú diario listo para WhatsApp.
 * Capa: Controller — delega toda la lógica a MenuDigitalService.
 */
@RestController
@RequestMapping("/menu-digital")
@Tag(name = "Menú Digital", description = "Genera el texto del menú diario para copiar y enviar por WhatsApp")
public class MenuDigitalController {

    private final MenuDigitalService menuDigitalService;

    public MenuDigitalController(MenuDigitalService menuDigitalService) {
        this.menuDigitalService = menuDigitalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> obtenerMenu() {
        return ResponseEntity.ok(
                ApiResponse.exito(200, "Menú digital generado correctamente",
                        menuDigitalService.generarMenu())
        );
    }
}
