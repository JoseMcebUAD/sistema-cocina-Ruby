package com.cocinarubi.domain.service;

import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.presentation.dto.request.RutaOrdenItemDTO;
import com.cocinarubi.presentation.dto.request.RutaRequestDTO;
import com.cocinarubi.presentation.dto.response.RutaResponseDTO;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestiona las rutas de entrega a domicilio. Cada ruta define un área geográfica
 * expresada como polígono en formato WKT, con su tarifa de envío y tiempo estimado.
 */
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
                .orden(dto.getOrden())
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
        existente.setOrden(dto.getOrden());
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
            // El payload JSON llega como Double; se convierte a String primero para evitar pérdida de precisión
            existente.setTarifaEnvio(new BigDecimal(payload.get("tarifaEnvio").toString()));
        }
        if (payload.containsKey("tiempoEstimadoMin")) {
            existente.setTiempoEstimadoMin((Integer) payload.get("tiempoEstimadoMin"));
        }
        if (payload.containsKey("orden")) {
            existente.setOrden((Integer) payload.get("orden"));
        }
        return toResponseDTO(rutaRepository.save(existente));
    }

    public void delete(int id) {
        if (!rutaRepository.existsById(id)) {
            throw new BusinessException("Ruta no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        // Guardar integridad referencial: clientes y pedidos a domicilio referencian la ruta
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

    @Transactional
    public List<RutaResponseDTO> reordenar(List<RutaOrdenItemDTO> items) {
        Set<Integer> ordenes = new HashSet<>();
        for (RutaOrdenItemDTO item : items) {
            if (!ordenes.add(item.getOrden())) {
                throw new BusinessException(
                        "El orden " + item.getOrden() + " está duplicado en la solicitud",
                        HttpStatus.BAD_REQUEST);
            }
        }

        List<Ruta> rutas = items.stream().map(item -> {
            Ruta ruta = findEntityById(item.getIdRuta());
            ruta.setOrden(item.getOrden());
            return ruta;
        }).collect(Collectors.toList());

        return rutaRepository.saveAll(rutas).stream()
                .sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden()))
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Ruta findEntityById(int id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Ruta no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    /**
     * Convierte un string WKT (Well-Known Text) en un objeto Geometry de JTS.
     * Lanza BusinessException con 400 si el WKT es inválido para dar feedback claro al cliente.
     */
    private Geometry parseBoundary(String wkt) {
        try {
            return new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new BusinessException("WKT de boundary inválido: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /** Serializa el boundary geográfico de vuelta a WKT para la respuesta. */
    private RutaResponseDTO toResponseDTO(Ruta ruta) {
        return new RutaResponseDTO(
                ruta.getIdRuta(),
                ruta.getNombre(),
                ruta.getBoundary().toText(),
                ruta.isActive(),
                ruta.getTarifaEnvio(),
                ruta.getTiempoEstimadoMin(),
                ruta.getOrden()
        );
    }
}
