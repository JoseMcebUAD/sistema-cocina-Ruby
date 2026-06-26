package com.cocinarubi.service;

import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.dto.request.RutaRequestDTO;
import com.cocinarubi.dto.response.RutaResponseDTO;
import com.cocinarubi.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RutaService {

    private final RutaRepository rutaRepository;

    public RutaService(RutaRepository rutaRepository) {
        this.rutaRepository = rutaRepository;
    }

    public List<RutaResponseDTO> findAll() {
        return rutaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RutaResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    public RutaResponseDTO save(RutaRequestDTO dto) {
        Geometry boundary = parseBoundary(dto.getBoundaryWkt());
        Ruta ruta = Ruta.builder()
                .nombre(dto.getNombre())
                .boundary(boundary)
                .isActive(dto.isActive())
                .tarifaEnvio(dto.getTarifaEnvio())
                .tiempoEstimadoMin(dto.getTiempoEstimadoMin())
                .build();
        return toResponseDTO(rutaRepository.save(ruta));
    }

    public RutaResponseDTO update(int id, RutaRequestDTO dto) {
        Ruta existente = findEntityById(id);
        existente.setNombre(dto.getNombre());
        existente.setBoundary(parseBoundary(dto.getBoundaryWkt()));
        existente.setActive(dto.isActive());
        existente.setTarifaEnvio(dto.getTarifaEnvio());
        existente.setTiempoEstimadoMin(dto.getTiempoEstimadoMin());
        return toResponseDTO(rutaRepository.save(existente));
    }

    public RutaResponseDTO patch(int id, Map<String, Object> payload) {
        Ruta existente = findEntityById(id);
        if (payload.containsKey("nombre")) {
            existente.setNombre((String) payload.get("nombre"));
        }
        if (payload.containsKey("boundaryWkt")) {
            existente.setBoundary(parseBoundary((String) payload.get("boundaryWkt")));
        }
        if (payload.containsKey("isActive")) {
            existente.setActive((Boolean) payload.get("isActive"));
        }
        if (payload.containsKey("tarifaEnvio")) {
            existente.setTarifaEnvio(new BigDecimal(payload.get("tarifaEnvio").toString()));
        }
        if (payload.containsKey("tiempoEstimadoMin")) {
            existente.setTiempoEstimadoMin((Integer) payload.get("tiempoEstimadoMin"));
        }
        return toResponseDTO(rutaRepository.save(existente));
    }

    public void delete(int id) {
        if (!rutaRepository.existsById(id)) {
            throw new BusinessException("Ruta no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (rutaRepository.countClientesConRuta(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar la ruta porque está asignada a clientes existentes",
                    HttpStatus.CONFLICT);
        }
        if (rutaRepository.countPedidosDomicilioConRuta(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar la ruta porque está referenciada en pedidos a domicilio",
                    HttpStatus.CONFLICT);
        }
        rutaRepository.deleteById(id);
    }

    private Ruta findEntityById(int id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Ruta no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    private Geometry parseBoundary(String wkt) {
        try {
            return new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new BusinessException("WKT de boundary inválido: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private RutaResponseDTO toResponseDTO(Ruta ruta) {
        return new RutaResponseDTO(
                ruta.getIdRuta(),
                ruta.getNombre(),
                ruta.getBoundary().toText(),
                ruta.isActive(),
                ruta.getTarifaEnvio(),
                ruta.getTiempoEstimadoMin()
        );
    }
}
