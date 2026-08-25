package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.exception.AdvertenciaEliminacionException;
import com.cocinarubi.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

/** Gestiona los complementos opcionales que se pueden agregar a un paquete básico (ej. ensalada, postre). */
@Service
public class ComplementoService {

    private final ComplementoRepository complementoRepository;

    public ComplementoService(ComplementoRepository complementoRepository) {
        this.complementoRepository = complementoRepository;
    }

    public List<Complemento> findAll() {
        return complementoRepository.findAll();
    }

    public Page<Complemento> findAll(Pageable pageable) {
        return complementoRepository.findAllPaginado(pageable);
    }

    public List<Complemento> findDisponibles() {
        return complementoRepository.findDisponiblesOrdenados(DBConstants.Estatus.DISPONIBLE);
    }

    public Complemento findById(int id) {
        return complementoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Complemento no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public Complemento save(Complemento complemento) {
        return complementoRepository.save(complemento);
    }

    public void delete(int id, boolean saltarConfirmacion) {
        if (!complementoRepository.existsById(id)) {
            throw new BusinessException("Complemento no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (!saltarConfirmacion && complementoRepository.existsEnBasicos(id)) {
            throw new AdvertenciaEliminacionException(
                    "Este complemento forma parte de uno o más paquetes básicos. Al eliminarlo, los básicos asociados también serán eliminados. ¿Desea continuar?");
        }
        if (!saltarConfirmacion && complementoRepository.existsEnPedidos(id)) {
            throw new AdvertenciaEliminacionException(
                    "Este complemento tiene pedidos relacionados. ¿Desea continuar con la eliminación?");
        }
        complementoRepository.deleteById(id);
    }
}
