package com.cocinarubi.domain.interfaces.web;

import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebRequestDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebResponseDTO;
import com.cocinarubi.presentation.dto.web.RutaWebResponseDTO;

import java.util.List;

public interface IClienteWebService {

    ClienteWebResponseDTO sesion(ClienteWebRequestDTO dto);

    List<RutaWebResponseDTO> rutas();

    List<RutaWebResponseDTO> rutasPorUbicacion(double lat, double lng);

    List<PedidoResponseDTO> ultimosPedidos(String uuidCliente);
}
