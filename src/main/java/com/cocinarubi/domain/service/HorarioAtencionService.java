package com.cocinarubi.domain.service;

import com.cocinarubi.dao.HorarioAtencionRepository;
import com.cocinarubi.domain.entity.HorarioAtencion;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

/** Gestiona los horarios de atención del restaurante (días y franjas horarias de servicio). */
@Service
public class HorarioAtencionService {

    private final HorarioAtencionRepository horarioAtencionRepository;

    public HorarioAtencionService(HorarioAtencionRepository horarioAtencionRepository) {
        this.horarioAtencionRepository = horarioAtencionRepository;
    }

    public List<HorarioAtencion> findAll() {
        return horarioAtencionRepository.findAll();
    }

    public HorarioAtencion findById(int id) {
        return horarioAtencionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Horario de atención no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public HorarioAtencion save(HorarioAtencion horarioAtencion) {
        return horarioAtencionRepository.save(horarioAtencion);
    }

    public void delete(int id) {
        // Verificar existencia antes de borrar para devolver 404 en lugar del silencioso no-op de JPA
        if (!horarioAtencionRepository.existsById(id)) {
            throw new BusinessException(
                    "Horario de atención no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        horarioAtencionRepository.deleteById(id);
    }
}
