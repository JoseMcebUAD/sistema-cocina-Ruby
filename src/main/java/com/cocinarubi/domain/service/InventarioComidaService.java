package com.cocinarubi.domain.service;

import com.cocinarubi.dao.InventarioComidaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.InventarioComida;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.InventarioComidaRequestDTO;
import com.cocinarubi.presentation.dto.response.InventarioComidaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.InventarioComidaValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Gestiona el registro de consumo de insumos por platillo (RF-044/045). */
@Service
public class InventarioComidaService {

    private final InventarioComidaRepository inventarioComidaRepository;
    private final ComidaService comidaService;
    private final InventarioComidaValidationImp inventarioComidaValidation;

    public InventarioComidaService(InventarioComidaRepository inventarioComidaRepository,
                                   ComidaService comidaService,
                                   InventarioComidaValidationImp inventarioComidaValidation) {
        this.inventarioComidaRepository = inventarioComidaRepository;
        this.comidaService = comidaService;
        this.inventarioComidaValidation = inventarioComidaValidation;
    }

    @Transactional(readOnly = true)
    public List<InventarioComidaResponseDTO> findAll() {
        return inventarioComidaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventarioComidaResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<InventarioComidaResponseDTO> findByComida(int idComida) {
        return inventarioComidaRepository.findByComidaIdComida(idComida).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventarioComidaResponseDTO save(InventarioComidaRequestDTO dto) {
        inventarioComidaValidation.validarPost(dto);
        Comida comida = comidaService.findById(dto.getIdComida());
        InventarioComida entidad = InventarioComida.builder()
                .comida(comida)
                .cantidad(dto.getCantidad())
                .tipo_contador_comida(dto.getTipoContadorComida())
                .build();
        return toResponseDTO(inventarioComidaRepository.save(entidad));
    }

    @Transactional
    public InventarioComidaResponseDTO update(int id, InventarioComidaRequestDTO dto) {
        inventarioComidaValidation.validarPost(dto);
        InventarioComida existente = findEntityById(id);
        existente.setComida(comidaService.findById(dto.getIdComida()));
        existente.setCantidad(dto.getCantidad());
        existente.setTipo_contador_comida(dto.getTipoContadorComida());
        return toResponseDTO(inventarioComidaRepository.save(existente));
    }

    public void delete(int id) {
        if (!inventarioComidaRepository.existsById(id)) {
            throw new BusinessException(
                    "Registro de inventario no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        inventarioComidaRepository.deleteById(id);
    }

    private InventarioComida findEntityById(int id) {
        return inventarioComidaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Registro de inventario no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private InventarioComidaResponseDTO toResponseDTO(InventarioComida entidad) {
        Comida comida = entidad.getComida();
        return new InventarioComidaResponseDTO(
                entidad.getIdInventarioComida(),
                comida.getIdComida(),
                comida.getNombreComida(),
                entidad.getCantidad(),
                entidad.getTipo_contador_comida()
        );
    }
}
