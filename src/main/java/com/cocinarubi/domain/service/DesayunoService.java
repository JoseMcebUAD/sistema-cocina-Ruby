package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.domain.entity.Desayuno;
import com.cocinarubi.exception.BusinessException;
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

    public List<Desayuno> findDisponibles() {
        return desayunoRepository.findDisponiblesOrdenados(DBConstants.Estatus.DISPONIBLE);
    }

    public Desayuno findById(int id) {
        return desayunoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Desayuno no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public Desayuno save(Desayuno desayuno) {
        return desayunoRepository.save(desayuno);
    }

    public void delete(int id) {
        if (!desayunoRepository.existsById(id)) {
            throw new BusinessException("Desayuno no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        // Evitar eliminación si el desayuno está asociado a pedidos históricos
        if (desayunoRepository.countEnPedidos(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar el desayuno porque está referenciado en pedidos existentes",
                    HttpStatus.CONFLICT);
        }
        desayunoRepository.deleteById(id);
    }
}
