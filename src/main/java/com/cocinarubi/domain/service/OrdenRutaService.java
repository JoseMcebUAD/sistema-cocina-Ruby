package com.cocinarubi.domain.service;

import com.cocinarubi.dao.OrdenRutaRepository;
import com.cocinarubi.domain.entity.OrdenRuta;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.OrdenRutaRequestDTO;
import com.cocinarubi.presentation.dto.response.OrdenRutaResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Gestiona los datos editables de un grupo de rutas (OrdenRuta).
 * Capa: Service — lógica de negocio sobre tiempo estimado y horario de llegada.
 */
@Service
public class OrdenRutaService {

    private final OrdenRutaRepository ordenRutaRepository;

    public OrdenRutaService(OrdenRutaRepository ordenRutaRepository) {
        this.ordenRutaRepository = ordenRutaRepository;
    }

    /**
     * Reemplaza todos los campos editables de un grupo de rutas (PUT completo).
     * Un campo null en el DTO limpia el valor en base de datos.
     */
    public OrdenRutaResponseDTO actualizarOrdenRuta(int id, OrdenRutaRequestDTO dto) {
        OrdenRuta orden = ordenRutaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "OrdenRuta no encontrada con id: " + id, HttpStatus.NOT_FOUND));

        orden.setTiempoEstimadoMin(dto.getTiempoEstimadoMin());
        orden.setHoraLlegadaDesde(dto.getHoraLlegadaDesde());
        orden.setHoraLlegadaHasta(dto.getHoraLlegadaHasta());

        return toResponseDTO(ordenRutaRepository.save(orden));
    }

    private OrdenRutaResponseDTO toResponseDTO(OrdenRuta orden) {
        return new OrdenRutaResponseDTO(
                orden.getIdOrdenRuta(),
                orden.getTiempoEstimadoMin(),
                orden.getHoraLlegadaDesde(),
                orden.getHoraLlegadaHasta()
        );
    }
}
