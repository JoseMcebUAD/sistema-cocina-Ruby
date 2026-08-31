package com.cocinarubi.presentation.controller.Web;

import com.cocinarubi.domain.interfaces.web.IClienteWebService;
import com.cocinarubi.domain.service.web.PedidoWebService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebRequestDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebResponseDTO;
import com.cocinarubi.presentation.dto.web.RutaWebResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/web")
@Tag(name = "Web - Clientes", description = "Endpoints públicos para la app web de clientes")
public class ClienteWebController {

    private final IClienteWebService clienteWebService;
    private final PedidoWebService pedidoWebService;

    public ClienteWebController(IClienteWebService clienteWebService, PedidoWebService pedidoWebService) {
        this.clienteWebService = clienteWebService;
        this.pedidoWebService = pedidoWebService;
    }

    // Inicia o recupera la sesión del cliente; prioriza la cookie existente sobre el UUID del body y la genera si no hay ninguno
    @PostMapping("/sesion")
    public ResponseEntity<ApiResponse<ClienteWebResponseDTO>> sesion(
            @Valid @RequestBody ClienteWebRequestDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        String uuid = resolverUuid(request, dto.getUuidCliente());
        dto.setUuidCliente(uuid);

        ResponseCookie cookie = ResponseCookie.from("uuid_cliente", uuid)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofDays(365))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.exito(200, "Sesión iniciada correctamente",
                clienteWebService.sesion(dto)));
    }

    private String resolverUuid(HttpServletRequest request, String uuidDelBody) {
        if (request.getCookies() != null) {
            String desdeCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "uuid_cliente".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            if (desdeCookie != null && !desdeCookie.isBlank()) return desdeCookie;
        }
        if (uuidDelBody != null && !uuidDelBody.isBlank()) return uuidDelBody;
        return UUID.randomUUID().toString();
    }

    // Devuelve la lista de rutas de entrega disponibles
    @GetMapping("/rutas")
    public ResponseEntity<ApiResponse<List<RutaWebResponseDTO>>> rutas() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas obtenidas correctamente",
                clienteWebService.rutas()));
    }

    // Detecta las rutas cuyo polígono contiene la ubicación dada; devuelve lista para que el cliente elija si hay solapamiento
    @GetMapping("/rutas/por-ubicacion")
    public ResponseEntity<ApiResponse<List<RutaWebResponseDTO>>> rutasPorUbicacion(
            @RequestParam double lat,
            @RequestParam double lng) {
        List<RutaWebResponseDTO> rutas = clienteWebService.rutasPorUbicacion(lat, lng);
        if (rutas.isEmpty()) {
            throw new BusinessException(
                    "La ubicación no pertenece a ninguna zona de reparto", HttpStatus.NOT_FOUND);
        }
        String mensaje = rutas.size() == 1
                ? "Ruta encontrada"
                : "La ubicación pertenece a " + rutas.size() + " zonas, el cliente debe elegir";
        return ResponseEntity.ok(ApiResponse.exito(200, mensaje, rutas));
    }

    // Retorna los últimos pedidos asociados al UUID del cliente
    @GetMapping("/pedidos/{uuidCliente}")
    public ResponseEntity<ApiResponse<List<PedidoResponseDTO>>> ultimosPedidos(
            @PathVariable String uuidCliente) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedidos obtenidos correctamente",
                clienteWebService.ultimosPedidos(uuidCliente)));
    }

    // Registra un nuevo pedido y retorna 201 con el recurso creado
    @PostMapping("/pedidos")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> crearPedido(
            @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Pedido creado correctamente",
                        pedidoWebService.save(dto)));
    }

    // Actualiza los datos de un pedido existente identificado por su ID
    @PutMapping("/pedidos/{id}")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> actualizarPedido(
            @PathVariable int id,
            @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedido actualizado correctamente",
                pedidoWebService.update(id, dto)));
    }
}
