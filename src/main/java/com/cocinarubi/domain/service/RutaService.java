package com.cocinarubi.domain.service;

import com.cocinarubi.dao.OrdenRutaRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.presentation.dto.request.AsignarRutasOrdenDTO;
import com.cocinarubi.presentation.dto.request.RutaRequestDTO;
import com.cocinarubi.presentation.dto.response.OrdenRutaResponseDTO;
import com.cocinarubi.presentation.dto.response.RutaResponseDTO;
import com.cocinarubi.presentation.dto.response.RutaSimpleResponseDTO;
import com.cocinarubi.domain.entity.OrdenRuta;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gestiona las rutas de entrega a domicilio. Cada ruta define un área geográfica
 * expresada como polígono en formato WKT, con su tarifa de envío, agrupadas bajo OrdenRuta.
 */
@Service
public class RutaService {

    private final RutaRepository rutaRepository;
    private final OrdenRutaRepository ordenRutaRepository;

    public RutaService(RutaRepository rutaRepository, OrdenRutaRepository ordenRutaRepository) {
        this.rutaRepository = rutaRepository;
        this.ordenRutaRepository = ordenRutaRepository;
    }

    public List<RutaSimpleResponseDTO> findAllSimple() {
        return rutaRepository.findAll().stream()
                .map(this::toSimpleResponseDTO)
                .collect(Collectors.toList());
    }

    public List<RutaResponseDTO> findAll() {
        return rutaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RutaResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    public List<RutaSimpleResponseDTO> findByOrden(int idOrden) {
        return rutaRepository.findByOrdenRutaId(idOrden).stream()
                .map(this::toSimpleResponseDTO)
                .collect(Collectors.toList());
    }

    public RutaResponseDTO save(RutaRequestDTO dto) {
        Geometry boundary = parseBoundary(dto.getBoundaryWkt());
        Ruta ruta = Ruta.builder()
                .uuidRuta(UUID.randomUUID().toString())
                .nombre(dto.getNombre())
                .boundary(boundary)
                .isActive(dto.isActive())
                .tarifaEnvio(dto.getTarifaEnvio())
                .build();
        return toResponseDTO(rutaRepository.save(ruta));
    }

    public RutaResponseDTO update(int id, RutaRequestDTO dto) {
        Ruta existente = findEntityById(id);
        existente.setNombre(dto.getNombre());
        existente.setBoundary(parseBoundary(dto.getBoundaryWkt()));
        existente.setActive(dto.isActive());
        existente.setTarifaEnvio(dto.getTarifaEnvio());
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
        return toResponseDTO(rutaRepository.save(existente));
    }

    public void delete(int id) {
        if (!rutaRepository.existsById(id)) {
            throw new BusinessException("Ruta no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (rutaRepository.existsClientesConRuta(id)) {
            throw new BusinessException(
                    "No se puede eliminar la ruta porque está asignada a clientes existentes",
                    HttpStatus.CONFLICT);
        }
        if (rutaRepository.existsPedidosDomicilioConRuta(id)) {
            throw new BusinessException(
                    "No se puede eliminar la ruta porque está referenciada en pedidos a domicilio",
                    HttpStatus.CONFLICT);
        }
        rutaRepository.deleteById(id);
    }

    @Transactional
    public OrdenRutaResponseDTO asignarRutas(AsignarRutasOrdenDTO dto) {
        OrdenRuta orden = ordenRutaRepository.findById(dto.getIdOrdenRuta())
                .orElseThrow(() -> new BusinessException(
                        "OrdenRuta no encontrada con id: " + dto.getIdOrdenRuta(), HttpStatus.NOT_FOUND));
        List<Ruta> rutas = dto.getRutaIds().stream()
                .map(this::findEntityById)
                .collect(Collectors.toList());
        rutas.forEach(r -> r.setOrdenRuta(orden));
        rutaRepository.saveAll(rutas);
        return new OrdenRutaResponseDTO(orden.getIdOrdenRuta(), orden.getTiempoEstimadoMin());
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

    private RutaSimpleResponseDTO toSimpleResponseDTO(Ruta ruta) {
        Integer idOrdenRuta = ruta.getOrdenRuta() != null ? ruta.getOrdenRuta().getIdOrdenRuta() : null;
        return new RutaSimpleResponseDTO(
                ruta.getIdRuta(),
                ruta.getUuidRuta(),
                ruta.getNombre(),
                ruta.isActive(),
                ruta.getTarifaEnvio(),
                idOrdenRuta
        );
    }

    private RutaResponseDTO toResponseDTO(Ruta ruta) {
        Coordinate[] coords = ruta.getBoundary().getCoordinates();
        // JTS Polygon cierra el anillo repitiendo el primer punto al final; se omite ese duplicado
        List<RutaResponseDTO.CoordinateDTO> coordinates = Arrays.stream(coords)
                .limit(coords.length - 1)
                .map(c -> new RutaResponseDTO.CoordinateDTO(c.y, c.x))
                .collect(Collectors.toList());
        Integer idOrdenRuta = ruta.getOrdenRuta() != null ? ruta.getOrdenRuta().getIdOrdenRuta() : null;
        return new RutaResponseDTO(
                ruta.getIdRuta(),
                ruta.getUuidRuta(),
                ruta.getNombre(),
                coordinates,
                ruta.isActive(),
                ruta.getTarifaEnvio(),
                idOrdenRuta
        );
    }
}
