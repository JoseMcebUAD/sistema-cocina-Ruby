package com.cocinarubi.presentation.controller.Web;

import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.interfaces.web.IClienteWebService;
import com.cocinarubi.domain.interfaces.web.SesionWebResult;
import com.cocinarubi.domain.service.CodigoClienteService;
import com.cocinarubi.domain.service.web.PedidoWebService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.CodigoClienteResponseDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebRequestDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebResponseDTO;
import com.cocinarubi.presentation.dto.web.RutaWebResponseDTO;
import com.cocinarubi.presentation.filter.ClienteSessionFilter;
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
    private final CodigoClienteService codigoClienteService;

    public ClienteWebController(IClienteWebService clienteWebService,
                                PedidoWebService pedidoWebService,
                                CodigoClienteService codigoClienteService) {
        this.clienteWebService = clienteWebService;
        this.pedidoWebService = pedidoWebService;
        this.codigoClienteService = codigoClienteService;
    }

    // Inicia o recupera la sesión del cliente; prioriza la cookie existente sobre el UUID del body y la genera si no hay ninguno
    @PostMapping("/sesion")
    public ResponseEntity<ApiResponse<ClienteWebResponseDTO>> sesion(
            @Valid @RequestBody ClienteWebRequestDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        String uuid = resolverUuid(request, dto.getUuidCliente());
        dto.setUuidCliente(uuid);

        // Cookie de identidad del navegador (persistente 1 año, no es un secreto)
        ResponseCookie cookieUuid = ResponseCookie.from("uuid_cliente", uuid)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofDays(365))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUuid.toString());

        SesionWebResult resultado = clienteWebService.sesion(dto);
        // Solo emitimos la cookie de sesion cuando el service genera un token nuevo (renovacion o alta).
        // El token vive solo en la cookie HttpOnly; nunca en el body ni en logs.
        if (resultado.tokenPlano() != null) {
            ResponseCookie cookieToken = ResponseCookie.from("session_token", resultado.tokenPlano())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .maxAge(Duration.ofDays(7))
                    .path("/")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookieToken.toString());
        }

        return ResponseEntity.ok(ApiResponse.exito(200, "Sesión iniciada correctamente",
                resultado.dto()));
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

    // Retorna los últimos pedidos del cliente autenticado (resuelto desde el session token)
    @GetMapping("/pedidos")
    public ResponseEntity<ApiResponse<List<PedidoResponseDTO>>> ultimosPedidos(
            HttpServletRequest request) {
        // ClienteSessionFilter: valida el session token y expone el Cliente autenticado
        Cliente autenticado = (Cliente) request.getAttribute(ClienteSessionFilter.CLIENTE_ATTR);
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedidos obtenidos correctamente",
                clienteWebService.ultimosPedidos(autenticado.getUuidCliente())));
    }

    // Consulta el estatus y la tarifa de un código de cliente especial
    @GetMapping("/codigo-cliente/{codigo}")
    public ResponseEntity<ApiResponse<CodigoClienteResponseDTO>> consultarCodigoCliente(
            @PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Código de cliente encontrado",
                codigoClienteService.findByCodigoCliente(codigo)));
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
