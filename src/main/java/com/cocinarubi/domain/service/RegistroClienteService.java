package com.cocinarubi.domain.service;

import com.cocinarubi.dao.RegistroClienteRepository;
import com.cocinarubi.dao.RutaRepository;
import com.cocinarubi.domain.entity.RegistroCliente;
import com.cocinarubi.domain.entity.Ruta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.RegistroClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.RegistroClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroClienteService {

    private final RegistroClienteRepository registroClienteRepository;
    private final RutaRepository rutaRepository;

    public RegistroClienteService(RegistroClienteRepository registroClienteRepository,
                                   RutaRepository rutaRepository) {
        this.registroClienteRepository = registroClienteRepository;
        this.rutaRepository = rutaRepository;
    }

    @Transactional(readOnly = true)
    public Page<RegistroClienteResponseDTO> findAll(PageRequest pageable) {
        return registroClienteRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public RegistroClienteResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<RegistroClienteResponseDTO> findByTelefono(String telefono, PageRequest pageable) {
        return registroClienteRepository.findByTelefonoContaining(telefono, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public RegistroClienteResponseDTO save(RegistroClienteRequestDTO dto) {
        if (registroClienteRepository.existsByTelefono(dto.getTelefono())) {
            throw new BusinessException(
                    "Ya existe un cliente con el teléfono: " + dto.getTelefono(), HttpStatus.CONFLICT);
        }
        RegistroCliente entidad = RegistroCliente.builder()
                .nombre(dto.getNombre())
                .telefono(dto.getTelefono())
                .ruta(resolverRuta(dto.getIdRuta()))
                .direccion(dto.getDireccion())
                .build();
        return toResponseDTO(registroClienteRepository.save(entidad));
    }

    @Transactional
    public RegistroClienteResponseDTO update(int id, RegistroClienteRequestDTO dto) {
        RegistroCliente existente = findEntityById(id);
        if (!existente.getTelefono().equals(dto.getTelefono())
                && registroClienteRepository.existsByTelefono(dto.getTelefono())) {
            throw new BusinessException(
                    "Ya existe un cliente con el teléfono: " + dto.getTelefono(), HttpStatus.CONFLICT);
        }
        existente.setNombre(dto.getNombre());
        existente.setTelefono(dto.getTelefono());
        existente.setRuta(resolverRuta(dto.getIdRuta()));
        existente.setDireccion(dto.getDireccion());
        return toResponseDTO(registroClienteRepository.save(existente));
    }

    public void delete(int id) {
        if (!registroClienteRepository.existsById(id)) {
            throw new BusinessException(
                    "Registro de cliente no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        registroClienteRepository.deleteById(id);
    }

    public RegistroCliente findEntityById(int id) {
        return registroClienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Registro de cliente no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    private Ruta resolverRuta(Integer idRuta) {
        if (idRuta == null) return null;
        return rutaRepository.findById(idRuta)
                .orElseThrow(() -> new BusinessException(
                        "Ruta no encontrada con id: " + idRuta, HttpStatus.BAD_REQUEST));
    }

    private RegistroClienteResponseDTO toResponseDTO(RegistroCliente entidad) {
        return new RegistroClienteResponseDTO(
                entidad.getIdRegistroCliente(),
                entidad.getNombre(),
                entidad.getTelefono(),
                entidad.getRuta() != null ? entidad.getRuta().getIdRuta() : null,
                entidad.getRuta() != null ? entidad.getRuta().getNombre() : null,
                entidad.getDireccion()
        );
    }
}
