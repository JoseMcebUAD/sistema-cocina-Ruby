package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

/** Gestiona el catálogo de comidas disponibles en el menú del restaurante. */
@Service
public class ComidaService {

    private final ComidaRepository comidaRepository;

    public ComidaService(ComidaRepository comidaRepository) {
        this.comidaRepository = comidaRepository;
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

    public Comida findById(int id) {
        return comidaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Comida no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    public Comida save(Comida comida) {
        //verifica que el precio de la media sea menor al de la entera
        if (comida.getPrecioMedia() != null && comida.getPrecioEntera() != null
                && comida.getPrecioMedia().compareTo(comida.getPrecioEntera()) >= 0) {
            throw new BusinessException(
                    "El precio de media porción debe ser menor al precio de la porción entera",
                    HttpStatus.BAD_REQUEST);
        }
        return comidaRepository.save(comida);
    }

    public void delete(int id) {
        if (!comidaRepository.existsById(id)) {
            throw new BusinessException("Comida no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        // Guardar integridad referencial: la DB no tiene ON DELETE CASCADE para estas relaciones
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
