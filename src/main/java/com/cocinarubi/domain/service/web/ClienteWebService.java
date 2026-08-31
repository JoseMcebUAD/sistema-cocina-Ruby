package com.cocinarubi.domain.service.web;

import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.interfaces.web.IClienteWebService;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.domain.service.RutaService;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebRequestDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebResponseDTO;
import com.cocinarubi.presentation.dto.web.RutaWebResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteWebService implements IClienteWebService {

    private final ClienteRepository clienteRepository;
    private final RutaRepository rutaRepository;
    private final RutaService rutaService;
    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;

    public ClienteWebService(ClienteRepository clienteRepository,
                             RutaRepository rutaRepository,
                             RutaService rutaService,
                             PedidoRepository pedidoRepository,
                             PedidoMapper pedidoMapper) {
        this.clienteRepository = clienteRepository;
        this.rutaRepository = rutaRepository;
        this.rutaService = rutaService;
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Override
    @Transactional
    public ClienteWebResponseDTO sesion(ClienteWebRequestDTO dto) {
        String huella = computarHuella(dto);
        Optional<Cliente> existing = clienteRepository.findByUuidCliente(dto.getUuidCliente());

        Cliente cliente;
        LocalDateTime now = LocalDateTime.now();

        if (existing.isEmpty()) {
            cliente = Cliente.builder()
                    .uuidCliente(dto.getUuidCliente())
                    .sessionToken(UUID.randomUUID().toString())
                    .tokenExpiracion(now.plusDays(7))
                    .huella(huella)
                    .userAgent(dto.getUserAgent())
                    .ipAddress(dto.getIpAddress())
                    .build();
        } else {
            cliente = existing.get();
            if (cliente.getTokenExpiracion() == null || cliente.getTokenExpiracion().isBefore(now)) {
                cliente.setSessionToken(UUID.randomUUID().toString());
                cliente.setTokenExpiracion(now.plusDays(7));
            }
            cliente.setHuella(huella);
            cliente.setUserAgent(dto.getUserAgent());
            cliente.setIpAddress(dto.getIpAddress());
        }

        return toResponseDTO(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaWebResponseDTO> rutas() {
        return rutaRepository.findAll().stream()
                .filter(r -> r.isActive())
                .map(r -> RutaWebResponseDTO.builder()
                        .idRuta(r.getIdRuta())
                        .uuidRuta(r.getUuidRuta())
                        .nombre(r.getNombre())
                        .active(r.isActive())
                        .tarifaEnvio(r.getTarifaEnvio())
                        .tiempoEstimadoMin(r.getTiempoEstimadoMin())
                        .orden(r.getOrden())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaWebResponseDTO> rutasPorUbicacion(double lat, double lng) {
        return rutaService.buscarPorUbicacion(lat, lng).stream()
                .map(r -> RutaWebResponseDTO.builder()
                        .idRuta(r.getIdRuta())
                        .uuidRuta(r.getUuidRuta())
                        .nombre(r.getNombre())
                        .active(r.isActive())
                        .tarifaEnvio(r.getTarifaEnvio())
                        .tiempoEstimadoMin(r.getTiempoEstimadoMin())
                        .orden(r.getOrden())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> ultimosPedidos(String uuidCliente) {
        return pedidoRepository
                .findTop5ByUuidClienteOrderByFechaExpedicionPedidoDesc(uuidCliente)
                .stream()
                .map(pedidoMapper::toResponseDTO)
                .toList();
    }

    private ClienteWebResponseDTO toResponseDTO(Cliente c) {
        return ClienteWebResponseDTO.builder()
                .idCliente(c.getIdCliente())
                .uuidCliente(c.getUuidCliente())
                .sessionToken(c.getSessionToken())
                .tokenExpiracion(c.getTokenExpiracion())
                .huella(c.getHuella())
                .codigoCliente(c.getCodigoCliente())
                .userAgent(c.getUserAgent())
                .ipAddress(c.getIpAddress())
                .ubicacionLatitud(c.getUbicacionLatitud())
                .ubicacionLongitud(c.getUbicacionLongitud())
                .nombre(c.getNombre())
                .direccionCliente(c.getDireccionCliente())
                .telefono(c.getTelefono())
                .idRuta(c.getRuta() != null ? c.getRuta().getIdRuta() : null)
                .build();
    }

    private String computarHuella(ClienteWebRequestDTO dto) {
        String raw = String.join("|",
                nullSafe(dto.getUserAgent()),
                String.valueOf(dto.getScreenWidth()),
                String.valueOf(dto.getScreenHeight()),
                nullSafe(dto.getTimezone()),
                nullSafe(dto.getLanguage()),
                String.valueOf(dto.getColorDepth())
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }
}
