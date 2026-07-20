package com.cocinarubi.domain.service;

import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.presentation.dto.request.ClienteRequestDTO;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * Gestiona los clientes del sistema: dispositivos/sesiones que realizan pedidos.
 * Cada cliente se identifica de forma única por su sessionToken.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final RutaRepository rutaRepository;

    public ClienteService(ClienteRepository clienteRepository, RutaRepository rutaRepository) {
        this.clienteRepository = clienteRepository;
        this.rutaRepository = rutaRepository;
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Cliente findById(int id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Cliente no encontrado, regrese a la ventana anterior" + id, HttpStatus.NOT_FOUND));
    }

    public Cliente save(ClienteRequestDTO dto) {
        // El sessionToken identifica al dispositivo/sesión; no puede estar duplicado
        if (clienteRepository.existsBySessionToken(dto.getSessionToken())) {
            throw new BusinessException(
                    "Ya existe un cliente con ese session token", HttpStatus.CONFLICT);
        }
        Ruta ruta = resolveRuta(dto.getIdRuta());
        return clienteRepository.save(buildCliente(dto, ruta));
    }

    public Cliente update(int id, ClienteRequestDTO dto) {
        Cliente existente = findById(id);
        // Solo se valida unicidad si el token realmente cambió
        if (!existente.getSessionToken().equals(dto.getSessionToken())
                && clienteRepository.existsBySessionToken(dto.getSessionToken())) {
            throw new BusinessException(
                    "Ya existe un cliente con ese session token", HttpStatus.CONFLICT);
        }
        Ruta ruta = resolveRuta(dto.getIdRuta());
        existente.setRuta(ruta);
        existente.setUuidCliente(dto.getUuidCliente());
        existente.setSessionToken(dto.getSessionToken());
        existente.setCodigoCliente(dto.getCodigoCliente());
        existente.setUserAgent(dto.getUserAgent());
        existente.setIpAddress(dto.getIpAddress());
        existente.setUbicacionLatitud(dto.getUbicacionLatitud());
        existente.setUbicacionLongitud(dto.getUbicacionLongitud());
        existente.setNombre(dto.getNombre());
        existente.setDireccionCliente(dto.getDireccionCliente());
        existente.setTelefono(dto.getTelefono());
        return clienteRepository.save(existente);
    }

    public Cliente patch(int id, Map<String, Object> payload) {
        Cliente existente = findById(id);
        if (payload.containsKey("nombre")) existente.setNombre((String) payload.get("nombre"));
        if (payload.containsKey("telefono")) existente.setTelefono((String) payload.get("telefono"));
        if (payload.containsKey("direccionCliente")) existente.setDireccionCliente((String) payload.get("direccionCliente"));
        if (payload.containsKey("userAgent")) existente.setUserAgent((String) payload.get("userAgent"));
        if (payload.containsKey("ipAddress")) existente.setIpAddress((String) payload.get("ipAddress"));
        if (payload.containsKey("codigoCliente")) existente.setCodigoCliente((String) payload.get("codigoCliente"));
        if (payload.containsKey("idRuta")) {
            // Se permite enviar null explícitamente para desasignar la ruta del cliente
            Object idRutaVal = payload.get("idRuta");
            existente.setRuta(idRutaVal == null ? null : resolveRuta(((Number) idRutaVal).intValue()));
        }
        return clienteRepository.save(existente);
    }

    public void delete(int id) {
        // Verificar existencia antes de borrar para devolver 404 en lugar del silencioso no-op de JPA
        if (!clienteRepository.existsById(id)) {
            throw new BusinessException("Cliente no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        clienteRepository.deleteById(id);
    }

    /** Devuelve null si idRuta es null; la ruta es opcional para un cliente. */
    private Ruta resolveRuta(Integer idRuta) {
        if (idRuta == null) return null;
        return rutaRepository.findById(idRuta)
                .orElseThrow(() -> new BusinessException(
                        "Ruta no encontrada con id: " + idRuta, HttpStatus.NOT_FOUND));
    }

    private Cliente buildCliente(ClienteRequestDTO dto, Ruta ruta) {
        return Cliente.builder()
                .ruta(ruta)
                .uuidCliente(dto.getUuidCliente())
                .sessionToken(dto.getSessionToken())
                .codigoCliente(dto.getCodigoCliente())
                .userAgent(dto.getUserAgent())
                .ipAddress(dto.getIpAddress())
                .ubicacionLatitud(dto.getUbicacionLatitud())
                .ubicacionLongitud(dto.getUbicacionLongitud())
                .nombre(dto.getNombre())
                .direccionCliente(dto.getDireccionCliente())
                .telefono(dto.getTelefono())
                .build();
    }
}
