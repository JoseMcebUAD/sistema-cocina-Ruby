package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ComplementoRepository;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.exception.BusinessException;
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

    public void delete(int id) {
        if (!complementoRepository.existsById(id)) {
            throw new BusinessException("Complemento no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        // Guardar integridad referencial: la DB no tiene ON DELETE CASCADE para estas relaciones
        if (complementoRepository.countEnBasicos(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar el complemento porque está referenciado en paquetes básicos",
                    HttpStatus.CONFLICT);
        }
        if (complementoRepository.countEnPedidos(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar el complemento porque está referenciado en pedidos existentes",
                    HttpStatus.CONFLICT);
        }
        complementoRepository.deleteById(id);
    }
}
