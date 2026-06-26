package com.cocinarubi.service;

import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.entity.Comida;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ComidaService {

    private final ComidaRepository comidaRepository;

    public ComidaService(ComidaRepository comidaRepository) {
        this.comidaRepository = comidaRepository;
    }

    public List<Comida> findAll() {
        return comidaRepository.findAll();
    }

    public Comida findById(int id) {
        return comidaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Comida no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    public Comida save(Comida comida) {
        return comidaRepository.save(comida);
    }

    public void delete(int id) {
        if (!comidaRepository.existsById(id)) {
            throw new BusinessException("Comida no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        if (comidaRepository.countEnPedidos(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar la comida porque está referenciada en pedidos existentes",
                    HttpStatus.CONFLICT);
        }
        if (comidaRepository.countEnBasicos(id) > 0) {
            throw new BusinessException(
                    "No se puede eliminar la comida porque está referenciada en paquetes básicos",
                    HttpStatus.CONFLICT);
        }
        comidaRepository.deleteById(id);
    }
}
