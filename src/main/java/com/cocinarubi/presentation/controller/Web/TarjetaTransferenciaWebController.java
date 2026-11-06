package com.cocinarubi.presentation.controller.Web;

import com.cocinarubi.domain.service.TarjetaTransferenciaService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.TarjetaTransferenciaActivaDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público del carrito web para consultar la tarjeta de transferencia activa.
 * No requiere autenticación — cubierto por la regla permitAll() de /web/** en SecurityConfig.
 * Capa: Controller — expone la tarjeta activa para el checkout.
 */
@RestController
@RequestMapping("/web/tarjeta-transferencia")
@Tag(name = "Tarjeta transferencia web", description = "Endpoint público para consultar la tarjeta activa de transferencia")
public class TarjetaTransferenciaWebController {

    private final TarjetaTransferenciaService tarjetaService;

    public TarjetaTransferenciaWebController(TarjetaTransferenciaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @GetMapping("/activa")
    public ResponseEntity<ApiResponse<TarjetaTransferenciaActivaDTO>> obtenerActiva() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Tarjeta activa obtenida",
                tarjetaService.obtenerActiva()));
    }
}
