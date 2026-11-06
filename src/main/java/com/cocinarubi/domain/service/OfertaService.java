package com.cocinarubi.domain.service;

import com.cocinarubi.dao.OfertaRepository;
import com.cocinarubi.domain.entity.Oferta;
import com.cocinarubi.domain.mapper.OfertaMapper;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.exception.ErrorCode;
import com.cocinarubi.presentation.dto.request.OfertaLineaRequestDTO;
import com.cocinarubi.presentation.dto.request.OfertaRequestDTO;
import com.cocinarubi.presentation.dto.response.OfertaResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.OfertaValidationImp;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocinarubi.Constants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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
        LocalDate hoy = LocalDate.now(Constants.ZONA_MERIDA);
        return ofertaRepository.findActivasConLineas(hoy)
                .stream()
                .map(ofertaMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve las entidades {@link Oferta} activas y vigentes con sus líneas cargadas,
     * cacheadas en Redis para evitar la query en cada llamada del preview y del save.
     * La fecha se incluye en la clave de caché: al cruce de medianoche la clave cambia
     * automáticamente → cache miss → query fresca sin necesidad de scheduled eviction.
     *
     * Las colecciones se copian a ArrayList/HashSet planos dentro de la transacción para
     * que Redis no serialice PersistentBag/PersistentSet de Hibernate, los cuales no pueden
     * deserializarse fuera de una sesión JPA y causarían LazyInitializationException al leer la caché.
     */
    @Cacheable(value = "ofertas-activas")
    @Transactional(readOnly = true)
    public List<Oferta> findActivasEntidades(LocalDate hoy) {
        List<Oferta> ofertas = ofertaRepository.findActivasConLineas(hoy);
        ofertas.forEach(o -> {
            o.setLineas(new ArrayList<>(o.getLineas()));
            o.setTiposPedidoRestriccion(new HashSet<>(o.getTiposPedidoRestriccion()));
        });
        return ofertas;
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

    @Caching(evict = {
        @CacheEvict(value = "menu-web", allEntries = true),
        @CacheEvict(value = "ofertas-activas", allEntries = true)
    })
    @Transactional
    public OfertaResponseDTO save(OfertaRequestDTO dto) {
        ofertaValidation.validarPost(dto);
        if (ofertaRepository.existsByPrioridad(dto.getPrioridad())) {
            throw new BusinessException(
                    "Ya existe una oferta con prioridad " + dto.getPrioridad(),
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
        return ofertaMapper.toResponse(ofertaRepository.save(ofertaMapper.toEntity(dto)));
    }

    /**
     * Reemplaza todos los campos y líneas de matching de la oferta existente.
     * Las líneas antiguas se eliminan por orphanRemoval; las nuevas se insertan.
     */
    @Caching(evict = {
        @CacheEvict(value = "menu-web", allEntries = true),
        @CacheEvict(value = "ofertas-activas", allEntries = true)
    })
    @Transactional
    public OfertaResponseDTO update(int id, OfertaRequestDTO dto) {
        ofertaValidation.validarPost(dto);
        if (ofertaRepository.existsByPrioridadAndIdOfertaNot(dto.getPrioridad(), id)) {
            throw new BusinessException(
                    "Ya existe una oferta con prioridad " + dto.getPrioridad(),
                    HttpStatus.BAD_REQUEST, ErrorCode.VALIDACION);
        }
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

    @Caching(evict = {
        @CacheEvict(value = "menu-web", allEntries = true),
        @CacheEvict(value = "ofertas-activas", allEntries = true)
    })
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
    @Caching(evict = {
        @CacheEvict(value = "menu-web", allEntries = true),
        @CacheEvict(value = "ofertas-activas", allEntries = true)
    })
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
