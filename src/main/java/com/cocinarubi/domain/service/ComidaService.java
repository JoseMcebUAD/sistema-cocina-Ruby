package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/** Gestiona el catálogo de comidas disponibles en el menú del restaurante. */
@Service
public class ComidaService {

    private final ComidaRepository comidaRepository;
    private final ComplementoService complementoService;

    public ComidaService(ComidaRepository comidaRepository, ComplementoService complementoService) {
        this.comidaRepository = comidaRepository;
        this.complementoService = complementoService;
    }

    public List<Comida> findAll() {
        return comidaRepository.findAll();
    }

    public Page<Comida> findAll(Pageable pageable) {
        return comidaRepository.findAllPaginado(pageable);
    }

    public List<Comida> findDisponibles() {
        return comidaRepository.findDisponiblesOrdenados(DBConstants.Estatus.DISPONIBLE);
    }

    public Page<Comida> findDisponibles(Pageable pageable) {
        return comidaRepository.findDisponiblesPaginado(DBConstants.Estatus.DISPONIBLE, pageable);
    }

    public Comida findById(int id) {
        return comidaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Comida no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    @CacheEvict(value = "menu-web", allEntries = true)
    public Comida save(Comida comida) {
        if (comida.getPrecioMedia() != null && comida.getPrecioEntera() != null
                && comida.getPrecioMedia().compareTo(comida.getPrecioEntera()) >= 0) {
            throw new BusinessException(
                    "El precio de media porción debe ser menor al precio de la porción entera",
                    HttpStatus.BAD_REQUEST);
        }
        if (comida.getLimiteComplemento() == null) {
            comida.setLimiteComplemento(0);
        }
        adjuntarComplementosPredeterminados(comida);
        return comidaRepository.save(comida);
    }

    private void adjuntarComplementosPredeterminados(Comida comida) {
        var comps = comida.getComplementosPredeterminados();
        if (comps == null || comps.isEmpty()) return;

        var complementosMap = complementoService.findAllByIds(
                comps.stream().map(c -> c.getComplemento().getIdComplemento()).toList()
        ).stream().collect(Collectors.toMap(c -> c.getIdComplemento(), c -> c));

        comps.forEach(c -> {
            c.setComida(comida);
            c.setComplemento(complementosMap.get(c.getComplemento().getIdComplemento()));
        });
    }

    @CacheEvict(value = "menu-web", allEntries = true)
    public void delete(int id, boolean saltarConfirmacion) {
        if (!comidaRepository.existsById(id)) {
            throw new BusinessException("Comida no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (!saltarConfirmacion && comidaRepository.existsEnPedidos(id)) {
            throw new AdvertenciaEliminacionException(
                    "Esta comida tiene pedidos relacionados. ¿Desea continuar con la eliminación?");
        }
        if (!saltarConfirmacion && comidaRepository.existsEnBasicos(id)) {
            throw new AdvertenciaEliminacionException(
                    "Esta comida está referenciada en paquetes básicos. ¿Desea continuar con la eliminación?");
        }
        comidaRepository.deleteById(id);
    }

    @CacheEvict(value = "menu-web", allEntries = true)
    public Comida toggleDestacado(int id) {
        Comida comida = findById(id);
        comida.setDestacado(!comida.isDestacado());
        return comidaRepository.save(comida);
    }
}
