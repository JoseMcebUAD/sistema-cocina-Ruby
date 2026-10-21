package com.cocinarubi.domain.service;

import com.cocinarubi.dao.OfertaRepository;
import com.cocinarubi.domain.entity.Oferta;
import com.cocinarubi.domain.mapper.OfertaMapper;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.OfertaLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.OfertaRequestDTO;
import com.cocinarubi.presentation.dto.response.OfertaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.OfertaValidationImp;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD del catálogo de {@link Oferta}.
 * Capa: Service — lógica de negocio de ofertas.
 */
@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final OfertaMapper ofertaMapper;
    private final OfertaValidationImp ofertaValidation;

    public OfertaService(OfertaRepository ofertaRepository,
                         OfertaMapper ofertaMapper,
                         OfertaValidationImp ofertaValidation) {
        this.ofertaRepository = ofertaRepository;
        this.ofertaMapper = ofertaMapper;
        this.ofertaValidation = ofertaValidation;
    }

    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> findActivas() {
        return ofertaRepository.findActivasConLineas()
                .stream()
                .map(ofertaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> findAll() {
        return ofertaRepository.findAll().stream()
                .map(ofertaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfertaResponseDTO findById(int id) {
        return ofertaMapper.toResponse(findEntityById(id));
    }

    @CacheEvict(value = "menu-web", allEntries = true)
    @Transactional
    public OfertaResponseDTO save(OfertaRequestDTO dto) {
        ofertaValidation.validarPost(dto);
        return ofertaMapper.toResponse(ofertaRepository.save(ofertaMapper.toEntity(dto)));
    }

    /**
     * Reemplaza todos los campos y líneas de matching de la oferta existente.
     * Las líneas antiguas se eliminan por orphanRemoval; las nuevas se insertan.
     */
    @CacheEvict(value = "menu-web", allEntries = true)
    @Transactional
    public OfertaResponseDTO update(int id, OfertaRequestDTO dto) {
        ofertaValidation.validarPost(dto);
        Oferta existente = findEntityById(id);
        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecioOferta(dto.getPrecioOferta());
        existente.setPrioridad(dto.getPrioridad());
        existente.setActivo(dto.isActivo());
        existente.setFechaInicio(dto.getFechaInicio());
        existente.setFechaFin(dto.getFechaFin());
        existente.getTiposPedidoRestriccion().clear();
        if (dto.getTiposPedidoRestriccion() != null) {
            existente.getTiposPedidoRestriccion().addAll(dto.getTiposPedidoRestriccion());
        }
        existente.getLineas().clear();
        for (OfertaLineaRequestDTO lineaDto : dto.getLineas()) {
            existente.addLinea(ofertaMapper.toLineaEntity(lineaDto));
        }
        return ofertaMapper.toResponse(ofertaRepository.save(existente));
    }

    @CacheEvict(value = "menu-web", allEntries = true)
    @Transactional
    public OfertaResponseDTO toggleActivo(int id) {
        Oferta oferta = findEntityById(id);
        oferta.setActivo(!oferta.isActivo());
        return ofertaMapper.toResponse(ofertaRepository.save(oferta));
    }

    /**
     * Elimina la oferta del catálogo. Si tiene pedidos asociados y {@code saltarConfirmacion}
     * es false, lanza {@link AdvertenciaEliminacionException} para que el front muestre el diálogo.
     */
    @CacheEvict(value = "menu-web", allEntries = true)
    @Transactional
    public void delete(int id, boolean saltarConfirmacion) {
        Oferta oferta = findEntityById(id);
        if (!saltarConfirmacion && ofertaRepository.existeEnPedidos(id)) {
            throw new AdvertenciaEliminacionException(
                    "La oferta '" + oferta.getNombre() + "' tiene pedidos asociados. "
                    + "¿Deseas eliminarla de todos modos?");
        }
        ofertaRepository.deleteById(id);
    }

    public Oferta findEntityById(int id) {
        return ofertaRepository.findByIdConLineas(id)
                .orElseThrow(() -> new BusinessException(
                        "Oferta no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }
}
