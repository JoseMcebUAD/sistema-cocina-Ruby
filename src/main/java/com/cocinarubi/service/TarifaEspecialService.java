package com.cocinarubi.service;

import com.cocinarubi.dao.TarifaEspecialRepository;
import com.cocinarubi.presentation.dto.request.TarifaEspecialRequestDTO;
import com.cocinarubi.entity.TarifaEspecial;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TarifaEspecialService {

    private final TarifaEspecialRepository tarifaEspecialRepository;

    public TarifaEspecialService(TarifaEspecialRepository tarifaEspecialRepository) {
        this.tarifaEspecialRepository = tarifaEspecialRepository;
    }

    public List<TarifaEspecial> findAll() {
        return tarifaEspecialRepository.findAll();
    }

    public TarifaEspecial findById(int id) {
        return tarifaEspecialRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Tarifa especial no encontrada con id: " + id, HttpStatus.NOT_FOUND));
    }

    public TarifaEspecial save(TarifaEspecialRequestDTO dto) {
        TarifaEspecial tarifa = TarifaEspecial.builder()
                .nombreTarifa(dto.getNombreTarifa())
                .tarifa(dto.getTarifa())
                .isActive(dto.isActive())
                .build();
        return tarifaEspecialRepository.save(tarifa);
    }

    public TarifaEspecial update(int id, TarifaEspecialRequestDTO dto) {
        TarifaEspecial existente = findById(id);
        existente.setNombreTarifa(dto.getNombreTarifa());
        existente.setTarifa(dto.getTarifa());
        existente.setActive(dto.isActive());
        return tarifaEspecialRepository.save(existente);
    }

    public TarifaEspecial patch(int id, Map<String, Object> payload) {
        TarifaEspecial existente = findById(id);
        if (payload.containsKey("nombreTarifa")) {
            existente.setNombreTarifa((String) payload.get("nombreTarifa"));
        }
        if (payload.containsKey("tarifa")) {
            existente.setTarifa(new BigDecimal(payload.get("tarifa").toString()));
        }
        if (payload.containsKey("isActive")) {
            existente.setActive((Boolean) payload.get("isActive"));
        }
        return tarifaEspecialRepository.save(existente);
    }

    public void delete(int id) {
        if (!tarifaEspecialRepository.existsById(id)) {
            throw new BusinessException(
                    "Tarifa especial no encontrada con id: " + id, HttpStatus.NOT_FOUND);
        }
        tarifaEspecialRepository.deleteById(id);
    }
}
