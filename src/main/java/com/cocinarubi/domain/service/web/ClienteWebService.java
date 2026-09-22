package com.cocinarubi.domain.service.web;

import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.interfaces.web.IClienteWebService;
import com.cocinarubi.domain.interfaces.web.SesionWebResult;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.domain.service.RutaService;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebRequestDTO;
import com.cocinarubi.presentation.dto.web.ClienteWebResponseDTO;
import com.cocinarubi.presentation.dto.web.RutaWebResponseDTO;
import com.cocinarubi.util.HashUtils;
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
    public SesionWebResult sesion(ClienteWebRequestDTO dto) {
        String huella = computarHuella(dto);
        Optional<Cliente> existing = clienteRepository.findByUuidCliente(dto.getUuidCliente());

        Cliente cliente;
        LocalDateTime now = LocalDateTime.now();
        // tokenPlano: se emite al cliente solo por cookie HttpOnly; en BD se guarda su hash.
        String tokenPlano;

        if (existing.isEmpty()) {
            tokenPlano = UUID.randomUUID().toString();
            cliente = Cliente.builder()
                    .uuidCliente(dto.getUuidCliente())
                    .sessionTokenHash(HashUtils.sha256Hex(tokenPlano))
                    .tokenExpiracion(now.plusDays(7))
                    .huella(huella)
                    .userAgent(dto.getUserAgent())
                    .ipAddress(dto.getIpAddress())
                    .build();
        } else {
            cliente = existing.get();
            boolean expirado = cliente.getTokenExpiracion() == null
                    || cliente.getTokenExpiracion().isBefore(now);
            if (expirado) {
                tokenPlano = UUID.randomUUID().toString();
                cliente.setSessionTokenHash(HashUtils.sha256Hex(tokenPlano));
                cliente.setTokenExpiracion(now.plusDays(7));
            } else {
                // Token vigente: la sesion se prolonga, pero el cliente ya lo tiene en su cookie.
                // No podemos devolverselo (no lo tenemos en claro); enviamos null para que el
                // controller no sobrescriba la cookie existente.
                tokenPlano = null;
            }
            cliente.setHuella(huella);
            cliente.setUserAgent(dto.getUserAgent());
            cliente.setIpAddress(dto.getIpAddress());
        }

        return new SesionWebResult(toResponseDTO(clienteRepository.save(cliente)), tokenPlano);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaWebResponseDTO> rutas() {
        return rutaRepository.findAll().stream()
                .filter(r -> r.isActive() && !"General".equalsIgnoreCase(r.getNombre()))
                .map(r -> RutaWebResponseDTO.builder()
                        .idRuta(r.getIdRuta())
                        .uuidRuta(r.getUuidRuta())
                        .nombre(r.getNombre())
                        .active(r.isActive())
                        .tarifaEnvio(r.getTarifaEnvio())
                        .idOrdenRuta(r.getOrdenRuta() != null ? r.getOrdenRuta().getIdOrdenRuta() : null)
                        // OrdenRuta: ventana horaria de reparto del grupo
                        .horaLlegadaDesde(r.getOrdenRuta() != null ? r.getOrdenRuta().getHoraLlegadaDesde() : null)
                        .horaLlegadaHasta(r.getOrdenRuta() != null ? r.getOrdenRuta().getHoraLlegadaHasta() : null)
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
                        .idOrdenRuta(r.getOrdenRuta() != null ? r.getOrdenRuta().getIdOrdenRuta() : null)
                        // OrdenRuta: ventana horaria de reparto del grupo
                        .horaLlegadaDesde(r.getOrdenRuta() != null ? r.getOrdenRuta().getHoraLlegadaDesde() : null)
                        .horaLlegadaHasta(r.getOrdenRuta() != null ? r.getOrdenRuta().getHoraLlegadaHasta() : null)
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
                .tokenExpiracion(c.getTokenExpiracion())
                .codigoCliente(c.getCodigoCliente())
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
