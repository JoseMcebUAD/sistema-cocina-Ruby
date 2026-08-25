package com.cocinarubi.presentation.controller.Web;

import com.cocinarubi.domain.interfaces.web.IClienteWebService;
import com.cocinarubi.domain.service.web.PedidoWebService;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebRequestDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebResponseDTO;
import com.cocinarubi.presentation.dto.web.RutaWebResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/sesion")
    public ResponseEntity<ApiResponse<ClienteWebResponseDTO>> sesion(
            @Valid @RequestBody ClienteWebRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Sesión iniciada correctamente",
                clienteWebService.sesion(dto)));
    }

    @GetMapping("/rutas")
    public ResponseEntity<ApiResponse<List<RutaWebResponseDTO>>> rutas() {
        return ResponseEntity.ok(ApiResponse.exito(200, "Rutas obtenidas correctamente",
                clienteWebService.rutas()));
    }

    @GetMapping("/pedidos/{uuidCliente}")
    public ResponseEntity<ApiResponse<List<PedidoResponseDTO>>> ultimosPedidos(
            @PathVariable String uuidCliente) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedidos obtenidos correctamente",
                clienteWebService.ultimosPedidos(uuidCliente)));
    }

    @PostMapping("/pedidos")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> crearPedido(
            @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(201, "Pedido creado correctamente",
                        pedidoWebService.save(dto)));
    }

    @PutMapping("/pedidos/{id}")
    public ResponseEntity<ApiResponse<PedidoResponseDTO>> actualizarPedido(
            @PathVariable int id,
            @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.exito(200, "Pedido actualizado correctamente",
                pedidoWebService.update(id, dto)));
    }
}
