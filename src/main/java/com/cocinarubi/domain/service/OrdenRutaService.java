package com.cocinarubi.domain.service;

import com.cocinarubi.dao.OrdenRutaRepository;
import com.cocinarubi.domain.entity.OrdenRuta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.response.OrdenRutaResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrdenRutaService {

    private final OrdenRutaRepository ordenRutaRepository;

    public OrdenRutaService(OrdenRutaRepository ordenRutaRepository) {
        this.ordenRutaRepository = ordenRutaRepository;
    }

    public OrdenRutaResponseDTO actualizarTiempoEstimado(int id, Integer tiempoEstimadoMin) {
        OrdenRuta orden = ordenRutaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "OrdenRuta no encontrada con id: " + id, HttpStatus.NOT_FOUND));
        orden.setTiempoEstimadoMin(tiempoEstimadoMin);
        return toResponseDTO(ordenRutaRepository.save(orden));
    }

    private OrdenRutaResponseDTO toResponseDTO(OrdenRuta orden) {
        return new OrdenRutaResponseDTO(orden.getIdOrdenRuta(), orden.getTiempoEstimadoMin());
    }
}
