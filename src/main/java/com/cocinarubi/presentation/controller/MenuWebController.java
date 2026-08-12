package com.cocinarubi.presentation.controller;

import com.cocinarubi.domain.interfaces.IMenuWebService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.MenuWebResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint único del menú web. Retorna todas las secciones disponibles en una
 * sola petición para minimizar el round-trip del cliente.
 * Capa: Controller.
 */
@RestController
@RequestMapping("/menu-web")
@Tag(name = "Menú Web", description = "Menú completo para la aplicación web de clientes")
public class MenuWebController {

    private final IMenuWebService menuWebService;

    public MenuWebController(IMenuWebService menuWebService) {
        this.menuWebService = menuWebService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MenuWebResponseDTO>> getMenu() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Menú obtenido correctamente",
                menuWebService.getMenu()));
    }
}
