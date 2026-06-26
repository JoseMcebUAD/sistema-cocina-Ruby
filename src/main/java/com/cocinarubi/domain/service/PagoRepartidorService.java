package com.cocinarubi.domain.service;

import com.cocinarubi.dao.PagoRepartidorRepository;
import com.cocinarubi.domain.entity.PagoRepartidor;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PagoRepartidorService {

    private final PagoRepartidorRepository pagoRepartidorRepository;

    public PagoRepartidorService(PagoRepartidorRepository pagoRepartidorRepository) {
        this.pagoRepartidorRepository = pagoRepartidorRepository;
    }

    public List<PagoRepartidor> findAll() {
        return pagoRepartidorRepository.findAll();
    }

    public PagoRepartidor findById(int id) {
        return pagoRepartidorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Pago de repartidor no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public PagoRepartidor save(PagoRepartidor pagoRepartidor) {
        return pagoRepartidorRepository.save(pagoRepartidor);
    }

    public void delete(int id) {
        if (!pagoRepartidorRepository.existsById(id)) {
            throw new BusinessException(
                    "Pago de repartidor no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        pagoRepartidorRepository.deleteById(id);
    }
}
