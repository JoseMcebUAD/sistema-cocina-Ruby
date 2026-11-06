package com.cocinarubi.domain.service;

import com.cocinarubi.dao.SuscripcionSemanalRepository;
import com.cocinarubi.domain.entity.SuscripcionSemanal;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.SuscripcionSemanalRequestDTO;
import com.cocinarubi.presentation.dto.response.SuscripcionSemanalResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gestiona el ciclo de vida de las suscripciones semanales de comidas.
 * Capa: Service — lógica de negocio de suscripciones.
 */
@Service
public class SuscripcionSemanalService {

    private final SuscripcionSemanalRepository suscripcionSemanalRepository;

    public SuscripcionSemanalService(SuscripcionSemanalRepository suscripcionSemanalRepository) {
        this.suscripcionSemanalRepository = suscripcionSemanalRepository;
    }

    @Transactional(readOnly = true)
    public Page<SuscripcionSemanalResponseDTO> findAll(Pageable pageable) {
        return suscripcionSemanalRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<SuscripcionSemanalResponseDTO> findAll() {
        return suscripcionSemanalRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SuscripcionSemanalResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    /**
     * Crea una nueva suscripción semanal.
     * Si el request no incluye un código de recuperación, genera uno automáticamente
     * (8 caracteres alfanuméricos en mayúsculas derivados de un UUID).
     */
    @Transactional
    public SuscripcionSemanalResponseDTO save(SuscripcionSemanalRequestDTO dto) {
        String codigo = resolverCodigo(dto.getCodigoRecuperacion());
        if (suscripcionSemanalRepository.existsByCodigoRecuperacion(codigo)) {
            throw new BusinessException(
                    "El código de recuperación '" + codigo + "' ya existe",
                    HttpStatus.CONFLICT, ErrorCode.VALIDACION);
        }
        SuscripcionSemanal entidad = SuscripcionSemanal.builder()
                .identificadorCliente(dto.getIdentificadorCliente())
                .uuidCliente(dto.getUuidCliente())
                .semanaInicio(dto.getSemanaInicio())
                .comidasPorDia(dto.getComidasPorDia())
                .precioSuscripcion(dto.getPrecioSuscripcion())
                .metodoPago(dto.getMetodoPago())
                .codigoRecuperacion(codigo)
                .activo(dto.isActivo())
                .origen(dto.getOrigen())
                .build();
        return toResponseDTO(suscripcionSemanalRepository.save(entidad));
    }

    /**
     * Actualiza los campos editables de una suscripción existente.
     * Los campos inmutables (codigoRecuperacion, semanaInicio, origen) no se modifican.
     */
    @Transactional
    public SuscripcionSemanalResponseDTO update(int id, SuscripcionSemanalRequestDTO dto) {
        SuscripcionSemanal existente = findEntityById(id);
        existente.setIdentificadorCliente(dto.getIdentificadorCliente());
        existente.setUuidCliente(dto.getUuidCliente());
        existente.setComidasPorDia(dto.getComidasPorDia());
        existente.setPrecioSuscripcion(dto.getPrecioSuscripcion());
        existente.setMetodoPago(dto.getMetodoPago());
        existente.setActivo(dto.isActivo());
        return toResponseDTO(suscripcionSemanalRepository.save(existente));
    }

    @Transactional
    public void delete(int id) {
        if (!suscripcionSemanalRepository.existsById(id)) {
            throw new BusinessException(
                    "Suscripción semanal no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        suscripcionSemanalRepository.deleteById(id);
    }

    public SuscripcionSemanal findEntityById(int id) {
        return suscripcionSemanalRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Suscripción semanal no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    // Genera un código de 8 chars en mayúsculas si el caller no proporcionó uno.
    private String resolverCodigo(String codigoDelRequest) {
        if (codigoDelRequest != null && !codigoDelRequest.isBlank()) {
            return codigoDelRequest.trim().toUpperCase();
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private SuscripcionSemanalResponseDTO toResponseDTO(SuscripcionSemanal entidad) {
        return new SuscripcionSemanalResponseDTO(
                entidad.getIdSuscripcionSemanal(),
                entidad.getIdentificadorCliente(),
                entidad.getUuidCliente(),
                entidad.getSemanaInicio(),
                entidad.getComidasPorDia(),
                entidad.getPrecioSuscripcion(),
                entidad.getMetodoPago(),
                entidad.getCodigoRecuperacion(),
                entidad.isActivo(),
                entidad.getOrigen()
        );
    }
}
