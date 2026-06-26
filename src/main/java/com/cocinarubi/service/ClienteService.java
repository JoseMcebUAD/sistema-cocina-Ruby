package com.cocinarubi.service;

import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.dto.request.ClienteRequestDTO;
import com.cocinarubi.entity.Cliente;
import com.cocinarubi.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

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
                        "Cliente no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public Cliente save(ClienteRequestDTO dto) {
        if (clienteRepository.existsBySessionToken(dto.getSessionToken())) {
            throw new BusinessException(
                    "Ya existe un cliente con ese session token", HttpStatus.CONFLICT);
        }
        Ruta ruta = resolveRuta(dto.getIdRuta());
        return clienteRepository.save(buildCliente(dto, ruta));
    }

    public Cliente update(int id, ClienteRequestDTO dto) {
        Cliente existente = findById(id);
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
            Object idRutaVal = payload.get("idRuta");
            existente.setRuta(idRutaVal == null ? null : resolveRuta(((Number) idRutaVal).intValue()));
        }
        return clienteRepository.save(existente);
    }

    public void delete(int id) {
        if (!clienteRepository.existsById(id)) {
            throw new BusinessException("Cliente no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        clienteRepository.deleteById(id);
    }

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
