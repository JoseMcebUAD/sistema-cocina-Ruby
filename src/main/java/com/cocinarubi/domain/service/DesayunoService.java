package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

/** Gestiona el catálogo de desayunos disponibles en el menú del restaurante. */
@Service
public class DesayunoService {

    private final DesayunoRepository desayunoRepository;

    public DesayunoService(DesayunoRepository desayunoRepository) {
        this.desayunoRepository = desayunoRepository;
    }

    public List<Desayuno> findAll() {
        return desayunoRepository.findAll();
    }

    public Page<Desayuno> findAll(Pageable pageable) {
        return desayunoRepository.findAllPaginado(pageable);
    }

    public List<Desayuno> findDisponibles() {
        return desayunoRepository.findDisponiblesOrdenados(DBConstants.Estatus.DISPONIBLE);
    }

    public Page<Desayuno> findDisponibles(Pageable pageable) {
        return desayunoRepository.findDisponiblesPaginado(DBConstants.Estatus.DISPONIBLE, pageable);
    }

    public Desayuno findById(int id) {
        return desayunoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Desayuno no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public Desayuno save(Desayuno desayuno) {
        return desayunoRepository.save(desayuno);
    }

    public void delete(int id, boolean saltarConfirmacion) {
        if (!desayunoRepository.existsById(id)) {
            throw new BusinessException("Desayuno no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (!saltarConfirmacion && desayunoRepository.existsEnPedidos(id)) {
            throw new AdvertenciaEliminacionException(
                    "Este desayuno tiene pedidos relacionados. ¿Desea continuar con la eliminación?");
        }
        desayunoRepository.deleteById(id);
    }

    public Desayuno toggleDestacado(int id) {
        Desayuno desayuno = findById(id);
        desayuno.setDestacado(!desayuno.isDestacado());
        return desayunoRepository.save(desayuno);
    }
}
