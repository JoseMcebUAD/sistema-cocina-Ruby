package com.cocinarubi.domain.service;

import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.presentation.dto.request.BasicoRequestDTO;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.presentation.dto.response.ComplementoResponseDTO;
import com.cocinarubi.domain.entity.Basico;
import com.cocinarubi.domain.entity.BasicoComplemento;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestiona los paquetes básicos del menú: combinaciones de una comida principal
 * con complementos opcionales y un precio fijo.
 */
@Service
public class BasicoService {

    private final BasicoRepository basicoRepository;
    private final ComidaService comidaService;
    private final ComplementoService complementoService;

    public BasicoService(BasicoRepository basicoRepository, ComidaService comidaService,
                         ComplementoService complementoService) {
        this.basicoRepository = basicoRepository;
        this.comidaService = comidaService;
        this.complementoService = complementoService;
    }

    @Transactional(readOnly = true)
    public List<BasicoResponseDTO> findAll() {
        return basicoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BasicoResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public BasicoResponseDTO save(BasicoRequestDTO dto) {
        Comida comida = comidaService.findById(dto.getIdComida());
        Basico basico = Basico.builder()
                .comida(comida)
                .descripcion(dto.getDescripcion())
                .destacado(dto.isDestacado())
                .precioBasico(dto.getPrecioBasico())
                .build();
        agregarComplementos(basico, dto.getIdComplementos());
        return toResponseDTO(basicoRepository.save(basico));
    }

    @Transactional
    public BasicoResponseDTO update(int id, BasicoRequestDTO dto) {
        Basico existente = findEntityById(id);
        existente.setComida(comidaService.findById(dto.getIdComida()));
        existente.setDescripcion(dto.getDescripcion());
        existente.setDestacado(dto.isDestacado());
        existente.setPrecioBasico(dto.getPrecioBasico());
        // Reemplazar la lista completa de complementos en cada actualización total
        existente.getComplementos().clear();
        agregarComplementos(existente, dto.getIdComplementos());
        return toResponseDTO(basicoRepository.save(existente));
    }

    @Transactional
    public BasicoResponseDTO patch(int id, Map<String, Object> payload) {
        Basico existente = findEntityById(id);
        if (payload.containsKey("idComida")) {
            existente.setComida(comidaService.findById(((Number) payload.get("idComida")).intValue()));
        }
        if (payload.containsKey("descripcion")) {
            existente.setDescripcion((String) payload.get("descripcion"));
        }
        if (payload.containsKey("destacado")) {
            existente.setDestacado((Boolean) payload.get("destacado"));
        }
        if (payload.containsKey("precioBasico")) {
            // El payload JSON llega como Double; se convierte a String primero para evitar pérdida de precisión
            existente.setPrecioBasico(new BigDecimal(payload.get("precioBasico").toString()));
        }
        if (payload.containsKey("idComplementos")) {
            // Jackson deserializa números de JSON como Integer o Long según el tamaño; se normaliza con Number
            List<Integer> ids = ((List<?>) payload.get("idComplementos")).stream()
                    .map(o -> ((Number) o).intValue())
                    .collect(Collectors.toList());
            existente.getComplementos().clear();
            agregarComplementos(existente, ids);
        }
        return toResponseDTO(basicoRepository.save(existente));
    }

    public void delete(int id) {
        // Verificar existencia antes de borrar para devolver 404 en lugar del silencioso no-op de JPA
        if (!basicoRepository.existsById(id)) {
            throw new BusinessException("Básico no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        basicoRepository.deleteById(id);
    }

    /** Vincula los complementos al básico; no hace nada si la lista está vacía o es nula. */
    private void agregarComplementos(Basico basico, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Integer idComp : ids) {
            basico.addComplemento(BasicoComplemento.builder()
                    .complemento(complementoService.findById(idComp))
                    .build());
        }
    }

    /** Usa la query con JOIN FETCH para evitar N+1 al cargar los complementos. */
    private Basico findEntityById(int id) {
        return basicoRepository.findByIdWithComplementos(id)
                .orElseThrow(() -> new BusinessException(
                        "Básico no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private BasicoResponseDTO toResponseDTO(Basico basico) {
        List<ComplementoResponseDTO> complementos = basico.getComplementos().stream()
                .map(bc -> new ComplementoResponseDTO(
                        bc.getComplemento().getIdComplemento(),
                        bc.getComplemento().getNombreComplemento(),
                        bc.getComplemento().getPrecioExtra()))
                .collect(Collectors.toList());
        return new BasicoResponseDTO(
                basico.getIdBasico(),
                basico.getComida().getNombreComida(),
                basico.getDescripcion(),
                basico.isDestacado(),
                basico.getPrecioBasico(),
                complementos
        );
    }
}
