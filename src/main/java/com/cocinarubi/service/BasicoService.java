package com.cocinarubi.service;

import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.presentation.dto.request.BasicoRequestDTO;
import com.cocinarubi.presentation.dto.response.BasicoResponseDTO;
import com.cocinarubi.entity.Basico;
import com.cocinarubi.entity.Comida;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BasicoService {

    private final BasicoRepository basicoRepository;
    private final ComidaService comidaService;

    public BasicoService(BasicoRepository basicoRepository, ComidaService comidaService) {
        this.basicoRepository = basicoRepository;
        this.comidaService = comidaService;
    }

    public List<BasicoResponseDTO> findAll() {
        return basicoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BasicoResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    public BasicoResponseDTO save(BasicoRequestDTO dto) {
        Comida comida = comidaService.findById(dto.getIdComida());
        Basico basico = Basico.builder()
                .comida(comida)
                .descripcion(dto.getDescripcion())
                .destacado(dto.isDestacado())
                .precioBasico(dto.getPrecioBasico())
                .build();
        return toResponseDTO(basicoRepository.save(basico));
    }

    public BasicoResponseDTO update(int id, BasicoRequestDTO dto) {
        Basico existente = findEntityById(id);
        existente.setComida(comidaService.findById(dto.getIdComida()));
        existente.setDescripcion(dto.getDescripcion());
        existente.setDestacado(dto.isDestacado());
        existente.setPrecioBasico(dto.getPrecioBasico());
        return toResponseDTO(basicoRepository.save(existente));
    }

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
            existente.setPrecioBasico(new BigDecimal(payload.get("precioBasico").toString()));
        }
        return toResponseDTO(basicoRepository.save(existente));
    }

    public void delete(int id) {
        if (!basicoRepository.existsById(id)) {
            throw new BusinessException("Básico no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        basicoRepository.deleteById(id);
    }

    private Basico findEntityById(int id) {
        return basicoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Básico no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private BasicoResponseDTO toResponseDTO(Basico basico) {
        long totalComplementos = basicoRepository.countComplementosByBasicoId(basico.getIdBasico());
        return new BasicoResponseDTO(
                basico.getIdBasico(),
                basico.getComida().getNombreComida(),
                basico.getDescripcion(),
                basico.isDestacado(),
                basico.getPrecioBasico(),
                (int) totalComplementos
        );
    }
}
