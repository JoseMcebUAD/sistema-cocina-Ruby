package com.cocinarubi.domain.service;

import com.cocinarubi.dao.CodigoClienteRepository;
import com.cocinarubi.domain.entity.CodigoCliente;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.CodigoClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.CodigoClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodigoClienteService {

    private final CodigoClienteRepository codigoClienteRepository;

    public CodigoClienteService(CodigoClienteRepository codigoClienteRepository) {
        this.codigoClienteRepository = codigoClienteRepository;
    }

    @Transactional(readOnly = true)
    public Page<CodigoClienteResponseDTO> findAll(PageRequest pageable) {
        return codigoClienteRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<CodigoClienteResponseDTO> findAll() {
        return codigoClienteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CodigoClienteResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public CodigoClienteResponseDTO save(CodigoClienteRequestDTO dto) {
        if (codigoClienteRepository.existsByCodigoCliente(dto.getCodigoCliente())) {
            throw new BusinessException(
                    "El código '" + dto.getCodigoCliente() + "' ya existe",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
        CodigoCliente entidad = CodigoCliente.builder()
                .identificador(dto.getIdentificador())
                .codigoCliente(dto.getCodigoCliente())
                .tarifaEspecial(dto.getTarifaEspecial())
                .estatus(dto.getEstatus())
                .build();
        return toResponseDTO(codigoClienteRepository.save(entidad));
    }

    @Transactional
    public CodigoClienteResponseDTO update(int id, CodigoClienteRequestDTO dto) {
        CodigoCliente existente = findEntityById(id);
        // RF-047: no se permite modificar los caracteres del código
        if (!existente.getCodigoCliente().equals(dto.getCodigoCliente())) {
            throw new BusinessException(
                    "No se puede modificar el código del cliente, solo identificador, tarifa y estatus",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
        existente.setIdentificador(dto.getIdentificador());
        existente.setTarifaEspecial(dto.getTarifaEspecial());
        existente.setEstatus(dto.getEstatus());
        return toResponseDTO(codigoClienteRepository.save(existente));
    }

    public void delete(int id) {
        if (!codigoClienteRepository.existsById(id)) {
            throw new BusinessException(
                    "Código de cliente no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        codigoClienteRepository.deleteById(id);
    }

    public CodigoCliente findEntityById(int id) {
        return codigoClienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Código de cliente no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private CodigoClienteResponseDTO toResponseDTO(CodigoCliente entidad) {
        return new CodigoClienteResponseDTO(
                entidad.getIdCodigoCliente(),
                entidad.getIdentificador(),
                entidad.getCodigoCliente(),
                entidad.getTarifaEspecial(),
                entidad.getEstatus()
        );
    }
}
